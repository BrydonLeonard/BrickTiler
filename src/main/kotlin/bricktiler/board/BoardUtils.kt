package bricktiler.board

import bricktiler.Solution
import bricktiler.dlx.SparseMatrix

data class PiecePosition(
    val piece: Piece,
    val oneDimensionalPosition: Int,
    val value: Int
)

object BoardUtils {
    /**
     * @param includeInvalidRows If false, rows that have no columns matching the desired solution won't be added to the matrix at all.
     */
    fun makeSparseMatrix(desiredSolution: List<Int>, board: Board, pieces: List<Pair<Piece, Int>>, includeInvalidRows: Boolean = true): Pair<SparseMatrix, List<PiecePosition>> {
        val sparseMatrix = SparseMatrix(desiredSolution)
        val piecePositions: MutableList<PiecePosition> = mutableListOf()
        var row = 0

        pieces.forEach { (piece, value) ->
            piece.getAllValidTopLeftPositions(board).forEach { topLeft ->
                val covering = piece.topLeftToPieceCovering(topLeft, board)

                if (includeInvalidRows || covering.any { position -> desiredSolution[position] == value }){
                    piecePositions.add(PiecePosition(piece, topLeft, value))
                    covering.forEach { position ->
                        sparseMatrix.add(position, row, value)
                    }
                    row++
                }
            }
        }

        return sparseMatrix to piecePositions
    }

    fun describeSolution(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
        solution.solutionRows.joinToString("\n") { row ->
            val piecePosition = piecePositions[row]

            val twoDimensionalPosition = toTwoDimensions(piecePosition.oneDimensionalPosition, board)

            "size=${piecePosition.piece}, colour=${piecePosition.value}, position=$twoDimensionalPosition"
        }


    fun toTwoDimensions(x: Int, board: Board) = Position(x % board.width, x / board.width)
}

