package me.lysne.okapi.window


import org.joml.Vector2f
import org.lwjgl.glfw.GLFW.*
import java.util.HashMap

public class Input(private val window: Window) {

    enum class Key(val code: Int) {
        QUIT(GLFW_KEY_ESCAPE),
        FORWARD(GLFW_KEY_W),
        BACKWARD(GLFW_KEY_S),
        LEFT(GLFW_KEY_A),
        RIGHT(GLFW_KEY_D),
        LOOK_LEFT(GLFW_KEY_Q),
        LOOK_RIGHT(GLFW_KEY_E)
    }

    enum class Mouse(val code: Int) {
        LEFT(GLFW_MOUSE_BUTTON_1),
        RIGHT(GLFW_MOUSE_BUTTON_2),
        MIDDLE(GLFW_MOUSE_BUTTON_3)
    }

    private val keys: HashMap<Key, Boolean> = hashMapOf(
            Key.FORWARD to false,
            Key.BACKWARD to false,
            Key.LEFT to false,
            Key.RIGHT to false,
            Key.LOOK_LEFT to false,
            Key.LOOK_RIGHT to false
    )

    private val mice: HashMap<Mouse, Boolean> = hashMapOf(
            Mouse.LEFT to false,
            Mouse.RIGHT to false,
            Mouse.MIDDLE to false
    )

    private val centerx = 960 / 2
    private val centery = 540 / 2

    public var mousedx: Int = 0
        private set
    public var mousedy: Int = 0
        private set

    init {
        registerCallbacks()
        setCursorPosition(centerx, centery)
    }

    private fun registerCallbacks() {

        window.registerKeyCallback(GLFWKeyCallback({
            w, key, scancode, action, mods ->
                if (action != GLFW_REPEAT) {
                    val pressed: Boolean = action == GLFW_PRESS
                    when (key) {
                        Key.QUIT.code -> window.setShoudClose()
                        Key.FORWARD.code -> keys[Key.FORWARD] = pressed
                        Key.BACKWARD.code -> keys[Key.BACKWARD] = pressed
                        Key.LEFT.code -> keys[Key.LEFT] = pressed
                        Key.RIGHT.code -> keys[Key.RIGHT] = pressed
                        Key.LOOK_LEFT.code -> keys[Key.LOOK_LEFT] = pressed
                        Key.LOOK_RIGHT.code -> keys[Key.LOOK_RIGHT] = pressed
                    }
                }
        }))

        window.registerMouseCallback(GLFWMouseButtonCallback({
            w, button, action, mods ->
                val pressed = action == GLFW_PRESS
                when (button) {
                    Mouse.LEFT.code -> mice[Mouse.LEFT] = pressed
                    Mouse.RIGHT.code -> mice[Mouse.RIGHT] = pressed
                    Mouse.MIDDLE.code -> mice[Mouse.MIDDLE] = pressed
                }
        }))

        window.registerCursorCallback(GLFWCursorPosCallback({
            w, xpos, ypos ->
                mousedx = (xpos).toInt() - centerx
                mousedy = (ypos).toInt() - centery
        }))
    }

    public fun keyDown(key: Key): Boolean {
        return keys.get(key)
    }

    public fun mouseButtonDown(mouseButton: Mouse): Boolean {
        return mice.get(mouseButton)
    }

    public fun setCursorPosition(x: Int, y: Int) {
        glfwSetCursorPos(window.handle, x.toDouble(), y.toDouble())
    }

    public fun centerCursor() {
        setCursorPosition(centerx, centery)
        mousedx = 0
        mousedy = 0
    }
}
