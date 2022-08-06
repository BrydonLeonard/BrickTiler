package bricktiler.board

import bricktiler.board.BoardUtils.toTwoDimensions

data class Board(val width: Int, val height: Int)

data class Position(val x: Int, val y: Int)

data class Piece(val width: Int, val height: Int) {
    constructor(legoSize: LegoSizes): this(legoSize.width, legoSize.height)

    val size = width * height

    fun getAllValidTopLeftPositions(board: Board): List<Int> {
        val oneDimensionalWidth = board.width * board.height

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

    fun bottomRight(topLeft: Position) = Position(topLeft.x + width - 1, topLeft.y + height - 1)

    override fun toString() = "${width}x$height"

    fun topLeftToPieceCovering(topLeft: Int, board: Board): List<Int> = List(this.height) { verticalOffset ->
            List(this.width) { horizontalOffset ->
                topLeft + verticalOffset * board.width + horizontalOffset
            }
        }.flatten()

    companion object {
        fun allPiecesWithValue(value: Int): List<Pair<Piece, Int>> = LegoSizes.values().map { Piece(it.width, it.height) to value }
    }
}

enum class LegoSizes(val width: Int, val height: Int) {
    TWO_BY_ONE(2, 1),
    ONE_BY_TWO(1, 2),
    TWO_BY_TWO(2, 2),
    TWO_BY_THREE(2, 3),
    THREE_BY_TWO(3, 2),
    TWO_BY_FOUR(2, 4),
    FOUR_BY_TWO(4, 2);
}