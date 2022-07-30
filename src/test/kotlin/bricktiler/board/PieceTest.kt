package bricktiler.board

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested

class PieceTest {

    val board = Board(5, 5)

    @Nested
    inner class TopLeftPositions {
        @Test
        fun `exact fit 1x1`() {
            val piece = Piece(1, 1)

            val smolBoard = Board(1, 1)

            val validPositions = piece.getAllValidTopLeftPositions(smolBoard)

            assertEquals(listOf(0), validPositions)
        }

        @Test
        fun `exact fit 1x2`() {
            val piece = Piece(1, 2)

            val smolBoard = Board(1, 2)

            val validPositions = piece.getAllValidTopLeftPositions(smolBoard)

            assertEquals(listOf(0), validPositions)
        }

        @Test
        fun `1x2`() {
            val piece = Piece(1, 2)

            val validPositions = piece.getAllValidTopLeftPositions(board)

            // On a 5x5 board, we'd expect a vertical baby piece to be able to go anywhere but the last row
            val expected = List(5 * 4) { it }

            assertEquals(expected, validPositions)
        }

        @Test
        fun `2x1`() {
            val piece = Piece(2, 1)

            val validPositions = piece.getAllValidTopLeftPositions(board)

            // On a 5x5 board, we'd expect a horizontal baby piece to be able to go anywhere but the last column
            val expected = List(5 * 5) { it }.filter { (it - 4) % 5 != 0 }

            assertEquals(expected, validPositions)
        }


        @Test
        fun `2x2`() {
            val piece = Piece(2, 2)

            val validPositions = piece.getAllValidTopLeftPositions(board)

            // Everything except the last row or column
            val expected = List(5 * 4) { it }.filter { (it - 4) % 5 != 0 }

            assertEquals(expected, validPositions)
        }

        @Test
        fun `2x3`() {
            val piece = Piece(2, 3)

            val validPositions = piece.getAllValidTopLeftPositions(board)

            // Everything except the last row or two columns
            val expected = List(5 * 3) { it }.filter { (it - 4) % 5 != 0 }

            assertEquals(expected, validPositions)
        }

        @Test
        fun `3x2`() {
            val piece = Piece(3, 2)

            val validPositions = piece.getAllValidTopLeftPositions(board)

            // Everything except the last column or two rows
            val expected = List(5 * 4) { it }.filter { ((it - 4) % 5 != 0) && ((it - 3) % 5 != 0) }

            assertEquals(expected, validPositions)
        }
    }

    @Nested
    inner class BottomRight {
        @Test
        fun `1x1`() {
            val piece = Piece(1, 1)

            val bottomRight = piece.bottomRight(Position(0, 0))

            assertEquals(Position(0, 0), bottomRight)
        }

        @Test
        fun `1x2`() {
            val piece = Piece(1, 2)


            val bottomRight = piece.bottomRight(Position(0, 0))

            assertEquals(Position(0, 1), bottomRight)
        }

        @Test
        fun `2x2`() {
            val piece = Piece(2, 2)

            val bottomRight = piece.bottomRight(Position(0, 0))

            assertEquals(Position(1, 1), bottomRight)
        }

        @Test
        fun `4x2`() {
            val piece = Piece(4, 2)

            val bottomRight = piece.bottomRight(Position(0, 0))

            assertEquals(Position(3, 1), bottomRight)
        }
    }

    @Test
    fun topLeftToPieceCoveringTest() {
        val piece = Piece(2, 4)

        val covering = piece.topLeftToPieceCovering(2, board)

        val expectedCovering = listOf(2, 3, 7, 8, 12, 13, 17, 18)

        assertEquals(covering, expectedCovering)
    }
}