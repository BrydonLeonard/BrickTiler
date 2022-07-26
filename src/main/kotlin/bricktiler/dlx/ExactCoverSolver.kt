package bricktiler.dlx

import bricktiler.Solution
import kotlin.math.abs
import kotlin.random.Random

object ExactCoverSolver {
    fun solve(matrix: SparseMatrix): List<Solution> {
        val headerWithFewestOnes = matrix.shortestUncoveredColumn()

        if (headerWithFewestOnes == null) {
            return listOf(Solution())
        }

        if (headerWithFewestOnes.desiredValue == 4) {
            println("hmm")
        }

        // This is the bit where we try a bunch of rows (recursively) to see what works
        var chosenRowNode = headerWithFewestOnes.first
        var count = 0
        val solutions = mutableListOf<Solution>()

        while (chosenRowNode != null && (chosenRowNode != headerWithFewestOnes.first || count == 0)) {
            count++

            if (totalRowError(chosenRowNode) > 0) {
                chosenRowNode = chosenRowNode.down
                continue
            }

            val chosenRowIndex = chosenRowNode!!.row

            val columnsWithOnesInChosenRow = matrix.headers.filter { !it.covered && it.getNodeInRow(chosenRowIndex) != null }

            columnsWithOnesInChosenRow.forEach { it.cover() }

            val subSolutions = solve(matrix)

            columnsWithOnesInChosenRow.reversed().forEach { it.uncover() }

            solutions.addAll(
                    subSolutions.map {
                        it + chosenRowNode!!.row
                    }
            )

            chosenRowNode = chosenRowNode.down
        }

        return solutions
    }

    /**
     * Finds the total error for the whole row
     */
    private fun totalRowError(startingNode: Node): Int {
        var error = 0
        var count = 0

        var node = startingNode

        while (node != startingNode || count == 0) {
            error += abs(node.value - node.header.desiredValue)
            node = node.right
            count++
        }

        return error
    }
}