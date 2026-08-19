package io.twotle.infrastructure

import io.twotle.domain.PlayerDirectory
import io.twotle.domain.TeamMember
import org.bukkit.Bukkit

class BukkitPlayerDirectory : PlayerDirectory {
    override fun findByUsername(username: String): TeamMember? {
        Bukkit.getPlayerExact(username)?.let {
            return TeamMember(it.uniqueId, it.name)
        }

        Bukkit.getOfflinePlayers()
            .firstOrNull { it.name?.equals(username, ignoreCase = true) == true }
            ?.let { return TeamMember(it.uniqueId, it.name ?: username) }

        // Also resolve profiles that have never joined this server. Paper may perform
        // a profile lookup here so the UUID still matches when the player first joins.
        return Bukkit.getOfflinePlayer(username).let {
            TeamMember(it.uniqueId, it.name ?: username)
        }
    }

    override fun onlineUsernames(): List<String> =
        Bukkit.getOnlinePlayers().map { it.name }.sorted()
}
