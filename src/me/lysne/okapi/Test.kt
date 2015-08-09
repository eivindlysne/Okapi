package me.lysne.okapi

import me.lysne.okapi.graphics.Mesh
import me.lysne.okapi.graphics.Shader
import me.lysne.okapi.graphics.Texture
import me.lysne.okapi.graphics.Vertex
import org.joml.Vector2f
import org.joml.Vector3f

public object Test {

    public fun getTestTexture() : Texture {
        return Texture("crack.png", Texture.Filter.LINEAR, Texture.WrapMode.REPEAT)
    }

    public fun getBasicShader() : Shader {
        val shader = Shader("basic_vert.glsl", "basic_frag.glsl")
        shader.registerUniforms(arrayOf(
                "diffuse0", "diffuse1",
                "viewProjection",
                "transform.position",
                "transform.orientation",
                "transform.scale"))
        shader.setUniform("diffuse0", 0)
//        shader.setUniform("diffuse1", 1)
        return shader
    }
}