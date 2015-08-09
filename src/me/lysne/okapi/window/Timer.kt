package me.lysne.okapi.window


import org.lwjgl.glfw.GLFW.glfwGetTime


public fun getTime(): Double {
    return glfwGetTime()
}
