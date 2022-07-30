package bricktiler.dlx

import bricktiler.board.Board
import bricktiler.board.BoardUtils
import bricktiler.board.Piece
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExactCoverSolverTest {
    @Test
    fun `solves a canonical exact cover problem`() {
        // This problem will only want a covering consisting of 1s
        val desiredSolution = List(16) { 1 }

        // Cheating a little here, but generating problem spaces is hard, so just steal from the existing brick tiling logic
        val board = Board(4, 4)
        val matrix = BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Piece(1, 2) to 1)).first

        val solutions = ExactCoverSolver.solve(matrix)

        println(matrix)

        // Given the selection of bricks and board size, we only expect a single solution.
        assertEquals(1, solutions.size, "Didn't find any solutions, but expected to find 1")
        assertThat(solutions[0].solutionRows, containsInAnyOrder(11,10,9,8,3,2,1,0))
    }

    @Test
    fun `solves an extended exact cover problem with no allowed error`() {
        // This problem will only want a covering consisting of 1s
        val desiredSolution = listOf(
                listOf(1, 1, 1, 2),
                listOf(1, 1, 1, 2),
                listOf(1, 1, 2, 1),
                listOf(1, 1, 2, 1),
        ).flatten()

        // Cheating a little here, but generating problem spaces is hard, so just steal from the existing brick tiling logic
        val board = Board(4, 4)
        val matrix = BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Piece(1, 2) to 1, Piece(1, 2) to 2)).first

        val solutions = ExactCoverSolver.solve(matrix, SolutionConfig(onlyExactMatches = true))

        // Given the selection of bricks and board size, we only expect a single solution.
        assertEquals(1, solutions.size, "Didn't find any solutions, but expected to find 1")

        println(solutions[0])

        assertThat(solutions[0].solutionRows, containsInAnyOrder(11, 22, 9, 8, 15, 2, 1, 0))
    }

    @Test
    fun `solves an extended exact cover problem with error`() {
        // This problem will only want a covering consisting of 1s
        val desiredSolution = listOf(
            listOf(1, 1, 1, 2),
            listOf(1, 1, 2, 2),
            listOf(1, 1, 2, 1),
            listOf(1, 1, 1, 1),
        ).flatten()

        // Cheating a little here, but generating problem spaces is hard, so just steal from the existing brick tiling logic
        val board = Board(4, 4)
        val matrix = BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Piece(1, 2) to 1, Piece(1, 2) to 2)).first

        val solutions = ExactCoverSolver.solve(matrix, SolutionConfig(onlyExactMatches = false))

        // Given the selection of bricks and board size, we only expect a single solution.
        assertEquals(4, solutions.size, "Didn't find any solutions, but expected to find 1")

        assertThat(solutions[0].solutionRows, containsInAnyOrder(11, 10, 9, 8, 15, 2, 1, 0))
        assertThat(solutions[1].solutionRows, containsInAnyOrder(11, 22, 9, 8, 15, 2, 1, 0))
        assertThat(solutions[2].solutionRows, containsInAnyOrder(11, 10, 9, 8, 15, 14, 1, 0))
        assertThat(solutions[3].solutionRows, containsInAnyOrder(11, 22, 9, 8, 15, 14, 1, 0))
    }

    @Test
    fun `can be configured to exit on the first solution`() {
        // This problem will only want a covering consisting of 1s
        val desiredSolution = listOf(
            listOf(1, 1, 1, 2),
            listOf(1, 1, 2, 2),
            listOf(1, 1, 2, 1),
            listOf(1, 1, 1, 1),
        ).flatten()

        // Cheating a little here, but generating problem spaces is hard, so just steal from the existing brick tiling logic
        val board = Board(4, 4)
        val matrix = BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Piece(1, 2) to 1, Piece(1, 2) to 2)).first

        val solutions = ExactCoverSolver.solve(matrix, SolutionConfig(onlyExactMatches = false, exitOnFirstSolution = true))

        // Given the selection of bricks and board size, we only expect a single solution.
        assertEquals(1, solutions.size, "Only wanted one solution")
    }

    @Test
    fun `finds no solutions when only finding exact matches and no error free solution exists`() {
        // This problem will only want a covering consisting of 1s
        val desiredSolution = listOf(
            listOf(1, 1, 1, 2),
            listOf(1, 1, 2, 2),
            listOf(1, 1, 2, 1),
            listOf(1, 1, 1, 1),
        ).flatten()

        // Cheating a little here, but generating problem spaces is hard, so just steal from the existing brick tiling logic
        val board = Board(4, 4)
        val matrix = BoardUtils.makeSparseMatrix(desiredSolution, board, listOf(Piece(1, 2) to 1, Piece(1, 2) to 2)).first

        val solutions = ExactCoverSolver.solve(matrix, SolutionConfig(onlyExactMatches = true))

        // Given the selection of bricks and board size, we only expect a single solution.
        assertEquals(0, solutions.size, "Did not expect to find any solutions")
    }
}