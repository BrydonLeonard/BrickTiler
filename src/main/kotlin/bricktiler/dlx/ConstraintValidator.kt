package bricktiler.dlx

import bricktiler.Solution
import bricktiler.board.Piece
import bricktiler.board.PiecePosition
import java.util.concurrent.atomic.AtomicInteger

/**
 * TODO: Would be nice to bump over to a sealed interface
 */
sealed class ConstraintValidator {
    abstract fun addRowToSolution(row: Int, currentSolution: Solution): Boolean
    abstract fun removeRowFromSolution(row: Int, currentSolution: Solution)
    abstract fun clone(): ConstraintValidator
}

class NoopConstraintValidator : ConstraintValidator() {
    override fun addRowToSolution(row: Int, currentSolution: Solution): Boolean {
        currentSolution.add(row)
        return true
    }

    override fun removeRowFromSolution(row: Int, currentSolution: Solution) {
        currentSolution.remove(row)
    }

    /**
     * Just returns itself. It doesn't do anything anyway.
     */
    override fun clone(): ConstraintValidator {
        return this
    }
}

/**
 * @param pieceSizeValueCountMap maps piece area:value:count (we use area so that 1x2 == 2x1, for example).
 */
class PieceCountConstraintValidator(private val pieceAreaValueCountMap: MutableMap<Int, MutableMap<Int, Int>>, val piecePositions: List<PiecePosition>): ConstraintValidator() {
    override fun addRowToSolution(row: Int, currentSolution: Solution): Boolean {
        val piecePosition = piecePositions[row]
        val remaining = pieceAreaValueCountMap[piecePosition.piece.size]!![piecePosition.value]!! - 1
        if (remaining >= 0) {
            pieceAreaValueCountMap[piecePosition.piece.size]!![piecePosition.value] = remaining
            currentSolution.add(row)
            return true
        }
        return false
    }

    override fun removeRowFromSolution(row: Int, currentSolution: Solution) {
        val piecePosition = piecePositions[row]
        currentSolution.remove(row)
        pieceAreaValueCountMap[piecePosition.piece.size]!![piecePosition.value] = pieceAreaValueCountMap[piecePosition.piece.size]!![piecePosition.value]!! + 1
    }

    override fun clone(): ConstraintValidator {
        return PieceCountConstraintValidator(
            pieceAreaValueCountMap.entries.associate { entry -> (entry.key to entry.value.toMutableMap()) } as MutableMap,
            piecePositions
        )
    }
}

