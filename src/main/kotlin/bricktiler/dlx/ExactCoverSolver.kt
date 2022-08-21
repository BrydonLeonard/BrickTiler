package bricktiler.dlx

import bricktiler.Solution
import bricktiler.dlx.ExactCoverSolver.statTracker.badRowsDueToConstraints
import bricktiler.dlx.ExactCoverSolver.statTracker.badRowsDueToCovered
import bricktiler.dlx.ExactCoverSolver.statTracker.badRowsDueToError
import bricktiler.dlx.ExactCoverSolver.statTracker.deadEndDueToConstraints
import bricktiler.dlx.ExactCoverSolver.statTracker.deadEnds
import bricktiler.dlx.ExactCoverSolver.statTracker.depthCount
import bricktiler.dlx.ExactCoverSolver.statTracker.down
import bricktiler.dlx.ExactCoverSolver.statTracker.maxDepth
import bricktiler.dlx.ExactCoverSolver.statTracker.solutions
import bricktiler.dlx.ExactCoverSolver.statTracker.up
import bricktiler.dlx.Header.Companion.globalOpCounter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path
import kotlin.math.abs


enum class ErrorChecking {
    /**
     * Only exact matches will be allowed
     */
    ONLY_EXACT,

    /**
     * Errors will be allowed based on the error aggregator and max per piece errors
     */
    ALLOW_ERRORS,

    /**
     * Errors won't be checked at all
     */
    NO_CHECK
}

/**
 * @param onlyExactMatches If true, no error is allowed. I false, at most [maxPerPieceError] error is allowed per piece
 * @param exitOnFirstSolution Return as soon as possible after finding a solution. When searching recursively, may return more than 1.
 */
public data class SolutionConfig(
    val errorChecking: ErrorChecking = ErrorChecking.ALLOW_ERRORS,
    val maxPerPieceError: Int = 1,
    val exitOnFirstSolution: Boolean = false,
    val constraintValidator: ConstraintValidator = NoopConstraintValidator(),
    val maxRecursiveDepth: Int = 20
)

object ExactCoverSolver {

    fun solveOnce(matrix: SparseMatrix, config: SolutionConfig, dispatcher: CoroutineDispatcher): Solution? {
        var solution: Solution? = null
        File("./vis").deleteRecursively()
        Files.createDirectories(Path("./vis"))

        runBlocking(dispatcher) {
            val channel = Channel<Solution?>(100, BufferOverflow.DROP_LATEST)

            launch(dispatcher) {
                try {
                    println("Starting solver")
                    solve(matrix, config, channel)
                } catch (e: Exception) {
                    println(e)
                }
                println("Solver complete")
                channel.send(null)
            }

            println("Solver running")
            solution = channel.receive()
            println("Received")
            coroutineContext.cancelChildren()
        }
        return solution
    }

    /**
     * @param overridingValidator allows the validator in [config] to be overriden at any point
     */
    suspend fun solve(matrix: SparseMatrix, config: SolutionConfig = SolutionConfig(), channel: Channel<Solution?>, depth: Int = 0, currentSolution: Solution = Solution(), overridingValidator: ConstraintValidator? = null, id: String = "") {
        matrix.saveStateWithId("${id}_${globalOpCounter.get()}")
        depthCount.compute(depth) { _, value -> (value ?: 0) + 1 }
        maxDepth.updateAndGet { current -> if (current > depth) current else depth }
        val headerWithFewestOnes = matrix.shortestUncoveredColumn()
        if (headerWithFewestOnes == null) {
            channel.send(currentSolution.clone())
            solutions.incrementAndGet()
            up.incrementAndGet()
            return
        }

        val subRoutines = AtomicInteger(0)

        coroutineScope {
            yield()
            val deferredSolutions = mutableListOf<Job>()

            if (headerWithFewestOnes.nodes.size == 0) {
                deadEnds.incrementAndGet()
            }

            var constraintDrops = 0

            for ((row, node) in headerWithFewestOnes.nodes.entries) {
                matrix.saveStateWithId("${id}_${globalOpCounter.get()}_${row}_start")
                yield()
                if (node.covered) {
                    badRowsDueToCovered.incrementAndGet()
                    continue
                }

                // TODO: Speed up error calculation
                when (config.errorChecking) {
                    ErrorChecking.ONLY_EXACT -> if (!hasNoErrors(node)) {
                        badRowsDueToError.incrementAndGet()
                        continue
                    }

                    ErrorChecking.ALLOW_ERRORS -> if (calculateError(node, config) > config.maxPerPieceError) {
                        badRowsDueToError.incrementAndGet()
                        continue
                    }

                    else -> null
                }

                val validator = overridingValidator ?: config.constraintValidator

                if (!validator.addRowToSolution(row, currentSolution)) {
                    badRowsDueToConstraints.incrementAndGet()
                    constraintDrops++
                    continue
                }

                var iterNode = node.right

                while (iterNode != node) {
                    yield()
                    matrix.coverColumn(iterNode.header, id)
                    iterNode = iterNode.right
                }
                matrix.coverColumn(node.header, id)
                matrix.saveStateWithId("${id}_${globalOpCounter.get()}_${row}_covered")

                if (depth < config.maxRecursiveDepth) {
                    val clonedMatrix = matrix.cloneUncovered()
                    val clonedSolution = currentSolution.clone()
                    val validator = config.constraintValidator.clone()
                    deferredSolutions.add(
                        launch {
                            down.incrementAndGet()
                            solve(
                                clonedMatrix,
                                config,
                                channel,
                                depth + 1,
                                clonedSolution,
                                validator,
                                "$id-${subRoutines.getAndIncrement()}"
                            )
                        }
                    )
                } else {
                    down.incrementAndGet()
                    solve(matrix, config, channel, depth + 1, currentSolution, validator, id)
                }

                matrix.uncoverColumn(node.header, id)

                iterNode = node.left

                while (iterNode != node) {
                    yield()
                    matrix.uncoverColumn(iterNode.header, id)
                    iterNode = iterNode.left
                }

                matrix.saveStateWithId("${id}_${globalOpCounter.get()}_${row}_end")

                config.constraintValidator.removeRowFromSolution(node.row, currentSolution)
            }
            if (constraintDrops == headerWithFewestOnes.nodes.size) {
                deadEndDueToConstraints.incrementAndGet()
            }
        }
        up.incrementAndGet()
    }

    object statTracker {
        val deadEnds = AtomicInteger(0)
        val solutions = AtomicInteger(0)
        val maxDepth = AtomicInteger(0)
        val depthCount = ConcurrentHashMap<Int, Int>()
        val badRowsDueToConstraints = AtomicInteger(0)
        val badRowsDueToError = AtomicInteger(0)
        val badRowsDueToCovered = AtomicInteger(0)
        val deadEndDueToConstraints = AtomicInteger(0)
        val down = AtomicInteger(0)
        val up = AtomicInteger(0)
    }

    private fun hasNoErrors(startingNode: Node): Boolean {
        if (startingNode.value != startingNode.header.desiredValue) {
            return false
        }

        var node = startingNode.right

        while (node != startingNode) {
            if (node.value != node.header.desiredValue) {
                return false
            }
            node = node.right
        }
        return true
    }

    /**
     * Finds the total error for the whole row
     */
    private fun calculateError(startingNode: Node, config: SolutionConfig): Int {
        var count = 0

        var node = startingNode

        var error = 0

        while (node != startingNode || count == 0) {
            // Calculating absolute error is nice, but let's start by just trying to get any solutions
            // Maybe this will just implement anti-aliasing automatically?
            error += abs(node.value - node.header.desiredValue)
            node = node.right
            count++
        }

        return error
    }
}