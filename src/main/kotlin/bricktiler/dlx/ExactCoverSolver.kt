package bricktiler.dlx

import bricktiler.Solution
import kotlin.math.abs
import kotlin.random.Random

/**
 * @param onlyExactMatches If true, no error is allowed. I false, at most [maxPerPieceError] error is allowed per piece
 */
public data class SolutionConfig(
        val onlyExactMatches: Boolean = false,
        val maxPerPieceError: Int = 1,
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
        val headerWithFewestOnes = matrix.shortestUncoveredColumn ?: return listOf(Solution())

        // This is the bit where we try a bunch of rows (recursively) to see what works
        var chosenRowNode = headerWithFewestOnes.first
        var count = 0
        val solutions = mutableListOf<Solution>()

        while (chosenRowNode != null && (chosenRowNode != headerWithFewestOnes.first || count == 0)) {

            val error = calculateError(chosenRowNode, config)

            if ((config.onlyExactMatches && error > 0) || (!config.onlyExactMatches && error > 1)) {
                chosenRowNode = chosenRowNode.down
                continue
            }

            val chosenRowIndex = chosenRowNode.row

            val columnsWithOnesInChosenRow = matrix.headers.filter { !it.covered && it.getNodeInRow(chosenRowIndex) != null }

            columnsWithOnesInChosenRow.forEach { it.cover() }

            val subSolutions = solve(matrix, config)

            columnsWithOnesInChosenRow.reversed().forEach { it.uncover() }

            solutions.addAll(
                    subSolutions.map {
                        it + chosenRowNode!!.row
                    }
            )

            if (solutions.size > 0 && config.exitOnFirstSolution) {
                return solutions
            }

            chosenRowNode = chosenRowNode.down
            count++
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