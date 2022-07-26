package bricktiler

class Solution {
    val solutionRows: MutableList<Int> = mutableListOf()

    fun addRows(rows: List<Int>) {
        solutionRows.addAll(rows)
    }

    operator fun plus(other: Solution): Solution {
        addRows(other.solutionRows)
        return this
    }

    operator fun plus(rowIndex: Int): Solution {
        solutionRows.add(rowIndex)
        return this
    }

    override fun toString(): String {
        return solutionRows.joinToString("\t")
    }
}