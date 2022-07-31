package bricktiler.image

import bricktiler.Solution
import bricktiler.board.Board
import bricktiler.board.BoardUtils
import bricktiler.board.PiecePosition
import ij.IJ
import ij.ImagePlus
import ij.process.ColorProcessor
import ij.process.ImageConverter
import ij.process.ImageProcessor
import java.awt.Color

class ImageManager private constructor(val image: ImagePlus) {


    fun colourAt(x: Int, y: Int): Int {
        return image.getPixel(x, y)[0]
    }

    fun asOneDimensionalArray(): List<Int> {
        return List(image.height) { y ->
            List(image.width) { x ->
                colourAt(x, y)
            }
        }.flatten()
    }

    fun show(scaling: Int) {
        val scaled = ImagePlus("BIGBOI", image.image.getScaledInstance(image.width * scaling, image.height * scaling, 0))
        scaled.show()
    }

    override fun toString() = List(image.height) { y ->
            List(image.width) { x ->
                colourAt(x, y)
            }.joinToString("")
        }.joinToString("\n")

    companion object {
        private fun setupEnv() {
            // The ImageJ library does some checks that require this to be set. I don't feel like fighting it.
            System.setProperty("java.version", "1.8")
        }

        fun downscaleFromFile(width: Int, height: Int, colourCount: Int, imagePath: String = "C:/Users/user-pc/Desktop/gradient.jpg"): ImageManager {
            setupEnv()

            val imp = IJ.openImage(imagePath)

            val downscaled = ImagePlus("huh?", imp.image.getScaledInstance(width, height, 0))

            ImageConverter(downscaled).run {
                convertToGray8()
                convertToRGB()
                convertRGBtoIndexedColor(colourCount)
            }

            return ImageManager(downscaled)
        }

        fun fromSolution(solution: Solution, board: Board, piecePositions: List<PiecePosition>,
                         colorMapping: Map<Int, Color> = mapOf(
                             1 to Color.BLACK,
                             2 to Color.DARK_GRAY,
                             3 to Color.LIGHT_GRAY,
                             4 to Color.WHITE
                         )): ImageManager {
            setupEnv()

            val imp = IJ.createImage("Foo", "RGB", board.width, board.height, 32)

            val describedSolution = BoardUtils.describeSolution(solution, board, piecePositions)

            val processor = ColorProcessor(imp.image).also { colorProcessor ->
                describedSolution.forEach { (position, value) ->
                    colorProcessor.setColor(colorMapping[value] ?: Color.RED)
                    colorProcessor.drawPixel(position.x, position.y)
                }
            }

            return ImageManager(ImagePlus("foo", processor))
        }
    }
}