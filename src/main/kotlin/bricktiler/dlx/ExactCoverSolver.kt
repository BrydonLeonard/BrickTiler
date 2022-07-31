package bricktiler.dlx

import bricktiler.Solution
import kotlin.math.abs
import kotlin.random.Random

/**
 * @param onlyExactMatches If true, no error is allowed. I false, at most [maxPerPieceError] error is allowed per piece
 */
public data class SolutionConfig(
        val onlyExactMatches: Boolean = false,
        val maxPerPieceError: Double = 1.0,
        val errorAggregator: ErrorAggregator = ErrorAggregator.COUNT,
        val exitOnFirstSolution: Boolean = false
)

enum class ErrorAggregator(val aggregate: (List<Int>) -> Double) {
    COUNT({ errors -> errors.count { it != 0 }.toDouble() }),
    ABS_AVERAGE({ errors -> errors.map { abs(it) }.average() }),
    ABS_SUM({ errors -> errors.map { abs(it) }.sum().toDouble() })
}

object ExactCoverSolver {

    fun solve(matrix: SparseMatrix, config: SolutionConfig = SolutionConfig()): List<Solution> {
        val headerWithFewestOnes = matrix.shortestUncoveredColumn() ?: return listOf(Solution())

        val solutions = mutableListOf<Solution>()

        headerWithFewestOnes.nodes.toList().shuffled().forEach { (row, node) ->
            if (node.covered) {
                return@forEach
            }
            // TODO: Speed up error calculation
            val error = calculateError(node, config)

            if ((config.onlyExactMatches && error > 0) || (!config.onlyExactMatches && error > 1)) {
                return@forEach
            }

            val chosenRowIndex = node.row

            val columnsWithOnesInChosenRow = matrix.uncoveredHeaders().filter { it.getNodeInRow(chosenRowIndex) != null }

            // TODO inefficient
            columnsWithOnesInChosenRow.forEach { matrix.coverColumn(it) }

            val subSolutions = solve(matrix, config)

            // TODO inefficient
            columnsWithOnesInChosenRow.forEach { matrix.uncoverColumn(it) }

            solutions.addAll(
                    subSolutions.map {
                        it + node.row
                    }
            )

            if (solutions.size > 0 && config.exitOnFirstSolution) {
                return solutions
            }
        }

        return solutions
    }

    /**
     * Finds the total error for the whole row
     */
    private fun calculateError(startingNode: Node, config: SolutionConfig): Double {
        var count = 0

        var node = startingNode

        val errors = mutableListOf<Int>()

        while (node != startingNode || count == 0) {
            // Calculating absolute error is nice, but let's start by just trying to get any solutions
            // Maybe this will just implement anti-aliasing automatically?
            errors.add(node.value - node.header.desiredValue)
            node = node.right
            count++
        }

        return config.errorAggregator.aggregate(errors)
    }
}