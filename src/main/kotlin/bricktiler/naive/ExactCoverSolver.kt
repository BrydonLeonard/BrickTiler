package bricktiler.naive

import bricktiler.Solution

object ExactCoverSolver {
    fun solve(matrix: MutableMatrix<Int>): List<Solution> {
        val columnWithFewestOnes = matrix.mapEachColumn { it.sum() }
                .mapIndexed { K, V: Int -> Pair(K, V) }
                .minByOrNull { it.second }
                ?.first
                ?: return listOf(Solution()) // If we find an empty matrix, we have a good solution!

        // This is the bit where we try a bunch of rows (recursively) to see what works
        return matrix.mapEachRow { row -> row[columnWithFewestOnes] }
                .filterAndGetIndex { it == 1 }
                .flatMap { chosenRowIndex ->
                    // Cloning the whole matrix isn't particularly efficient, but until we implement DPX, it's what we've got
                    val workingMatrix = matrix.clone()

                    // We want to see where other rows clash with this one, so figure out where this row has 1s
                    val chosenRowOnesColumns = workingMatrix.getRow(chosenRowIndex)
                            .filterAndGetIndex { it == 1 }

                    // This monstrosity just checks whether other rows have 1s in the same column as our chosen row
                    // Functional styles of programming are nice, but this just seems opaque to me (and I wrote it!)
                    workingMatrix.mapEachRow { row ->
                        chosenRowOnesColumns.any { column ->
                            row[column] == 1
                        }
                    }
                            .filterAndGetIndex { it }
                            .run {
                                // Throw away the clashes, we know they can't be included with our chosen row
                                workingMatrix.removeRows(this)
                            }

                    // Given that we've now checked these columns, we have no reason to keep them in. Throw them out too
                    workingMatrix.removeColumns(chosenRowOnesColumns)

                    // Recursion!
                    val subSolutions = solve(workingMatrix)

                    // We might get multiple possible solutions from downstream, that just means that from the chosen row
                    // here, we have multiple choice. As such, we stick the solutions together and return them back down/up
                    // the stack.
                    subSolutions.filterIsInstance<Solution>().map {
                        it + matrix.originalRows[chosenRowIndex]
                    }
                }
    }

    private fun <T> List<T>.filterAndGetIndex(predicate: (T) -> Boolean) =
            this.mapIndexed { K, V -> Pair(K, V) }
                    .filter { predicate(it.second) }
                    .map { it.first }
}