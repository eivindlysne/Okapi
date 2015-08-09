package me.lysne.okapi.window

import org.lwjgl.glfw.*
import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL


public class Window(val width: Int, val height: Int, val title: String) {

    private val errorCallback: GLFWErrorCallback = errorCallbackPrint(System.err)

    private var keyCallback: GLFWKeyCallback? = null
    private var mouseCallback: GLFWMouseButtonCallback? = null
    private var cursorCallback: GLFWCursorPosCallback? = null

    public var handle: Long = 0
        private set

    init {
        initWindow()
        initOpenGL()

        // Must be called after context creation
        glfwSwapInterval(1)
    }

    private fun initWindow() {
        glfwSetErrorCallback(errorCallback)

        if (glfwInit() != GL11.GL_TRUE)
            error("Failed to init GLFW")

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_RESIZABLE, GL11.GL_FALSE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL11.GL_TRUE)

        handle = glfwCreateWindow(width, height, title, NULL, NULL)
        if (handle == NULL)
            error("Failed to create window")

        val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        glfwSetWindowPos(handle, (GLFWvidmode.width(vidmode) - width) / 2, (GLFWvidmode.height(vidmode) - height) / 2)
    }

    private fun initOpenGL() {

        glfwMakeContextCurrent(handle)
        GLContext.createFromCurrent()

        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glViewport(0, 0, width, height)

        GL11.glEnable(GL11.GL_TEXTURE_2D); // Not needed?
        GL11.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS); // Dunno if doin' anything

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public fun destroy() {

        glfwDestroyWindow(handle)

        keyCallback?.release()
        mouseCallback?.release()
        cursorCallback?.release()

        glfwTerminate()
        errorCallback.release()
    }

    public fun show() {
        glfwShowWindow(handle)
    }

    public fun clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    public fun poll() {
        glfwPollEvents()
    }

    public fun swap() {
        glfwSwapBuffers(handle)
    }

    public fun shouldClose(): Boolean {
        return glfwWindowShouldClose(handle) == GL11.GL_TRUE
    }

    public fun setShoudClose() {
        glfwSetWindowShouldClose(handle, GL11.GL_TRUE)
    }

    public fun registerKeyCallback(keyCallback: GLFWKeyCallback) {
        this.keyCallback = keyCallback
        glfwSetKeyCallback(handle, this.keyCallback)
    }

    public fun registerMouseCallback(mouseCallback: GLFWMouseButtonCallback) {
        this.mouseCallback = mouseCallback
        glfwSetMouseButtonCallback(handle, this.mouseCallback)
    }

    public fun registerCursorCallback(cursorCallback: GLFWCursorPosCallback) {
        this.cursorCallback = cursorCallback
        glfwSetCursorPosCallback(handle, this.cursorCallback)
    }
}
