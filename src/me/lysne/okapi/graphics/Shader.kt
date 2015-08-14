package me.lysne.okapi.graphics

import me.lysne.okapi.readFile
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.File
import java.util.HashMap

private var programInUse = -1

public fun unsetShader() {
    programInUse = -1
    GL20.glUseProgram(0)
}

public class Shader(vertexShaderPath: String, fragmentShaderPath: String) {

    private val SHADER_PATH = "res/shaders/"

    private val program: Int
    private val vertexShader: Int
    private val fragmentShader: Int

    private val uniformLocations: HashMap<String, Int> = HashMap()

    init {
        program = GL20.glCreateProgram()
        vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)

        val vertexShaderSource = readFile(File(SHADER_PATH + vertexShaderPath))
        GL20.glShaderSource(vertexShader, vertexShaderSource)
        GL20.glCompileShader(vertexShader)
        checkShaderLog(vertexShader, GL20.GL_COMPILE_STATUS)

        val fragmentShaderSource = readFile(File(SHADER_PATH + fragmentShaderPath))
        GL20.glShaderSource(fragmentShader, fragmentShaderSource)
        GL20.glCompileShader(fragmentShader)
        checkShaderLog(fragmentShader, GL20.GL_COMPILE_STATUS)

        GL20.glAttachShader(program, vertexShader)
        GL20.glAttachShader(program, fragmentShader)
        GL20.glLinkProgram(program)
        checkProgramLog(program, GL20.GL_LINK_STATUS)
        GL20.glValidateProgram(program)
        checkProgramLog(program, GL20.GL_VALIDATE_STATUS)
    }

    public fun destroy() {

        GL20.glDetachShader(program, vertexShader)
        GL20.glDetachShader(program, fragmentShader)

        GL20.glDeleteShader(vertexShader)
        GL20.glDeleteShader(fragmentShader)

        GL20.glDeleteProgram(program)
    }

    public fun use() {

        // Avoid switching context. Not sure if worth it
        // NOTE: NOT threadsafe!
        if (program != programInUse) {
            GL20.glUseProgram(program)
            programInUse = program
        }
    }

    public fun registerUniforms(vararg uniforms: String) {

        for (uniform in uniforms) {
            val location = GL20.glGetUniformLocation(program, uniform)
            if (location == -1) {
                println("Failed to locate uniform: $uniform")
                continue
            }
            uniformLocations[uniform] = location
        }
    }

    public fun setUniform(name: String, i: Int) {

        use()
        GL20.glUniform1i(uniformLocations[name], i)
    }

    public fun setUniform(name: String, f: Float) {

        use()
        GL20.glUniform1f(uniformLocations[name], f)
    }

    public fun setUniform(name: String, v: Vector2f) {

        use()
        GL20.glUniform2f(uniformLocations[name], v.x, v.y)
    }

    public fun setUniform(name: String, v: Vector3f) {

        use()
        GL20.glUniform3f(uniformLocations[name], v.x, v.y, v.z)
    }

    public fun setUniform(name: String, v: Vector4f) {

        use()
        GL20.glUniform4f(uniformLocations[name], v.x, v.y, v.z, v.w)
    }

    public fun setUniform(name: String, q: Quaternionf) {

        use()
        GL20.glUniform4f(uniformLocations[name], q.x, q.y, q.z, q.w)
    }

    public fun setUniform(name: String, m: Matrix4f) {

        use()
        val fb = BufferUtils.createFloatBuffer(16)
        m.get(fb)
        GL20.glUniformMatrix4fv(uniformLocations[name], false, fb)
    }

    public fun setUniform(name: String, a: Array<Vector3f>) {

        use()
        a.forEachIndexed { i, v ->
            val fb = BufferUtils.createFloatBuffer(3)
            v.get(fb)
            GL20.glUniform3fv(
                    GL20.glGetUniformLocation(program, name + "[$i]"),
                    fb)
        }
    }

    private fun checkShaderLog(handle: Int, flag: Int) {

        val status = GL20.glGetShaderi(handle, flag)
        if (status != GL11.GL_TRUE) {
            val log = GL20.glGetShaderInfoLog(handle)
            println("Shader error: $log")
        }
    }

    private fun checkProgramLog(handle: Int, flag: Int) {

        val status = GL20.glGetProgrami(handle, flag)
        if (status != GL11.GL_TRUE) {
            val log = GL20.glGetProgramInfoLog(handle)
            println("Program error: $log")
        }
    }


}