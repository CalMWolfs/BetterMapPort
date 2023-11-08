package com.calmwolfs.bettermap.config.gui

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.config.ConfigManager
import io.github.moulberry.moulconfig.gui.GuiScreenElementWrapper
import io.github.moulberry.moulconfig.gui.MoulConfigEditor

object ConfigGuiManager {
    val editor by lazy { MoulConfigEditor(ConfigManager.processor) }
    fun openConfigGui(search: String? = null) {
        if (search != null) {
            editor.search(search)
        }
        BetterMapMod.screenToOpen = GuiScreenElementWrapper(editor)
    }
}