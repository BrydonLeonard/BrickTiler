package bricktiler.image

import ij.IJ
import ij.ImagePlus
import ij.process.ImageConverter

class ImageManager private constructor(val width: Int, val height: Int, val colourCount: Int, val original: ImagePlus) {

    private val downscaled: ImagePlus

    init {
        downscaled = ImagePlus("huh?", original.image.getScaledInstance(width, height, 0))

        ImageConverter(downscaled).run {
            convertToGray8()
            convertToRGB()
            convertRGBtoIndexedColor(colourCount)
        }
    }

    fun colourAt(x: Int, y: Int): Int {
        return downscaled.getPixel(x, y)[0]
    }

    fun asOneDimensionalArray(): List<Int> {
        return List(downscaled.height) { y ->
            List(downscaled.width) { x ->
                colourAt(x, y)
            }
        }.flatten()
    }

    fun show() {
        downscaled.show()
    }

    override fun toString() = List(downscaled.height) { y ->
            List(downscaled.width) { x ->
                colourAt(x, y)
            }.joinToString("")
        }.joinToString("\n")

    companion object {
        private fun setupEnv() {
            // The ImageJ library does some checks that require this to be set. I don't feel like fighting it.
            System.setProperty("java.version", "1.8")
        }

        fun fromFile(width: Int, height: Int, colourCount: Int, imagePath: String = "C:/Users/user-pc/Desktop/Headpon.png"): ImageManager {
            setupEnv()

            val imp = IJ.openImage(imagePath)

            return ImageManager(width, height, colourCount, imp)
        }
    }
}