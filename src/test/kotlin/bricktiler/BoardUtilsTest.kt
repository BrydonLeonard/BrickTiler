package bricktiler

import bricktiler.dlx.SparseMatrix
import bricktiler.naive.MutableMatrix
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BoardUtilsTest {
    val board = Board(3, 3)

    @Test
    fun `can generate a sparse matrix`() {
        val sparseMatrix = BoardUtils.makeSparseMatrix(List(9) { 1 }, board, listOf(Pair(Piece(1, 2), 1)))

        val expectedMatrix = mutableListOf(
                mutableListOf(1, 0, 0, 1, 0, 0, 0, 0, 0),
                mutableListOf(0, 1, 0, 0, 1, 0, 0, 0, 0),
                mutableListOf(0, 0, 1, 0, 0, 1, 0, 0, 0),
                mutableListOf(0, 0, 0, 1, 0, 0, 1, 0, 0),
                mutableListOf(0, 0, 0, 0, 1, 0, 0, 1, 0),
                mutableListOf(0, 0, 0, 0, 0, 1, 0, 0, 1)
        )

        expectedMatrix.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, value ->
                val expectValue = value == 1
                assertTrue(expectValue == (sparseMatrix.headers[columnIndex].getNodeInRow(rowIndex) != null), "Mismatch in row $rowIndex, column $columnIndex")
            }

        }
    }
}