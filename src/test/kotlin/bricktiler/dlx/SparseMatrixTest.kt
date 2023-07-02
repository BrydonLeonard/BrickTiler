package bricktiler.dlx

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.random.Random
/*
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

        assertEquals(5, matrix.width)
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

        println(matrix)

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

    @Test
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

        headers.filter { !it.covered }.forEach  {
            assertTrue(columnCycles(it), "All columns should cycle")
        }

        verifyOne()

        // Cover ANOTHER!
        headers[0].cover()

        headers.filter { !it.covered }.forEach  {
            assertTrue(columnCycles(it), "All columns should cycle")
        }

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

        headers.filter { !it.covered }.forEach  {
            assertTrue(columnCycles(it), "All columns should cycle")
        }

        verifyOne()

        headers[3].uncover()

        headers.filter { !it.covered }.forEach  {
            assertTrue(columnCycles(it), "All columns should cycle")
        }

        repeat(5) {
            assertTrue(!headers[it].covered)
        }

        assertTrue(headers[0].count == 3)
        assertTrue(headers[1].count == 1)
        assertTrue(headers[2].count == 1)
        assertTrue(headers[3].count == 2)
        assertTrue(headers[4].count == 3)
    }

    @Test()
    fun `lots of covering and uncovering to make sure it all works`() {
        val matrix = SparseMatrix(200)

        val claimedCells = mutableSetOf<Pair<Int, Int>>()

        repeat(100) {
            val position = Pair(Random.nextInt(200), Random.nextInt(100))
            if (!claimedCells.contains(position)) {
                matrix.add(position.first, position.second, 1)
                claimedCells.add(position)
            }
        }

        var latest = 0

        repeat(500) {
            when {
                latest == 0 -> matrix.headers[latest++].cover()
                latest < 50 -> if (Random.nextInt(100) < 60) matrix.headers[latest++].cover() else matrix.headers[latest--].uncover()
                else -> if (Random.nextInt(100) < 50) matrix.headers[latest++].cover() else matrix.headers[latest--].uncover()
            }


            matrix.headers.filter { !it.covered }.forEach  {
                assertTrue(columnCycles(it), "All columns should cycle")
            }
        }

        while (latest >= 0) {
            matrix.headers[latest--].uncover()
        }
    }

    @Nested
    inner class ShortestUncoveredColumn {
        @Test
        fun `works in an uncovered matrix`() {
            val matrix = SparseMatrix(3)
            /*
            1 0 0
            1 1 0
            1 1 0
            0 1 1
            0 0 1
             */
            matrix.add(0,0, 1)
            matrix.add(0, 2, 1)
            matrix.add(1, 1, 1)
            matrix.add(1, 2, 1)
            matrix.add(1, 3, 1)
            matrix.add(2, 3, 1)

            val shortestUncoveredColumn = matrix.shortestUncoveredColumn()

            assertEquals(2, shortestUncoveredColumn!!.column)
        }

        @Test
        fun `works in matrix with the previously shortest row covered`() {
            val matrix = SparseMatrix(3)
            /*
            1 0 0
            0 1 0
            1 1 0
            0 1 1
            0 0 1
             */
            matrix.add(0,0, 1)
            matrix.add(0, 2, 1)
            matrix.add(1, 1, 1)
            matrix.add(1, 2, 1)
            matrix.add(1, 3, 1)
            matrix.add(2, 3, 1)

            matrix.headers[2].cover()

            val shortestUncoveredColumn = matrix.shortestUncoveredColumn()

            // Both uncovered columns will now have 2 non-zero values. The  first should be chosen
            assertEquals(0, shortestUncoveredColumn!!.column)
        }

        @Test
        fun `if a column with no non zero value exists, it is returned`() {
            val matrix = SparseMatrix(4)
            /*
            1 0 0 0
            0 1 0 0
            0 1 0 0
            0 0 1 1
            0 0 1 1
             */
            matrix.add(0,0, 1)
            matrix.add(1, 1, 1)
            matrix.add(1, 2, 1)
            matrix.add(2, 3, 1)
            matrix.add(2, 4, 1)
            matrix.add(3, 3, 1)
            matrix.add(3, 4, 1)

            matrix.headers[3].cover()

            val shortestUncoveredColumn = matrix.shortestUncoveredColumn()

            matrix.headers.forEach {
                println("${it.column}: ${it.count}")
            }

            // The third column has more non-zero values, but they're all covered.
            assertEquals(2, shortestUncoveredColumn!!.column)
        }
    }

    @Nested
    inner class Clone {
        @Test
        fun `clone works for an uncovered matrix`() {
            val matrix = buildMatrix()

            val clone = matrix.cloneUncovered()

            matrix.headers.forEachIndexed { index, header ->
                val cloneHeader = clone.headers[index]
                assertHeaderEquality(header, cloneHeader)
            }
        }

        @Test
        fun `clone works for a partially covered matrix`() {
            val matrix = SparseMatrix(3)

            // Corners. This will cover wrapping and adding in the same row/column as another element
            matrix.add(0,0, 1)
            matrix.add(0, 2, 2)
            matrix.add(1, 1, 2)
            matrix.add(1, 2, 2)
            matrix.add(2, 3, 2)
            matrix.add(2, 4, 2)
            matrix.coverColumn(matrix.headers[0])

            val clone = matrix.cloneUncovered()

            matrix.headers.filter { !it.covered }.forEachIndexed { index, header ->
                val cloneHeader = clone.headers[index]
                assertHeaderEquality(header, cloneHeader)
            }
        }

        @Test
        fun `clone works for a fully covered matrix`() {
            val matrix = SparseMatrix(3)

            /*
            1 0 0
            0 2 0
            2 2 0
            0 0 2
            0 0 2
             */
            matrix.add(0,0, 1)
            matrix.add(0, 2, 2)
            matrix.add(1, 1, 3)
            matrix.add(1, 2, 4)
            matrix.add(2, 3, 5)
            matrix.add(2, 4, 6)
            matrix.coverColumn(matrix.headers[0])
            matrix.coverColumn(matrix.headers[1])
            matrix.coverColumn(matrix.headers[2])

            val clone = matrix.cloneUncovered()

            assertEquals(0, clone.headers.size)
            assertThat(clone.uncoveredHeaders(), hasSize(0))
        }

        private fun assertHeaderEquality(header: Header, cloneHeader: Header) {
            assertEquals(header.count, cloneHeader.count)

            assertEquals(header.first!!.row, cloneHeader.first!!.row)
            assertEquals(header.first!!.value, cloneHeader.first!!.value)
            assertEquals(header.last!!.row, cloneHeader.last!!.row)
            assertEquals(header.last!!.value, cloneHeader.last!!.value)

            var iterNode = header.first
            var cloneIterNode = cloneHeader.first


            while (iterNode != header.last) {
                assertEquals(iterNode!!.value, cloneIterNode!!.value)
                assertEquals(iterNode!!.row, cloneIterNode!!.row)

                iterNode = iterNode!!.down
                cloneIterNode = cloneIterNode!!.down


            }
        }
    }

    fun buildMatrix(): SparseMatrix {
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

    private fun columnCycles(header: Header): Boolean {
        if (header.first == null) {
            return true
        }
        var node = header.first!!.down

        val visited = mutableSetOf<Node>()

        while (node != header.first!!) {
            node = node.down
            if (visited.contains(node)) {
                return false
            }
            visited.add(node)
        }

        node = header.last!!.up

        visited.clear()

        while (node != header.last!!) {
            node = node.up
            if (visited.contains(node)) {
                return false
            }
            visited.add(node)
        }

        return true
    }
}*/