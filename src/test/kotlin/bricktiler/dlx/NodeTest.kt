package bricktiler.dlx

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class NodeTest {
    val header = Header(1)

    @Test
    fun cover() {
        val node1 = Node(header, 0, 1)
        val node2 = Node(header, 0, 1)
        val node3 = Node(header, 0, 1)

        node1.addDown(node2)
        node2.addDown(node3)

        node2.cover()

        assertTrue(node1.down == node3)
        assertTrue(node3.up == node1)
    }

    @Test
    fun uncover() {
        val node1 = Node(header, 0, 1)
        val node2 = Node(header, 0, 1)
        val node3 = Node(header, 0, 1)

        node1.addDown(node2)
        node2.addDown(node3)

        node2.cover()
        node2.uncover()

        assertTrue(node1.down == node2)
        assertTrue(node3.up == node2)
    }
}