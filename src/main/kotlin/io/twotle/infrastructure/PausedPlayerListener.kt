package io.twotle.infrastructure

import io.twotle.application.GameService
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.projectiles.ProjectileSource

class PausedPlayerListener(
    private val gameService: GameService,
) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
        if (event.hasChangedPosition()) cancelIfFrozen(event, event.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) = cancelIfFrozen(event, event.player)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) = cancelIfFrozen(event, event.player)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) = cancelIfFrozen(event, event.player)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) = cancelIfFrozen(event, event.player)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDropItem(event: PlayerDropItemEvent) = cancelIfFrozen(event, event.player)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPickupItem(event: EntityPickupItemEvent) {
        (event.entity as? Player)?.let { cancelIfFrozen(event, it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        (event.whoClicked as? Player)?.let { cancelIfFrozen(event, it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDamage(event: EntityDamageEvent) {
        (event.entity as? Player)?.let { cancelIfFrozen(event, it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onAttack(event: EntityDamageByEntityEvent) {
        event.damager.playerSource()?.let { cancelIfFrozen(event, it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        (event.entity as? Player)?.let { cancelIfFrozen(event, it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onConsume(event: PlayerItemConsumeEvent) = cancelIfFrozen(event, event.player)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSwapHands(event: PlayerSwapHandItemsEvent) = cancelIfFrozen(event, event.player)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onShootBow(event: EntityShootBowEvent) {
        (event.entity as? Player)?.let { cancelIfFrozen(event, it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        event.entity.shooter
            .playerSource()
            ?.let { cancelIfFrozen(event, it) }
    }

    private fun cancelIfFrozen(
        event: Cancellable,
        player: Player,
    ) {
        if (gameService.isFrozenParticipant(player.uniqueId)) event.isCancelled = true
    }
}

internal fun Any?.playerSource(): Player? =
    when (this) {
        is Player -> this
        is Projectile -> shooter as? Player
        is ProjectileSource -> this as? Player
        else -> null
    }
