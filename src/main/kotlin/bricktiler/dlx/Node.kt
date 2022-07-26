package bricktiler.dlx

class Node(val header: Header, val row: Int, val value: Int) {
    var up: Node = this
        private set
    var down: Node = this
        private set
    var left: Node = this
        private set
    var right: Node = this
        private set

    /**
     * Useful for debugging. A covered node should be inaccessible from its header
     */
    var covered: Boolean = false
        private set

    fun cover() {
        this.up.down = this.down
        this.down.up = this.up
        this.covered = true
    }

    fun uncover() {
        this.up.down = this
        this.down.up = this
        this.covered = false
    }

    fun addUp(node: Node) {
        this.up = node
        node.down = this
    }

    fun addDown(node: Node) {
        this.down = node
        node.up = this
    }

    fun addLeft(node: Node)  {
        this.left = node
        node.right = this
    }

    fun addRight(node: Node) {
        this.right = node
        node.left = this
    }
}