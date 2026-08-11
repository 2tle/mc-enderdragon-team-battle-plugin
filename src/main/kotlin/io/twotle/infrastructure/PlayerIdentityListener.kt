package io.twotle.infrastructure

import io.twotle.application.TeamService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerIdentityListener(
    private val teamService: TeamService,
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        teamService.refreshMemberIdentity(event.player.uniqueId, event.player.name)
    }
}
