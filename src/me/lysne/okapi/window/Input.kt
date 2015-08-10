package me.lysne.okapi.window


import me.lysne.okapi.Config
import org.lwjgl.glfw.GLFW.*
import java.util.HashMap

public class Input(private val window: Window) {

    enum class Key(val code: Int) {
        QUIT(GLFW_KEY_ESCAPE),
        FORWARD(GLFW_KEY_W),
        BACKWARD(GLFW_KEY_S),
        LEFT(GLFW_KEY_A),
        RIGHT(GLFW_KEY_D),
        UP(GLFW_KEY_UP),
        DOWN(GLFW_KEY_DOWN)
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
            Key.UP to false,
            Key.DOWN to false
    )

    private val mice: HashMap<Mouse, Boolean> = hashMapOf(
            Mouse.LEFT to false,
            Mouse.RIGHT to false,
            Mouse.MIDDLE to false
    )

    private val centerx = Config.WindowWidth / 2
    private val centery = Config.WindowHeight / 2

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
                        Key.UP.code -> keys[Key.UP] = pressed
                        Key.DOWN.code -> keys[Key.DOWN] = pressed
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
