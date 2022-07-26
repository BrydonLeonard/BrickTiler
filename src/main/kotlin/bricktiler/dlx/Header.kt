package bricktiler.dlx

class Header(val column: Int, val desiredValue: Int = 1) {
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

    /**
     * Covers this column and all rows in which it has a value
     */
    fun cover() {
        covered = true
        this.left.right = this.right
        this.right.left = this.left

        // Moves down the column to cover rows with 1s
        var rowNode = first

        while (rowNode != last && rowNode != null) {
            coverRow(rowNode)
            rowNode = rowNode.down
        }

        coverRow(rowNode)
    }

    /**
     * The exact opposite of [cover] (including the order in which uncovering happens)
     */
    fun uncover() {
        covered = false
        this.right.left = this
        this.left.right = this

        var rowNode = last

        while (rowNode != first && rowNode != null) {
            uncoverRow(rowNode)
            rowNode = rowNode.up
        }

        uncoverRow(rowNode)
    }

    /**
     * Return the node in a given row in this column. Null if no value is present in the column
     */
    fun getNodeInRow(row: Int): Node? {
        var node = first ?: return null

        while (node != last) {
            if (node.row == row) {
                return node
            }
            node = node.down
        }

        if (node.row == row) {
            return node
        }

        return null
    }

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
        insertIntoColumn(newNode)
        insertIntoRow(newNode)
    }

    /**
     * Will cover all nodes in the row _except_ the one passed in as an argument
     */
    private fun coverRow(startingNode: Node?) {
        startingNode ?: return

        var node = startingNode.right

        while (node != startingNode) {
            node.cover()
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

            node = node.right
        }
    }

    /**
     * Will uncover all nodes in the given row _except_ the one provided as an arugment
     */
    private fun uncoverRow(startingNode: Node?) {
        startingNode ?: return

        var node = startingNode.left

        while (node != startingNode) {
            node.uncover()
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

        if (node.row > node.row) {
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
}