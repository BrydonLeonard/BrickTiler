package bricktiler.board

import bricktiler.board.BoardUtils.toTwoDimensions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BoardUtilsTest {
    @Test
    fun `can generate a small matrix`() {
        val board = Board(1, 2)
        val sparseMatrix = BoardUtils.makeSparseMatrix(List(2) { 1 }, board, listOf(Pair(Piece(1, 2), 1))).first

        val expectedMatrix = listOf(
                listOf(1, 1)
        )

        expectedMatrix.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, value ->
                val expectValue = value == 1
                assertTrue(expectValue == (sparseMatrix.headers[columnIndex].getNodeInRow(rowIndex) != null), "Mismatch in row $rowIndex, column $columnIndex")
            }
        }
    }

    @Test
    fun `can generate a sparse matrix`() {
        val board = Board(4, 4)
        val sparseMatrix = BoardUtils.makeSparseMatrix(List(16) { 1 }, board, listOf(Pair(Piece(1, 2), 1), Pair(Piece(2, 1), 1))).first

        val expectedMatrix = listOf(
                listOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1),
                listOf(1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0),
                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        )

        expectedMatrix.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, value ->
                val expectValue = value == 1
                assertTrue(expectValue == (sparseMatrix.headers[columnIndex].getNodeInRow(rowIndex) != null), "Mismatch in row $rowIndex, column $columnIndex")
            }
        }
    }

    @Test
    fun `throws an exception if a problem space can't be generated`() {
        val board = Board(3, 3)
        val desiredSolution = listOf(
            1, 2, 1,
            1, 2, 1,
            2, 1, 2
        )
        assertThrows(Exception::class.java) {
            BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Pair(Piece(1, 2), 1)), false).first
        }
    }

    @Test
    fun `piece positions are correct in generated matrices`() {
        val board = Board(3, 3)
        val desiredSolution = List(9) { 1 }
        val piecePositions = BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Pair(Piece(1, 2), 1)), false).second

        assertThat(
            piecePositions,
            containsInAnyOrder(
                PiecePosition(Piece(1, 2), 0, 1),
                PiecePosition(Piece(1, 2), 1, 1),
                PiecePosition(Piece(1, 2), 2, 1),
                PiecePosition(Piece(1, 2), 3, 1),
                PiecePosition(Piece(1, 2), 4, 1),
                PiecePosition(Piece(1, 2), 5, 1),
            )
        )

    }

    @Test
    fun `includes the correct pieces when accounting for error thresholds`() {
        val board = Board(3, 3)
        val desiredSolution = listOf(
            1, 1, 2,
            1, 2, 1,
            2, 2, 2)
        val piecePositions = BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Pair(Piece(1, 2), 1), Pair(Piece(2, 1), 1), Pair(Piece(1, 2), 2)), false, 1).second

        assertThat(
            piecePositions,
            containsInAnyOrder(
                PiecePosition(Piece(1, 2), 0, 1),
                PiecePosition(Piece(1, 2), 1, 1),
                PiecePosition(Piece(1, 2), 2, 1),
                PiecePosition(Piece(1, 2), 3, 1),
                PiecePosition(Piece(1, 2), 5, 1),
                PiecePosition(Piece(2, 1), 0, 1),
                PiecePosition(Piece(2, 1), 1, 1),
                PiecePosition(Piece(2, 1), 3, 1),
                PiecePosition(Piece(2, 1), 4, 1),

                PiecePosition(Piece(1, 2), 1, 2),
                PiecePosition(Piece(1, 2), 2, 2),
                PiecePosition(Piece(1, 2), 3, 2),
                PiecePosition(Piece(1, 2), 4, 2),
                PiecePosition(Piece(1, 2), 5, 2),
            )
        )
    }

    @Test
    fun `1D to 2D conversion works`() {
        var allGoodMappings = true
        List(50) { width ->
            List(50) { height ->
                allGoodMappings = allGoodMappings && dimensionConversionsCorrect(width, height)
            }
        }

        if (!allGoodMappings) {
            throw AssertionError("Got bad mappings")
        }
    }

    private fun dimensionConversionsCorrect(width: Int, height: Int): Boolean {
        // Yeah, nice and easy to read
        val badPositions = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
        repeat(width) { x ->
            repeat(height) { y ->
                val board = Board(width, height)
                val oneDimensional = x + y * width
                val roundTripped = toTwoDimensions(oneDimensional, board)

                if (Pair(x, y) != Pair(roundTripped.x, roundTripped.y)) {
                    badPositions.add(Pair(x, y) to Pair(roundTripped.x, roundTripped.y))
                }
            }
        }

        if (badPositions.isNotEmpty()) {
            val badPositionString = badPositions.joinToString("\n") {
                "wanted: ${it.first}, got: ${it.second} for x = ${it.first.first + it.first.second * width}"
            }

            println("Got bad mappings at board size ${width}x${height}:\n${badPositionString}")


            return false
        }
        return true
    }
}