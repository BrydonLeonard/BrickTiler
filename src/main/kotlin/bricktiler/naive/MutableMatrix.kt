package bricktiler.naive

/**
 * Yes, very ugly and inefficient, but ignore it for now.
 */
class MutableMatrix<T>(private val matrix: MutableList<MutableList<T>>, val originalRows: MutableList<Int>) {
    constructor(height: Int, width: Int, defaultValue: T) :
            this(MutableList(height) { MutableList(width) { defaultValue } })

    constructor(matrix: MutableList<MutableList<T>>) :
            this(matrix, MutableList(matrix.size) { it })

    init {
        val expectedWidth = matrix[0].size

        matrix.forEach { row ->
            require(row.size == expectedWidth) { "Matrix was not a rectangle" }
        }
    }

    val width: Int
        get() = if (matrix.size > 0) matrix[0].size else 0
    val height: Int
        get() = matrix.size

    operator fun get(x: Int, y: Int): T = matrix[y][x]

    operator fun set(x: Int, y: Int, value: T) {
        matrix[y][x] = value
    }

    fun getRow(index: Int) = matrix[index]

    fun removeRows(rows: List<Int>) {
        rows.sortedDescending().forEach { row ->
            matrix.removeAt(row)
            originalRows.removeAt(row)
        }
    }

    fun removeColumns(cols: List<Int>) {
        cols.sortedDescending().forEach { column ->
            matrix.forEach { row ->
                row.removeAt(column)
            }
        }
    }

    fun <P> mapEachRow(operation: (List<T>) -> P): List<P> {
        return matrix.map(operation)
    }

    fun <P> mapEachColumn(operation: (Iterable<T>) -> P): List<P> {
        return List(width) { column -> operation(columnIterable(column)) }
    }

    private fun columnIterable(column: Int) = object : Iterable<T> {
        var row = 0
        override fun iterator() = object : Iterator<T> {
            override fun hasNext() = row < height 
            override fun next() = matrix[row++][column]
        }
    }

    fun clone(): MutableMatrix<T> = MutableMatrix(matrix.map { it.toMutableList() }.toMutableList(), originalRows.toMutableList())
}