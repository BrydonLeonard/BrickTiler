package bricktiler.dlx

import bricktiler.board.PiecePosition

/**
 * If I felt like _thinking_, I could make the column count flexible, but it's much easier to just start with a known
 * width.
 */
class SparseMatrix private constructor(private val width: Int, private val desiredValues: List<Int>) {
    constructor(width: Int): this(width, List(width) { 1 } )
    constructor(desiredValues: List<Int>): this(desiredValues.size, desiredValues)

    private val firstCol: Header = Header(0, desiredValues[0])

    // Because I don't feel like traversing the LL to get to a header every time.
    val headers: MutableList<Header> = mutableListOf(firstCol)
    private val uncoveredHeaders: MutableSet<Header> = mutableSetOf()
    fun uncoveredHeaders(): Set<Header> = uncoveredHeaders

    private val rows: MutableList<PiecePosition?> = mutableListOf()


    val height: Int
        get() = headers.filter { !it.covered }.maxOf { (it.last?.run { row + 1 }) ?: 0 }

    init {
        // Since the list is circular, adding to the left of the first element is the same as adding to the end. Neat
        repeat(width - 1) {
            val newHeader = Header(it + 1, desiredValues[it + 1])
            firstCol.left.addRight(newHeader)
            firstCol.addLeft(newHeader)
            headers.add(newHeader)
            uncoveredHeaders.add(newHeader)
        }
    }

    /**
     * Add row metadata. Useful for applying constraints and turning a solved matrix into a usable solution
     */
    fun setRow(piecePosition: PiecePosition, matrixRow: Int) {
        if (rows.size <= matrixRow) {
            rows.add(piecePosition)
        }
    }

    fun add(x: Int, y: Int, value: Int) {
        require(x < width) { "This is a circular LL, doesn't mean we're going to wrap around to insert things. " +
                "Requested column '$x' is greater than the matrix width of '$width'" }

        require(y < rows.count()) { "Attempted to add to row $y before initialization. The matrix has ${rows.count()} rows." }

        headers[x].insert(y, value)
    }

    // TODO inefficient
    fun shortestUncoveredColumn() = uncoveredHeaders.minByOrNull { it.count }
    fun coverColumn(header: Header) {
        header.cover()
        uncoveredHeaders.remove(header)
    }

    fun uncoverColumn(header: Header) {
        header.uncover()
        uncoveredHeaders.add(header)
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

    fun rowsToString(rows: List<Int>): String {
        val solutionString = "->\t" + headers.map { header ->
            rows.mapNotNull { header.getNodeInRow(it) }.first().value
        }.joinToString("\t")

        val rowsString =  rows.map { row ->
            "${row}:\t" + headers.map { header ->
                header.getNodeInRow(row)?.value ?: 0
            }.joinToString("\t")
        }.joinToString("\n")

        return solutionString + "\n" + rowsString

    }
}