package bricktiler.dlx

import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

// Keep a ref back to the matrix for convenience sake while debugging
class Header(val column: Int, val matrix: SparseMatrix, val desiredValue: Int = 1) {
    var first: Node? = null
        private set
    var last: Node? = null
        private set

    var covered: Boolean = false
        private set

    var left: Header = this
    var right: Header = this

    var count: Int = 0
        private set

    val id = UUID.randomUUID().toString()

    /**
     * Mapping of row index: node
     * TODO: Should be private
     */
    val nodes = HashMap<Int, Node>()

    /**
     * Covers this column and all rows in which it has a value
     */
    fun cover(id: String) {
        covered = true
        this.left.right = this.right
        this.right.left = this.left

        // Moves down the column to cover rows with 1s
        var rowNode = first
        var count = 0

        while ((rowNode != first || count < 1) && rowNode != null) {
            count++
            if (count % 100 == 0) {
                println("We've hit $count!")
            }
            coverRow(rowNode, id)
            rowNode = rowNode.down
        }
    }

    /**
     * The exact opposite of [cover] (including the order in which uncovering happens)
     */
    fun uncover(id: String) {
        covered = false
        this.right.left = this
        this.left.right = this

        var rowNode = last?.up

        // TODO sometimes this runs infinitely
        while (rowNode != last && rowNode != null) {
            if (rowNode.covered) {
                println("Node is covered somehow")
            }
            uncoverRow(rowNode, id)
            rowNode = rowNode.up
        }

        uncoverRow(rowNode, id)
    }

    /**
     * Return the node in a given row in this column. Null if no value is present in the column.
     *
     * Once upon a time, this followed points, but that takes too long. Use the HashMap instead.
     */
    fun getNodeInRow(row: Int): Node? = nodes[row]?.let {
            if (it.covered) null else it
        }


    operator fun get(row: Int): Node? = getNodeInRow(row)

    fun addRight(header: Header) {
        this.right = header
        header.left = this
    }

    fun addLeft(header: Header) {
        this.left = header
        header.right = this
    }

    fun insert(row: Int, value: Int) {
        count++
        val newNode = Node(this, row, value)
        nodes[row] = newNode
        insertIntoColumn(newNode)
        insertIntoRow(newNode)
    }

    /**
     * Will cover all nodes in the row _except_ the one passed in as an argument
     */
    fun coverRow(startingNode: Node?, id: String) {
        startingNode ?: return

        var node = startingNode.right

        while (node != startingNode) {
            node.cover(id)

            node.header.count--

            // All this nonsense because I didn't want special header nodes
            if (node.down == node) {
                node.header.first = null
                node.header.last = null
            } else {
                if (node.header.first == node) {
                    node.header.first = node.down
                }

                if (node.header.last == node) {
                    node.header.last = node.up
                }
            }

            var rowNode = node.header.first
            var loopCount = 0

            while ((rowNode != node.header.first || loopCount < 1) && rowNode != null) {
                loopCount++
                if (loopCount % 100 == 0 && loopCount > 0) {
                    println("We've hit $loopCount!")
                }
                rowNode = rowNode.down
            }

            node = node.right
        }
    }

    /**
     * Will uncover all nodes in the given row _except_ the one provided as an arugment
     */
    fun uncoverRow(startingNode: Node?, id: String) {
        startingNode ?: return

        if (startingNode.header != this) {
            println("THis is a little weird (un)")
        }

        var node = startingNode.left

        while (node != startingNode) {
            node.uncover(id)
            node.header.count++

            // Null [first] means that there's nothing else uncovered currently. The uncovering node will become first and last
            if (node.header.first == null) {
                node.header.first = node
                node.header.last = node
            } else {
                // Sing and dance to check whether this node should be first or last
                if (node.down == node.header.first && node.down.row > node.row) {
                    node.header.first = node
                }

                if (node.up == node.header.last && node.up.row < node.row) {
                    node.header.last = node
                }
            }
            var rowNode = node.header.first
            var loopCount = 0

            val previouslySeen = HashSet<Node>()
            var foundLoop = false

            while ((rowNode != node.header.first || loopCount < 1) && rowNode != null && node.down != node) {
                if (!previouslySeen.add(rowNode) && !foundLoop) {
                    println("Loop is at ${rowNode.row}")
                    println("Dumping visualisation")

                    File("pumlVis.puml").writeText(matrix.pumlCompatibleVisualisation(node.header.column))
                    foundLoop = true
                }
                loopCount++
                if (loopCount % 100 == 0 && loopCount > 0) {
                    println("We've hit $loopCount!")
                }
                rowNode = rowNode.down
            }

            node = node.left
        }
    }

    /**
     * Finds the first element in the column in a row higher than the new node and inserts the node before it. If no such
     * no exists, inserts the node at the end.
     */
    private fun insertIntoColumn(newNode: Node) {
        var node = first ?: run {
            // Nodes point to themselves by default, so nothing extra required here
            first = newNode
            last = newNode
            return
        }

        while (node != last) {
            require(newNode.row != node.row) { "Can't add a duplicate element in column $column, row ${newNode.row}" }

            if (node.row > newNode.row) {
                node.up.addDown(newNode)
                node.addUp(newNode)
                first = last?.down // It's easier to use the wrapping points to find the first that any other approach
                return
            }

            node = node.down
        }

        if (node.row > newNode.row) {
            node.up.addDown(newNode)
            node.addUp(newNode)
            first = last?.down
            return
        }

        node.down.addUp(newNode)
        node.addDown(newNode)
        last = newNode
        return
    }

    /**
     * Finds the first column to the right that has a node in the same row and uses it to insert the new node.
     */
    private fun insertIntoRow(newNode: Node) {
        var header = right

        while (header != this) {
            header.getNodeInRow(newNode.row)?.also {
                it.left.addRight(newNode)
                it.addLeft(newNode)
                return
            }

            header = header.right
        }
    }

    companion object {
        val globalOpCounter = AtomicInteger(0)
    }
}