import bricktiler.image.ImageManager
import ij.IJ
import ij.ImagePlus
import ij.io.FileSaver
import ij.process.ImageConverter

fun main() {
    System.setProperty("java.version", "1.8")

    val imp = IJ.openImage("C:/Users/user-pc/Desktop/roll.jpg")

    val width = imp.width / 100
    val height = imp.height / 100

    val bigWidth = imp.width / 10
    val bigHeight = imp.height / 10

    FileSaver(ImagePlus("huh?", imp.image.getScaledInstance(bigWidth, bigHeight, 0))).saveAsPng("C:/Users/user-pc/Desktop/roll-1.png")

    val downscaled = ImagePlus("huh?", imp.image.getScaledInstance(width, height, 0))
    val upscaled = ImagePlus("huh?", downscaled.image.getScaledInstance(bigWidth, bigHeight, 0))

    FileSaver(upscaled).saveAsPng("C:/Users/user-pc/Desktop/roll-2.png")

    ImageConverter(downscaled).run {
        convertToGray8()
        convertToRGB()
    }
    val upscaled2 = ImagePlus("huh?", downscaled.image.getScaledInstance(bigWidth, bigHeight, 0))
    FileSaver(upscaled2).saveAsPng("C:/Users/user-pc/Desktop/roll-3.png")

    ImageConverter(downscaled).run {
        convertRGBtoIndexedColor(6)
    }

    val upscaled3 = ImagePlus("huh?", downscaled.image.getScaledInstance(bigWidth, bigHeight, 0))
    FileSaver(upscaled3).saveAsPng("C:/Users/user-pc/Desktop/roll-4.png")

    repeat(6) { row ->
        repeat(10) { column ->
            print((downscaled.getPixel(row, column)[0] + 1).toString() + "\t")
        }
        println("...")
    }
    println("...\t...\t...\t...\t...\t...\t...\t...\t...\t...\t")
}