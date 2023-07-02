package bricktiler.dlx

import bricktiler.board.PiecePosition
import java.io.File

/**
 * If I felt like _thinking_, I could make the column count flexible, but it's much easier to just start with a known
 * width.
 */
class SparseMatrix private constructor(val width: Int, private val desiredValues: List<Int>) {
    constructor(width: Int): this(width, List(width) { 1 } )
    constructor(desiredValues: List<Int>): this(desiredValues.size, desiredValues)

    private var firstCol: Header?

    // Because I don't feel like traversing the LL to get to a header every time.
    val headers: MutableList<Header>
    private val uncoveredHeaders: MutableSet<Header>
    fun uncoveredHeaders(): Set<Header> = uncoveredHeaders


    val height: Int
        get() = headers.filter { !it.covered }.maxOf { (it.last?.run { row + 1 }) ?: 0 }

    init {
        if (width > 0) {
            firstCol = Header(0, this, desiredValues[0])
            headers = mutableListOf(firstCol!!)
            uncoveredHeaders = mutableSetOf(firstCol!!)
        } else {
            firstCol = null
            headers = mutableListOf<Header>()
            uncoveredHeaders = mutableSetOf()
        }
        // Since the list is circular, adding to the left of the first element is the same as adding to the end. Neat
        repeat(width - 1) {
            val newHeader = Header(it + 1, this, desiredValues[it + 1])
            firstCol!!.left.addRight(newHeader)
            firstCol!!.addLeft(newHeader)
            headers.add(newHeader)
            uncoveredHeaders.add(newHeader)
        }
    }

    fun add(x: Int, y: Int, value: Int) {
        require(x < width) { "This is a circular LL, doesn't mean we're going to wrap around to insert things. " +
                "Requested column '$x' is greater than the matrix width of '$width'" }

        headers[x].insert(y, value)
    }

    fun shortestUncoveredColumn(): Header? {
        if (firstCol == null) {
            return null
        }

        var minHeader = if (firstCol!!.covered) {
            null
        } else {
            firstCol
        }

        var iterHeader = firstCol!!.right
        while (iterHeader != firstCol) {
            if (!iterHeader.covered && (minHeader == null || iterHeader.count < minHeader.count)) {
                minHeader = iterHeader
            }
            iterHeader = iterHeader.right
        }

        return minHeader
    }

    fun coverColumn(header: Header, id: String) {
        header.cover(id)
        if (header == firstCol && firstCol != null) {
            firstCol = firstCol!!.right
            while (firstCol!!.covered && header != firstCol) {
                firstCol = firstCol!!.right
            }
        }
        uncoveredHeaders.remove(header)
    }

    fun uncoverColumn(header: Header, id: String) {
        header.uncover(id)
        while (firstCol != null && firstCol!!.covered) {
            firstCol = firstCol!!.left
        }
        uncoveredHeaders.add(header)
    }

    /**
     * Copy all of the uncovered nodes into a new array
     * TODO add some tests for this and use it to parallelize things.
     */
    fun cloneUncovered(): SparseMatrix {
        if (uncoveredHeaders.size == 0) {
            return SparseMatrix(0, listOf())
        }

        // TODO inefficient
        val newMatrix = SparseMatrix(uncoveredHeaders.size, desiredValues.filterIndexed { index, _ -> uncoveredHeaders.contains(headers[index]) })

        var count = 0
        var iterHeader = firstCol

        while (iterHeader != firstCol || count == 0) {
            var iterNode = iterHeader!!.first
            if (iterNode != null) {
                newMatrix.add(count, iterNode!!.row, iterNode.value)
                iterNode = iterNode.down

                while (iterNode != iterHeader.first) {
                    newMatrix.add(count, iterNode!!.row, iterNode.value)
                    iterNode = iterNode.down
                }
            }

            count++
            iterHeader = iterHeader.right
        }

        return newMatrix
    }

    /**
     * Gotta sanity check this mess somehow
     */
    override fun toString(): String {
        val highestRow: Int = headers.map { it.last?.row ?: 0 }.maxOrNull() ?: 0

        var string = ""

        repeat(highestRow + 1) { row ->
            string += headers.map { header ->
                if (header.covered) {
                    "x"
                } else {
                    header.getNodeInRow(row)?.let { node ->
                        if (node.covered) {
                            "!" // If we see this, something's wrong. We shouldn't be able to access covered nodes
                        } else {
                            node.value
                        }
                    } ?: "0"
                }
            }.joinToString("\t") + "\n"
        }

        return string
    }

    enum class NodeBadness(val qualifier: String) {
        BAD("#red"),
        COVERED("#gray"),
        FIRST("<<first>>"),
        LAST("<<last>>")
    }

    fun pumlCompatibleVisualisation(badColumn: Int? = null, title: String? = null) = headers.mapIndexed { index, col ->
        val colStrings = mutableListOf<String>()

        col.nodes.forEach { (_, node) ->
            val qualifier = if (node.covered) {
                NodeBadness.COVERED.qualifier
            } else {
                null
            }
            val preQual = when {
                node.header.first == node -> NodeBadness.FIRST.qualifier
                node.header.last == node -> NodeBadness.LAST.qualifier
                else -> ""
            }
            colStrings.add(node.asPumlState("${preQual}${qualifier ?: ""}"))
        }

        col to colStrings.joinToString("\n")
    }.mapIndexed { index, (header, colString) ->
        val qualifier = when {
            header.column == badColumn -> "${NodeBadness.BAD.qualifier}"
            header.covered -> "${NodeBadness.COVERED.qualifier}"
            else -> ""
        }

        val colState = """
                state col$index$qualifier {
                    $colString
                }
            """.trimIndent()

        val lines = mutableListOf<String>(colState)
        if (index == 0) {
            lines.add("[*] -r-> col0")
        }
        if (index == headers.size - 1) {
            lines.add("col$index -r-> [*]")
        }
        if (headers.size != 1 && index != 0) {
            lines.add("col${index - 1} -r-> col$index")
        }
        lines.joinToString("\n")
    }.joinToString("\n\n")
        .let {
            """
                @startuml
                ${title?.let { "title $title" } ?: ""}
                skinparam state {
                  FontColor<<first>> Blue
                  FontColor<<last>> Red
                }
                $it
                @enduml
            """.trim()
        }


    fun saveStateWithId(id: String) {
        // No-op. Used this while looking for bugs
        //File("./vis/$id.puml").writeText(pumlCompatibleVisualisation(title = "$id: ${Header.globalOpCounter.get()}"))
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