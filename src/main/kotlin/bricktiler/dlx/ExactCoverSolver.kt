package bricktiler.dlx

import bricktiler.Solution
import bricktiler.board.PiecePosition
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean
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

    /**
     * @param overridingValidator allows the validator in [config] to be overriden at any point
     */
    suspend fun solve(matrix: SparseMatrix, config: SolutionConfig = SolutionConfig(), depth: Int = 0, currentSolution: Solution = Solution(), overridingValidator: ConstraintValidator? = null): List<Solution> {
        val headerWithFewestOnes = matrix.shortestUncoveredColumn() ?: return listOf(currentSolution.clone())

        val solutions = mutableListOf<Solution>()

        coroutineScope {
            val deferredSolutions = mutableListOf<Job>()
            val solutionChannel = Channel<List<Solution>>(50)

            for ((row, node) in headerWithFewestOnes.nodes.entries) {
                if (node.covered) {
                    continue
                }

                // TODO: Speed up error calculation
                when (config.errorChecking) {
                    ErrorChecking.ONLY_EXACT -> if (!hasNoErrors(node)) {
                        continue
                    }

                    ErrorChecking.ALLOW_ERRORS -> if (calculateError(node, config) > config.maxPerPieceError) {
                        continue
                    }

                    else -> null
                }

                val validator = overridingValidator ?: config.constraintValidator

                if (!validator.addRowToSolution(row, currentSolution)) {
                    continue
                }

                var iterNode = node.right

                while (iterNode != node) {
                    matrix.coverColumn(iterNode.header)
                    iterNode = iterNode.right
                }
                matrix.coverColumn(node.header)

                if (depth < config.maxRecursiveDepth) {
                    val clonedMatrix = matrix.cloneUncovered()
                    val clonedSolution = currentSolution.clone()
                    deferredSolutions.add(
                        launch {
                            val solutionSet = solve(
                                clonedMatrix,
                                config,
                                depth + 1,
                                clonedSolution,
                                config.constraintValidator.clone()
                            )
                            solutionChannel.send(solutionSet)
                        }
                    )
                } else {
                    val subSolutions = solve(matrix, config, depth + 1, currentSolution)

                    solutions.addAll(subSolutions)
                }

                iterNode = node.left

                while (iterNode != node) {
                    matrix.uncoverColumn(iterNode.header)
                    iterNode = iterNode.left
                }
                matrix.uncoverColumn(node.header)

                config.constraintValidator.removeRowFromSolution(node.row, currentSolution)

                if (solutions.size > 0 && config.exitOnFirstSolution) {
                    // Break out. We have what we need
                    break
                }
            }

            // If we're only looking for one solution, listen to the channel until it arrives, take it and GTFO.
            if (deferredSolutions.size > 0) {
                var done = false
                // While there are still jobs running
                while (!done) {
                    val nextSolutions = solutionChannel.receive()
                    if (nextSolutions.isNotEmpty()) {
                        solutions.addAll(nextSolutions)

                        if (config.exitOnFirstSolution) {
                            deferredSolutions.forEach { it.cancel() }
                            break
                        }
                    }

                    if (deferredSolutions.count { it.isActive } <= 0 && solutionChannel.isEmpty) {
                        done = true
                    }
                }
            }
        }

        return solutions
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