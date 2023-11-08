package com.calmwolfs.bettermap.commands

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.config.gui.ConfigGuiManager
import com.calmwolfs.bettermap.features.UsingBmCheck
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraftforge.client.ClientCommandHandler

object Commands {
    private val openConfig: (Array<String>) -> Unit = {
        if (it.isNotEmpty()) {
            ConfigGuiManager.openConfigGui(it.joinToString(" "))
        } else {
            ConfigGuiManager.openConfigGui()
        }
    }

    fun init() {
        registerCommand("bm", openConfig)
        registerCommand("bettermap", openConfig)

        registerCommand("bping") { UsingBmCheck.command(it) }
        registerCommand("bmdebugpacket") { UsingBmCheck.debugPacket() }

        registerCommand("bmupdaterepo") { BetterMapMod.repo.updateRepo() }
        registerCommand("bmreloadrepo") { BetterMapMod.repo.reloadRepo() }
        registerCommand("bmrepostatus") { BetterMapMod.repo.displayRepoStatus(false) }
    }

    private fun registerCommand(name: String, function: (Array<String>) -> Unit) {
        ClientCommandHandler.instance.registerCommand(SimpleCommand(name, createCommand(function)))
    }

    private fun registerCommand0(
        name: String,
        function: (Array<String>) -> Unit,
        autoComplete: ((Array<String>) -> List<String>) = { listOf() }
    ) {
        val command = SimpleCommand(
            name,
            createCommand(function),
            object : SimpleCommand.TabCompleteRunnable {
                override fun tabComplete(sender: ICommandSender?, args: Array<String>?, pos: BlockPos?): List<String> {
                    return autoComplete(args ?: emptyArray())
                }
            }
        )
        ClientCommandHandler.instance.registerCommand(command)
    }

    private fun createCommand(function: (Array<String>) -> Unit) = object : SimpleCommand.ProcessCommandRunnable() {
        override fun processCommand(sender: ICommandSender?, args: Array<String>?) {
            if (args != null) function(args.asList().toTypedArray())
        }
    }
}