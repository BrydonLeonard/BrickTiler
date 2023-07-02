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
        val board = Board(32, 32)
        val image = ImageManager.downscaleFromFile(board.width, board.height, 4, imagePath = "C:/Users/user-pc/Desktop/poo2.png")
        val desiredSolution = image.asOneDimensionalArray().map { when(it) {
            1 -> 2
            2 -> 1
            else -> it
        } }

        println("Desired image is:\n$image")

        val pieces = List(4) { value ->
            // We want values in [0, 3]
            Piece.allPiecesWithValue(value)
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
                0 to 30,
                1 to 56,
                2 to 31,
                3 to 33,
            ),
            4 to mutableMapOf(
                0 to 35,
                1 to 50,
                2 to 27,
                3 to 39,
            ),
            6 to mutableMapOf(
                0 to 22,
                1 to 32,
                2 to 29,
                3 to 27,
            ),
            8 to mutableMapOf(
                0 to 21,
                1 to 50,
                2 to 25,
                3 to 25,
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
            sparseMatrix, config, dispatcher) {
            solution -> BoardUtils.describeSolutionError(solution, board, piecePositions, desiredSolution, BoardUtils.SolutionErrorTypes.TOTAL_DIFF_CENTRE_WEIGHTED)
        }

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
            println(solutionImage.toString())
            println(summariseSolution(solution, board, piecePositions))
            image.show(10)
        }

    }

    fun summariseSolution(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
            solution.solutionRows.map { row -> piecePositions[row] }.groupingBy { Pair(it.piece, it.value) }.eachCount()

}