package me.lysne.okapi.graphics

import me.lysne.okapi.loadImage
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import java.io.File

public class Texture(
        fileName: String,
        filter: Texture.Filter = Texture.Filter.NEAREST,
        wrapMode: Texture.WrapMode = Texture.WrapMode.CLAMP_TO_EDGE) {

    // NOTE: No mipmapping support
    enum class Filter(val code: Int) {
        NEAREST(GL11.GL_NEAREST),
        LINEAR(GL11.GL_LINEAR)
    }
    enum class WrapMode(val code: Int) {
        REPEAT(GL11.GL_REPEAT),
        CLAMP_TO_EDGE(GL12.GL_CLAMP_TO_EDGE),
        CLAMP_TO_BORDER(GL13.GL_CLAMP_TO_BORDER)
    }

    private val TEXTURE_PATH = "res/"

    val handle: Int
    val width: Int
    val height: Int

    init {
        handle = GL11.glGenTextures()

        val image = loadImage(File(TEXTURE_PATH + fileName), true)
        width = image.width
        height = image.height

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter.code)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter.code)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapMode.code)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapMode.code)

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                image.data)

        image.free()
    }

    public fun destroy() {
        GL11.glDeleteTextures(handle)
    }

    public fun bind(unit: Int = 0) {

        if (unit < 0 || unit > 31) error("Invalid texture unit")

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle)
    }
}