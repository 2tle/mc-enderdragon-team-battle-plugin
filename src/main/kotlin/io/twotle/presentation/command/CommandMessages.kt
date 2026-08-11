package io.twotle.presentation.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

internal fun CommandSender.success(message: String) =
    sendMessage(Component.text(message, NamedTextColor.GREEN))

internal fun CommandSender.error(message: String) =
    sendMessage(Component.text(message, NamedTextColor.RED))

internal fun CommandSender.info(message: String) =
    sendMessage(Component.text(message, NamedTextColor.YELLOW))
