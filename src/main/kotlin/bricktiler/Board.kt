package bricktiler

import bricktiler.dlx.SparseMatrix

data class Board(val width: Int, val height: Int)

data class Position(val x: Int, val y: Int)

object BoardUtils {
    fun makeSparseMatrix(desiredSolution: List<Int>, board: Board, pieces: List<Pair<Piece, Int>>): SparseMatrix {
        val sparseMatrix = SparseMatrix(desiredSolution)
        var row = 0

        pieces.forEach { (piece, value) ->
            piece.getAllValidTopLeftPositions(board).forEach { topLeft ->
                piece.topLeftToPieceCovering(topLeft, board).forEach { topLeft ->
                    sparseMatrix.add(topLeft, row, value)
                }
                row++
            }
        }

        return sparseMatrix
    }
}

class Piece(val width: Int, val height: Int) {
    fun getAllValidTopLeftPositions(board: Board): List<Int> {
        val oneDimensionalWidth = board.width * board.height

        // "Additional" here is in addition to the 1x1 square already taken up by the top left corner.
        val additionalWidth = width - 1
        val additionalHeight = height - 1

        return List(oneDimensionalWidth) { topLeft ->
            val topLeft2D = toTwoDimensions(topLeft, board)
            val bottomRight = bottomRight(topLeft2D)
            // There's some nice maths we could do to not convert to 2D, but it's really unreadable. In the interest of
            // me remembering how this works in 3 minutes from now, I'm sticking with the conversion.
            when {
                bottomRight.y >= board.height -> null
                bottomRight.x >= board.width -> null
                else -> topLeft
            }
        }.filterNotNull()
    }

    private fun toTwoDimensions(x: Int, board: Board) = Position(x % board.width, x / board.height)
    private fun bottomRight(topLeft: Position) = Position(topLeft.x + width - 1, topLeft.y + height - 1)

    fun topLeftToPieceCovering(topLeft: Int, board: Board): List<Int> = List(this.height) { verticalOffset ->
            List(this.width) { horizontalOffset ->
                topLeft + verticalOffset * board.width + horizontalOffset
            }
        }.flatten()
}