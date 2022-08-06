package bricktiler

import bricktiler.board.*
import bricktiler.dlx.*
import bricktiler.image.ImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger


object BrickTiler {
    @JvmStatic
    fun main(args: Array<String>) {
        val board = Board(32, 32)
        val image = ImageManager.downscaleFromFile(board.width, board.height, 4)
        val desiredSolution = image.asOneDimensionalArray().map { it + 1 }

        println("Desired image is:\n$image")

        val pieces = List(4) { value ->
            // We want values in [1, 4]
            Piece.allPiecesWithValue(value + 1)
        }.flatten().sortedByDescending { it.first.size }

        val piecesString = pieces.joinToString("\n") { "${it.first} (${it.second})" }
        println("Valid pieces are:\n$piecesString")

        val (sparseMatrix, piecePositions) = BoardUtils.makeSparseMatrix(desiredSolution, board, pieces, includeInvalidRows = false)

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
            errorChecking = ErrorChecking.ALLOW_ERRORS,
            exitOnFirstSolution = true,
            maxPerPieceError = 2,
            constraintValidator = contraintsValidator,
            maxRecursiveDepth = 4
        )

        val dispatcher = Executors
            .newFixedThreadPool(12)
            .asCoroutineDispatcher()

        val solutions = runBlocking(dispatcher) {
            ExactCoverSolver.solve(
                sparseMatrix, config
            ).toSet().toList()
        }

        if (solutions.isEmpty()) {
            println("No solutions :(")
        } else {
            println("Found ${solutions.size} solutions")

            val randomSolution = solutions.first()

            println(BoardUtils.describeSolutionAsString(randomSolution, board, piecePositions))

            val solutionImage = ImageManager.fromSolution(randomSolution, board, piecePositions)

            val described = BoardUtils.describeSolution(randomSolution, board, piecePositions)

            described.entries.map { it.key }

            solutionImage.show(10)
            println(solutionImage.toString())
            println(summariseSolution(randomSolution, board, piecePositions))
        }
    }

    fun summariseSolution(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
            solution.solutionRows.map { row -> piecePositions[row] }.groupingBy { Pair(it.piece, it.value) }.eachCount()

}