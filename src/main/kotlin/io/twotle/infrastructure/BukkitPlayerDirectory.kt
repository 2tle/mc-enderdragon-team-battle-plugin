package io.twotle.infrastructure

import io.twotle.domain.PlayerDirectory
import io.twotle.domain.TeamMember
import org.bukkit.Bukkit

class BukkitPlayerDirectory : PlayerDirectory {
    override fun findByUsername(username: String): TeamMember? {
        Bukkit.getPlayerExact(username)?.let {
            return TeamMember(it.uniqueId, it.name)
        }

        return Bukkit.getOfflinePlayers()
            .firstOrNull { it.name?.equals(username, ignoreCase = true) == true }
            ?.let { TeamMember(it.uniqueId, it.name ?: username) }
    }

    override fun onlineUsernames(): List<String> =
        Bukkit.getOnlinePlayers().map { it.name }.sorted()
}
