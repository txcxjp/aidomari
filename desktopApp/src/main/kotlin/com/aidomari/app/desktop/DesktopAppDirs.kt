package com.aidomari.app.desktop

import java.io.File

/**
 * Resolves OS-appropriate local storage directories for Aidomari desktop data.
 */
object DesktopAppDirs {
    fun resolveAppDataDir(
        osName: String = System.getProperty("os.name").orEmpty(),
        userHome: String = System.getProperty("user.home").orEmpty(),
        appDataEnv: String? = System.getenv("APPDATA"),
        xdgDataHomeEnv: String? = System.getenv("XDG_DATA_HOME"),
    ): File {
        val baseDir = when {
            osName.contains("win", ignoreCase = true) -> {
                if (!appDataEnv.isNullOrBlank()) {
                    File(appDataEnv, "Aidomari")
                } else {
                    File(File(userHome, "AppData/Roaming"), "Aidomari")
                }
            }

            osName.contains("mac", ignoreCase = true) -> {
                File(File(userHome, "Library/Application Support"), "Aidomari")
            }

            else -> {
                if (!xdgDataHomeEnv.isNullOrBlank()) {
                    File(xdgDataHomeEnv, "aidomari")
                } else {
                    File(File(userHome, ".local/share"), "aidomari")
                }
            }
        }
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return baseDir
    }

    fun databaseFile(): File = File(resolveAppDataDir(), "aidomari.db")
}
