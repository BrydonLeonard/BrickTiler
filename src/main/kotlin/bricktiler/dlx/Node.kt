package bricktiler.dlx

import bricktiler.Logger.Companion.logger
import bricktiler.dlx.Header.Companion.globalOpCounter
import java.util.*

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
    var marker: Boolean = false

    val ops = Stack<String>()

    fun cover(id: String) {
        if (this.covered) {
            println("EVEN WORSE")
        }
        this.up.down = this.down
        val op = globalOpCounter.incrementAndGet()
        logger.log("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Hid ${this.row} from ${this.up.row} above")
        ops.add("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Hid ${this.row} from ${this.up.row} above")
        this.down.up = this.up
        logger.log("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Hid ${this.row} from ${this.down.row} below")
        ops.add(("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Hid ${this.row} from ${this.down.row} below"))
        this.covered = true


        if ((this.up.covered || this.down.covered)) {
            this.marker = true
        }
    }

    fun uncover(id: String) {
        this.up.down = this
        val op = globalOpCounter.incrementAndGet()
        this.header.matrix.saveStateWithId("$op-uncover-before")
        logger.log("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Revealed ${this.row} to ${this.up.row} above")
        ops.add(("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Revealed ${this.row} to ${this.up.row} above"))
        this.down.up = this
        logger.log("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Revealed ${this.row} to ${this.down.row} below")
        ops.add("[$id] [${this.header.id}] [$op] [Col ${header.column}] [$this] Revealed ${this.row} to ${this.down.row} below")
        this.covered = false
        this.header.matrix.saveStateWithId("$op-uncover-after")

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

    val shortName: String = this.toString().split("@")[1]

    fun asPumlState(qualifier: String?): String {
        val lines = mutableListOf<String>()
        lines.let {
            it.add("state $shortName" + (qualifier ?: ""))
            it.add("$shortName:(${this.header.column},${this.row})")
            if (this.up != this) {
                it.add("$shortName -u-> ${this.up.shortName}")
            }
            if (this.down != this) {
                it.add("$shortName -d-> ${this.down.shortName}")
            }
            if (this.left != this) {
                //it.add("$shortName -l-> ${this.left.shortName}")
            }
            if (this.right != this) {
                //it.add("$shortName -r-> ${this.right.shortName}")
            }
        }

        return lines.joinToString("\n")
    }
}