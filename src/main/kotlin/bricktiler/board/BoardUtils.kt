package bricktiler.board

import bricktiler.Solution
import bricktiler.dlx.SparseMatrix
import java.lang.Math.abs
import kotlin.math.absoluteValue

// TODO these two data classes could be combined a little more neatly
data class PiecePosition(
    val piece: Piece,
    val oneDimensionalPosition: Int,
    val value: Int
)

object BoardUtils {
    /**
     * @param includeInvalidRows If false, rows that have no columns matching the desired solution won't be added to the matrix at all.
     */
    fun makeSparseMatrix(desiredSolution: List<Int>, board: Board, pieces: List<Pair<Piece, Int>>, includeInvalidRows: Boolean = true, maxPerPieceError: Double = 0.0): Pair<SparseMatrix, List<PiecePosition>> {
        val sparseMatrix = SparseMatrix(desiredSolution)
        val piecePositions: MutableList<PiecePosition> = mutableListOf()
        var row = 0

        pieces.forEach { (piece, value) ->
            piece.getAllValidTopLeftPositions(board).forEach { topLeft ->
                val covering = piece.topLeftToPieceCovering(topLeft, board)

                if (includeInvalidRows || errorIsWithinBounds(covering, desiredSolution, value, maxPerPieceError)){
                    val piecePosition = PiecePosition(piece, topLeft, value)
                    piecePositions.add(piecePosition)
                    covering.forEach { position ->
                        sparseMatrix.add(position, row, value)
                    }
                    row++
                }
            }
        }

        if (sparseMatrix.headers.any { it.first == null}) {
            throw Exception("Impossible to generate a problem space with the given error threshold")
        }

        return sparseMatrix to piecePositions
    }

    fun errorIsWithinBounds(covering: List<Int>, desiredValues: List<Int>, value: Int, maxPerPieceError: Double): Boolean {
        val error = covering.sumOf { position -> value - desiredValues[position] }.absoluteValue
        return error <= maxPerPieceError
    }

    fun describeSolution(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
        solution.solutionRows.flatMap { row ->
            val piece = piecePositions[row]
            val pieceCovering = piece.piece.topLeftToPieceCovering(piece.oneDimensionalPosition, board)

            pieceCovering.map {
                val twoDimensionalPosition = toTwoDimensions(it, board)
                Position(twoDimensionalPosition.x, twoDimensionalPosition.y) to piecePositions[row].value
            }
        }.toMap()

    fun describeSolutionAsString(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
        solution.solutionRows.map { row ->
            val piecePosition = piecePositions[row]

            Pair(piecePosition, toTwoDimensions(piecePosition.oneDimensionalPosition, board))
        }.sortedBy { it.second.x }.sortedBy { it.second.y }.joinToString("\n") { (piecePosition, twoDimPosition) ->
            "${piecePosition.piece}, ${piecePosition.value}, $twoDimPosition"
        }


    fun toTwoDimensions(x: Int, board: Board) = Position(x % board.width, x / board.width)
}

