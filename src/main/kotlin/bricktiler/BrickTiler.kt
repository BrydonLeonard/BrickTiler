package bricktiler

import bricktiler.dlx.SparseMatrix
import bricktiler.dlx.ExactCoverSolver
import bricktiler.naive.MutableMatrix
import bricktiler.naive.ExactCoverSolver as NaiveSolver


object BrickTiler {
    @JvmStatic
    fun main(args: Array<String>) {
        val matrix = mutableListOf<MutableList<Int>>(
                mutableListOf(1, 0, 0, 1, 0, 0, 1),
                mutableListOf(1, 0, 0, 3, 0, 0, 0),
                mutableListOf(0, 0, 0, 1, 1, 0, 1),
                mutableListOf(0, 0, 3, 0, 1, 2, 0),
                mutableListOf(0, 1, 1, 0, 0, 1, 1),
                mutableListOf(0, 2, 0, 0, 0, 0, 4),
                mutableListOf(0, 2, 0, 0, 0, 0, 2),
        )

        val mutableMatrix = MutableMatrix<Int>(matrix)

        val desiredSolution = listOf(
                1, 2, 3, 3, 1, 2, 2
        )

        val sparseMatrix = SparseMatrix(desiredSolution)
        matrix.forEachIndexed { y: Int, row: MutableList<Int> ->
            row.forEachIndexed { x: Int, value: Int ->
                if (value > 0) {
                    sparseMatrix.add(x, y, value)
                }
            }
        }


        println("Seeking $desiredSolution in:")
        println(sparseMatrix)

        val solutions = ExactCoverSolver.solve(sparseMatrix)

        if (solutions.isEmpty()) {
            println("No solutions :(")
        }

        solutions.forEach {
            println(sparseMatrix.rowsToString(it.solutionRows.sorted()))
            println()
        }

        val naiveSolutions = NaiveSolver.solve(mutableMatrix)
        naiveSolutions.forEach {
           // println(it)
        }

    }

}