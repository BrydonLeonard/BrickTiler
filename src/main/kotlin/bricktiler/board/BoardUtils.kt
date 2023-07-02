package bricktiler.board

import bricktiler.Solution
import bricktiler.dlx.SparseMatrix
import kotlin.math.absoluteValue

// TODO these two data classes could be combined a little more neatly
data class PiecePosition(
    val piece: Piece,
    val oneDimensionalPosition: Int,
    val value: Int
)

object BoardUtils {
    /**
     * @param includeInvalidRows If false, rows that have no columns matching the desired solution won't be added to the matrix at all.
     */
    fun makeSparseMatrix(desiredSolution: List<Int>, board: Board, pieces: List<Pair<Piece, Int>>, includeInvalidRows: Boolean = false, maxPerPieceError: Double = 0.0): Pair<SparseMatrix, List<PiecePosition>> {
        val sparseMatrix = SparseMatrix(desiredSolution)
        val piecePositions: MutableList<PiecePosition> = mutableListOf()
        var row = 0

        pieces.shuffled().forEach { (piece, value) ->
            piece.getAllValidTopLeftPositions(board).forEach { topLeft ->
                val covering = piece.topLeftToPieceCovering(topLeft, board)

                if (includeInvalidRows || errorIsWithinBounds(covering, desiredSolution, value, maxPerPieceError)){
                    val piecePosition = PiecePosition(piece, topLeft, value)
                    piecePositions.add(piecePosition)
                    covering.forEach { position ->
                        sparseMatrix.add(position, row, value)
                    }
                    row++
                }
            }
        }

        if (sparseMatrix.headers.any { it.first == null}) {
            throw Exception("Impossible to generate a problem space with the given error threshold")
        }

        return sparseMatrix to piecePositions
    }

    fun errorIsWithinBounds(covering: List<Int>, desiredValues: List<Int>, value: Int, maxPerPieceError: Double): Boolean {
        val adjustedMaxError = when(covering.size) {
            2 -> maxPerPieceError
            4 -> 2 * maxPerPieceError
            6 -> 3 * maxPerPieceError
            8 -> 4 * maxPerPieceError
            else -> 0
        }

        val calc1 = { position: Int -> if ((value - desiredValues[position]).absoluteValue > 0) 1.0 else 0.0 }
        val calc2 = { position: Int -> (value - desiredValues[position]).absoluteValue }
        val calc3 = { position: Int -> (value - desiredValues[position]) }

        val error = covering.sumOf(calc1)
        return error <= adjustedMaxError.toDouble()
    }

    enum class SolutionErrorTypes {
        TOTAL_DIFF,
        SIMPLE,
        SIMPLE_CENTRE_WEIGHTED,
        TOTAL_DIFF_CENTRE_WEIGHTED,
    }

    fun describeSolutionError(solution: Solution, board: Board, piecePositions: List<PiecePosition>, desiredValues: List<Int>, algorithm: SolutionErrorTypes): Long =
        when(algorithm) {
            SolutionErrorTypes.TOTAL_DIFF ->
                solution.solutionRows.sumOf { row ->
                    val piece = piecePositions[row]
                    val pieceCovering = piece.piece.topLeftToPieceCovering(piece.oneDimensionalPosition, board)

                    pieceCovering.sumOf { position ->
                        (piece.value - desiredValues[position]).absoluteValue.toLong()
                    }
                }
            SolutionErrorTypes.SIMPLE ->
                solution.solutionRows.sumOf { row ->
                    val piece = piecePositions[row]
                    val pieceCovering = piece.piece.topLeftToPieceCovering(piece.oneDimensionalPosition, board)

                    pieceCovering.sumOf { position ->
                        if ((piece.value - desiredValues[position]).absoluteValue > 0) 1L else 0L
                    }
                }
            SolutionErrorTypes.SIMPLE_CENTRE_WEIGHTED ->
                solution.solutionRows.sumOf { row ->
                    val piece = piecePositions[row]
                    val pieceCovering = piece.piece.topLeftToPieceCovering(piece.oneDimensionalPosition, board)

                    pieceCovering.sumOf { position ->
                        val twoDimPosition = BoardUtils.toTwoDimensions(position, board)

                        val colDist = board.centerCol + 1 - (twoDimPosition.x - board.centerCol).absoluteValue
                        val rowDist = board.centerRow + 1 - (twoDimPosition.y - board.centerRow).absoluteValue

                        val sqDist = colDist * colDist + rowDist * rowDist

                        sqDist * if ((piece.value - desiredValues[position]).absoluteValue > 0) 1L else 0L
                    }
                }
            SolutionErrorTypes.TOTAL_DIFF_CENTRE_WEIGHTED ->
                solution.solutionRows.sumOf { row ->
                    val piece = piecePositions[row]
                    val pieceCovering = piece.piece.topLeftToPieceCovering(piece.oneDimensionalPosition, board)

                    pieceCovering.sumOf { position ->
                        val twoDimPosition = BoardUtils.toTwoDimensions(position, board)

                        val colDist = board.centerCol + 1 - (twoDimPosition.x - board.centerCol).absoluteValue
                        val rowDist = board.centerRow + 1 - (twoDimPosition.y - board.centerRow).absoluteValue

                        val sqDist = colDist + rowDist

                        sqDist * (piece.value - desiredValues[position]).absoluteValue.toLong()
                    }
                }
        }


    fun describeSolution(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
        solution.solutionRows.flatMap { row ->
            val piece = piecePositions[row]
            val pieceCovering = piece.piece.topLeftToPieceCovering(piece.oneDimensionalPosition, board)

            pieceCovering.map {
                val twoDimensionalPosition = toTwoDimensions(it, board)
                Position(twoDimensionalPosition.x, twoDimensionalPosition.y) to piecePositions[row].value
            }
        }.toMap()

    fun describeSolutionAsString(solution: Solution, board: Board, piecePositions: List<PiecePosition>) =
        solution.solutionRows.map { row ->
            val piecePosition = piecePositions[row]

            Pair(piecePosition, toTwoDimensions(piecePosition.oneDimensionalPosition, board))
        }.sortedBy { it.second.x }.sortedBy { it.second.y }.joinToString("\n") { (piecePosition, twoDimPosition) ->
            "${piecePosition.piece}, ${piecePosition.value}, $twoDimPosition"
        }


    fun toTwoDimensions(x: Int, board: Board) = Position(x % board.width, x / board.width)
}

