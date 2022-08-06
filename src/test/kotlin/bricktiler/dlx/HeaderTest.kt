package bricktiler.dlx

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HeaderTest {
    @Test
    fun `cover and uncover with a single node`() {
        val header1 = Header(0, 1)
        val header2 = Header(1, 1)
        header1.addRight(header2)
        header2.addRight(header1) // Because circles

        header1.insert(0, 5)
        header2.insert(0, 4)

        val node1 = header1.getNodeInRow(0)
        val node2 = header2.getNodeInRow(0)

        header1.coverRow(node1)

        assertEquals(null, header2.first)
        assertEquals(null, header2.last)

        header1.uncoverRow(node1)

        assertEquals(node2, header2.first)
        assertEquals(node2, header2.last)
    }

    @Test
    fun `cover and uncover with a single node 2`() {
        val header1 = Header(0, 1)
        val header2 = Header(1, 1)
        header1.addRight(header2)
        header2.addRight(header1) // Because circles

        header1.insert(0, 5)
        header2.insert(0, 4)

        val node2 = header2.getNodeInRow(0)

        header1.cover()

        assertEquals(null, header2.first)
        assertEquals(null, header2.last)

        header1.uncover()

        assertEquals(node2, header2.first)
        assertEquals(node2, header2.last)
    }

    @Test
    fun `cover and uncover with multiple nodes`() {
        val header1 = Header(0, 1)
        val header2 = Header(1, 1)
        header1.addRight(header2)
        header2.addRight(header1) // Because circles

        header1.insert(0, 5)
        header1.insert(1, 5)
        header1.insert(2, 5)
        header1.insert(3, 5)
        header2.insert(0, 4)
        header2.insert(1, 4)
        header2.insert(2, 4)
        header2.insert(3, 4)

        val h1Nodes = List(4) { header1.getNodeInRow(it) }
        val h2Nodes = List(4) { header2.getNodeInRow(it) }

        header1.coverRow(h1Nodes[0])

        assertEquals(h2Nodes[1], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header1.coverRow(h1Nodes[1])

        assertEquals(h2Nodes[2], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header1.coverRow(h1Nodes[2])

        assertEquals(h2Nodes[3], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header1.coverRow(h1Nodes[3])

        assertEquals(null, header2.first)
        assertEquals(null, header2.last)

        header1.uncoverRow(h1Nodes[3])

        assertEquals(h2Nodes[3], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header1.uncoverRow(h1Nodes[2])

        assertEquals(h2Nodes[2], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header1.uncoverRow(h1Nodes[1])

        assertEquals(h2Nodes[1], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header1.uncoverRow(h1Nodes[0])

        assertEquals(h2Nodes[0], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)
    }


    @Test
    fun `cover and uncover with multiple nodes backwards`() {
        val header1 = Header(0, 1)
        val header2 = Header(1, 1)
        val header3 = Header(2, 1)
        header1.addRight(header2)
        header2.addRight(header3) // Because circles
        header3.addRight(header1) // Because circles

        header1.insert(0, 5)
        header1.insert(1, 5)
        header2.insert(0, 4)
        header2.insert(1, 4)
        header2.insert(2, 4)
        header2.insert(3, 4)
        header3.insert(2, 6)
        header3.insert(3, 6)

        val h2Nodes = List(4) { header2.getNodeInRow(it) }

        header1.cover()
        assertEquals(h2Nodes[2], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header3.cover()
        assertEquals(null, header2.first)
        assertEquals(null, header2.last)

        header3.uncover()
        assertEquals(h2Nodes[2], header2.first)
        assertEquals(h2Nodes[3], header2.last)
        columnCycles(header2)

        header1.uncover()
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
}