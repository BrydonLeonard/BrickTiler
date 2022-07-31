package bricktiler

import bricktiler.board.Board
import bricktiler.board.BoardUtils
import bricktiler.board.Piece
import bricktiler.dlx.ErrorAggregator
import bricktiler.dlx.ExactCoverSolver
import bricktiler.dlx.SolutionConfig
import bricktiler.image.ImageManager


object BrickTiler {
    @JvmStatic
    fun main(args: Array<String>) {
        val board = Board(20, 20)
        val image = ImageManager.downscaleFromFile(board.width, board.height, 4)
        val desiredSolution = image.asOneDimensionalArray().map { it + 1 }

        println("Desired image is:\n$image")

        val pieces = List(4) { value ->
            // We want values in [1, 4]
            Piece.allPiecesWithValue(value + 1)
        }.flatten().sortedByDescending { it.first.size }

        val piecesString = pieces.joinToString("\n") { "${it.first} (${it.second})"}
        println("Valid pieces are:\n$piecesString")

        val (sparseMatrix, piecePositions) = BoardUtils.makeSparseMatrix(desiredSolution, board, pieces, false)

        println("Matrix height is ${sparseMatrix.height}")

        val solutions = ExactCoverSolver.solve(sparseMatrix, SolutionConfig(onlyExactMatches = true, exitOnFirstSolution = true, errorAggregator = ErrorAggregator.ABS_AVERAGE, maxPerPieceError = 0.1)).toSet().toList()

        if (solutions.isEmpty()) {
            println("No solutions :(")
        } else {
            println("Found ${solutions.size} solutions")

            val randomSolution = solutions.random()

            println(BoardUtils.describeSolutionAsString(randomSolution, board, piecePositions))

            val solutionImage = ImageManager.fromSolution(randomSolution, board, piecePositions)

            solutionImage.show(10)
            println(solutionImage.toString())
        }
    }

}