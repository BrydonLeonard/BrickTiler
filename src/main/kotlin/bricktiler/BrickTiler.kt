package bricktiler

import bricktiler.board.*
import bricktiler.dlx.*
import bricktiler.dlx.ExactCoverSolver.statTracker.badRowsDueToConstraints
import bricktiler.dlx.ExactCoverSolver.statTracker.badRowsDueToCovered
import bricktiler.dlx.ExactCoverSolver.statTracker.badRowsDueToError
import bricktiler.dlx.ExactCoverSolver.statTracker.deadEndDueToConstraints
import bricktiler.dlx.ExactCoverSolver.statTracker.deadEnds
import bricktiler.dlx.ExactCoverSolver.statTracker.down
import bricktiler.dlx.ExactCoverSolver.statTracker.solutions
import bricktiler.dlx.ExactCoverSolver.statTracker.up
import bricktiler.image.ImageManager
import kotlinx.coroutines.asCoroutineDispatcher
import java.io.File
import java.util.concurrent.Executors


object BrickTiler {
    @JvmStatic
    fun main(args: Array<String>) {
        val board = Board(3, 3)
        val image = ImageManager.downscaleFromFile(board.width, board.height, 4)
        val desiredSolution = image.asOneDimensionalArray().map { it + 1 }

        println("Desired image is:\n$image")

        val pieces = List(4) { value ->
            // We want values in [1, 4]
            Piece.allPiecesWithValue(value + 1)
        }.flatten()

        val piecesString = pieces.joinToString("\n") { "${it.first} (${it.second})" }
        println("Valid pieces are:\n$piecesString")

        val (sparseMatrix, piecePositions) = BoardUtils.makeSparseMatrix(desiredSolution, board, pieces, includeInvalidRows = false, maxPerPieceError = 1.0)

        println("Matrix height is ${sparseMatrix.height}")

        /*
        Maps piece size:value:count
         */
        val pieceConstraints = mutableMapOf(
            2 to mutableMapOf(
                1 to 30,
                2 to 56,
                3 to 31,
                4 to 33,
            ),
            4 to mutableMapOf(
                1 to 35,
                2 to 50,
                3 to 27,
                4 to 39,
            ),
            6 to mutableMapOf(
                1 to 22,
                2 to 32,
                3 to 29,
                4 to 27,
            ),
            8 to mutableMapOf(
                1 to 21,
                2 to 50,
                3 to 25,
                4 to 25,
            )
        )

        val contraintsValidator = PieceCountConstraintValidator(pieceConstraints, piecePositions)

        val config = SolutionConfig(
            errorChecking = ErrorChecking.NO_CHECK,
            exitOnFirstSolution = true,
            maxPerPieceError = 2,
            constraintValidator = contraintsValidator,
            maxRecursiveDepth = 2
        )

        val dispatcher = Executors
            .newFixedThreadPool(12)
            .asCoroutineDispatcher()

        File("initialState.puml").writeText(sparseMatrix.pumlCompatibleVisualisation())

        val solution = ExactCoverSolver.solveOnce(
            sparseMatrix, config, dispatcher
        )

        if (solution == null) {
            println("No solutions :(")
            println("Found ${deadEnds} dead ends")
            println("\"Found\" ${solutions} solutions")
            println("Went to ${ExactCoverSolver.statTracker.maxDepth} max depth")
            println("Visited each depth this many times:\n ${ExactCoverSolver.statTracker.depthCount}")
            println("Drops:")
            println("  Constraints: ${badRowsDueToConstraints}")
            println("  Errors: ${badRowsDueToError}")
            println("  Covered: ${badRowsDueToCovered}")
            println("Dead ends due to constraints: ${deadEndDueToConstraints}")
            println("Descended: ${down} times and ascended ${up} times")
        } else {
            println(BoardUtils.describeSolutionAsString(solution, board, piecePositions))

            val solutionImage = ImageManager.fromSolution(solution, board, piecePositions)

            val described = BoardUtils.describeSolution(solution, board, piecePositions)

            described.entries.map { it.key }

            solutionImage.show(10)
            image.show(20)
            println(solutionImage.toString())
            println(summariseSolution(solution, board, piecePositions))
        }

    }

    fun summariseSolution(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
            solution.solutionRows.map { row -> piecePositions[row] }.groupingBy { Pair(it.piece, it.value) }.eachCount()

}