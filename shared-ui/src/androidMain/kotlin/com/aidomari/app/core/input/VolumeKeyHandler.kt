package com.aidomari.app.core.input

import android.view.KeyEvent

object VolumeKeyHandler {
    var listener: ((isUp: Boolean) -> Boolean)? = null

    fun handleKeyEvent(keyCode: Int): Boolean {
        val currentListener = listener ?: return false
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> currentListener(true)
            KeyEvent.KEYCODE_VOLUME_DOWN -> currentListener(false)
            else -> false
        }
    }
}
