package bricktiler.dlx

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SparseMatrixTest {
    @Test
    fun `initializes the matrix correctly`() {
        val matrix = SparseMatrix(5)

        matrix.headers.forEachIndexed { index, header ->
            assertTrue(!header.covered)
            assertTrue(header.count == 0)

            if (index == 0) {
                assertTrue(header.left == matrix.headers.last())
                assertTrue(matrix.headers.last().right == header)
            } else {
                assertTrue(header.left == matrix.headers[index - 1])
                assertTrue(matrix.headers[index - 1].right == header)
            }

            if (index == matrix.headers.count() - 1) {
                assertTrue(header.right == matrix.headers.first())
                assertTrue(matrix.headers.first().left == header)
            } else {
                assertTrue(header.right == matrix.headers[index + 1])
                assertTrue(matrix.headers[index + 1].left == header)
            }
        }
    }

    @Test
    fun `inserts elements correctly`() {
        val matrix = buildMatrix()

        // assertTrue
        val headers = matrix.headers

        assertTrue(headers[0].count == 3)
        assertTrue(headers[1].count == 1)
        assertTrue(headers[2].count == 1)
        assertTrue(headers[3].count == 2)
        assertTrue(headers[4].count == 3)

        // Check the corners' values
        assertTrue(headers[0].first!!.value == 1)
        assertTrue(headers[0].first!!.down.down.value == 2)
        assertTrue(headers[4].first!!.value == 4)
        assertTrue(headers[4].first!!.down.down.value == 3)

        // Check that the corners wrap around
        headers[0].first!!.down.down shouldBeAbove headers[0].first!!
        headers[4].first!!.down.down shouldBeAbove headers[4].first!!
        headers[4].first!! shouldBeLeftOf headers[0].first!!
        headers[4].first!!.down.down shouldBeLeftOf headers[0].first!!.down.down

        // Check that full row
        headers[0].first!!.down shouldBeLeftOf
                headers[1].first!! shouldBeLeftOf
                headers[2].first!! shouldBeLeftOf
                headers[3].first!! shouldBeLeftOf
                headers[4].first!!.down

        assertTrue(headers[3].first!!.down.value == 5)
        headers[3].first!!.down shouldBeLeftOf headers[3].first!!.down // Should be left of itself
    }

    fun `covers correctly`() {
        val matrix = buildMatrix()

        val headers = matrix.headers

        // Just some setup for reusable assertions. Could do this outside the test, but I don't want to!
        val verifyOne = {
            // The header should have been covered
            assertTrue(headers[2].right == headers[4])
            assertTrue(headers[4].left == headers[2])

            // All rows with values in the fourth column should have been covered
            headers[0].first!! shouldBeAbove headers[0].last!! // No more middle node
            headers[4].first!! shouldBeAbove headers[4].last!!
            assertTrue(headers[0].count == 2)
            assertTrue(headers[1].count == 0)
            assertTrue(headers[2].count == 0)
            assertTrue(headers[3].count == 2) // Unchanged since it was the covered column
            assertTrue(headers[4].count == 2)

            assertTrue(headers[3].covered)
        }

        // Cover 1
        headers[3].cover()

        verifyOne()

        // Cover ANOTHER!
        headers[0].cover()

        assertTrue(headers[4].count == 0)

        assertTrue(headers[1].first == null)
        assertTrue(headers[1].last == null)
        assertTrue(headers[1].count == 0)
        assertTrue(headers[2].first == null)
        assertTrue(headers[2].last == null)
        assertTrue(headers[2].count == 0)
        assertTrue(headers[4].first == null)
        assertTrue(headers[4].last == null)
        assertTrue(headers[4].count == 0)

        // We're very sorry for the inconvenience and would like to replace the column at no extra cost
        headers[0].uncover()

        verifyOne()

        headers[3].uncover()

        repeat(5) {
            assertTrue(!headers[it].covered)
        }

        assertTrue(headers[0].count == 3)
        assertTrue(headers[1].count == 1)
        assertTrue(headers[2].count == 1)
        assertTrue(headers[3].count == 2)
        assertTrue(headers[4].count == 3)
    }

    private fun buildMatrix(): SparseMatrix {
        /*
        Builds a matrix that looks like:
            1 0 0 0 4
            0 0 0 0 0
            6 6 6 6 6
            0 0 0 0 0
            2 0 0 0 3
            ... 0s until row 100
            0 0 0 5 0
         */
        val matrix = SparseMatrix(5)

        // Corners. This will cover wrapping and adding in the same row/column as another element
        matrix.add(0,0, 1)
        matrix.add(0, 4, 2)
        matrix.add(4, 4, 3)
        matrix.add(4, 0, 4)

        // Let's try adding waaaaay down. This should be fine
        matrix.add(3, 100, 5)

        // How about a full row?
        repeat(5) {
            matrix.add(it, 2, 6)
        }

        return matrix
    }

    private infix fun Node.shouldBeAbove(other: Node): Node {
        assertTrue(this.down == other)
        assertTrue(other.up == this)
        return other
    }

    private infix fun Node.shouldBeLeftOf(other: Node): Node {
        assertTrue(this.right == other)
        assertTrue(other.left == this)
        return other
    }
}