package me.lysne.okapi

import org.lwjgl.BufferUtils
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.imageio.ImageIO
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer


public fun readFile(file: File): String {

    if (!file.exists()) {
        println("File <\"$file\"> does not exist")
        return ""
    }

    return file.readText()
}

public fun loadBufferedImage(file: File, flip: Boolean): BufferedImage {

    if (!file.exists()) {
        println("File <\"$file\"> does not exist")
        return BufferedImage(0, 0, 0)
    }

    val image = ImageIO.read(file)

    if (flip) {
        val transform = AffineTransform.getScaleInstance(1.0, -1.0)
        transform.translate(0.0, -image.getHeight().toDouble())
        val operation = AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
        return operation.filter(image, null)
    }

    return image
}

class Image(val data: ByteBuffer, val width: Int, val height: Int) {
    public fun free() {
        STBImage.stbi_image_free(data)
    }
}

public fun loadImage(file: File, flip: Boolean): Image {

    STBImage.stbi_set_flip_vertically_on_load(if (flip) 1 else 0)

    val reqComp = 4
    val width = BufferUtils.createIntBuffer(1)
    val height = BufferUtils.createIntBuffer(1)
    val numComponents = BufferUtils.createIntBuffer(1)
    val data = STBImage.stbi_load(file.path, width, height, numComponents, reqComp)
    return Image(data, width.get(), height.get())
}