package io.twotle.infrastructure

import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.TeamRepository
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class TeamAttackListener(
    private val teams: TeamRepository,
    private val configuration: ConfigurationRepository,
) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (configuration.teamAttackAllowed()) return

        val attacker = event.damager.playerSource() ?: return
        val victim = event.entity as? Player ?: return
        val team = teams.findByMember(attacker.uniqueId) ?: return
        if (team.contains(victim.uniqueId)) event.isCancelled = true
    }
}
