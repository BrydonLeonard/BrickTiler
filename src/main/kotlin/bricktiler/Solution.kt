package bricktiler

class Solution {
    val solutionRows: MutableSet<Int> = mutableSetOf()

    operator fun plus(row: Int) = Solution().add(this).add(row)

    fun add(rows: Set<Int>) {
        solutionRows.addAll(rows)
    }

    fun add(other: Solution): Solution {
        add(other.solutionRows)
        return this
    }

    fun add(rowIndex: Int): Solution {
        solutionRows.add(rowIndex)
        return this
    }

    fun remove(rows: Set<Int>): Solution {
        solutionRows.removeAll(rows)
        return this
    }

    fun remove(row: Int): Solution {
        solutionRows.remove(row)
        return this
    }

    fun clone(): Solution {
        return Solution().add(this)
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