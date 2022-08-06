package bricktiler.dlx

import bricktiler.Solution
import bricktiler.board.LegoSizes
import bricktiler.board.Piece
import bricktiler.board.PiecePosition
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PieceCountConstraintValidatorTest {
    @Test
    fun `adds and removes rows`() {
        val piecePositions = getPiecePositions()
        val pieceCounts = getPieceCounts()
        val validator = PieceCountConstraintValidator(
            pieceCounts,
            piecePositions
        )
        val currentSolution = Solution()

        // Hide a 1x2 with a value of 1, of which there was only 1 to begin with
        assertTrue(validator.addRowToSolution(0, currentSolution))
        // Now hide a 2x2 with a value of 2, of which there were 6
        assertTrue(validator.addRowToSolution(2, currentSolution))

        assertThat(currentSolution.solutionRows, containsInAnyOrder(0, 2))
        assertEquals(0, pieceCounts[2]!![1])
        assertEquals(5, pieceCounts[4]!![2])

        validator.removeRowFromSolution(0, currentSolution)
        validator.removeRowFromSolution(2, currentSolution)
        assertThat(currentSolution.solutionRows, `is`(empty()))
        assertEquals(1, pieceCounts[2]!![1])
        assertEquals(6, pieceCounts[4]!![2])
    }

    @Test
    fun `fails to add rows when no capacity remains for the piece`() {
        val piecePositions = getPiecePositions()
        val pieceCounts = getPieceCounts()
        val validator = PieceCountConstraintValidator(
            pieceCounts,
            piecePositions
        )
        val currentSolution = Solution()

        // Hide a 1x2 with a value of 1, of which there was only 1 to begin with
        assertTrue(validator.addRowToSolution(0, currentSolution))
        // Now hide a 2x2 with a value of 2, of which there were 6
        assertFalse(validator.addRowToSolution(3, currentSolution))

        assertThat(currentSolution.solutionRows, containsInAnyOrder(0))

        assertEquals(0, pieceCounts[2]!![1])
    }

    private fun getPiecePositions() = listOf(
            PiecePosition(
                piece = Piece(LegoSizes.ONE_BY_TWO),
                oneDimensionalPosition = 0,
                value = 1
            ),
            PiecePosition(
                piece = Piece(LegoSizes.ONE_BY_TWO),
                oneDimensionalPosition = 1,
                value = 2
            ),
            PiecePosition(
                piece = Piece(LegoSizes.TWO_BY_TWO),
                oneDimensionalPosition = 2,
                value = 2
            ),
            PiecePosition(
                piece = Piece(LegoSizes.ONE_BY_TWO),
                oneDimensionalPosition = 3,
                value = 1
            ),
        )

    private fun getPieceCounts() = mutableMapOf(
            2 to mutableMapOf(
                1 to 1,
                2 to 2,
                3 to 3,
                4 to 4,
            ),
            4 to mutableMapOf(
                1 to 5,
                2 to 6,
                3 to 7,
                4 to 8,
            )
        )
}