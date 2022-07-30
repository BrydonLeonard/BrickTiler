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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Solution

        if (solutionRows.toList() != other.solutionRows.toList()) return false

        return true
    }

    override fun hashCode(): Int {
        return solutionRows.toList().hashCode()
    }
}