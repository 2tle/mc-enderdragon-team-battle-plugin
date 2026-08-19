package io.twotle.infrastructure

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import io.twotle.domain.Team
import io.twotle.domain.TeamSpawnDisplay
import io.twotle.domain.TeamSpawnLayout
import org.bukkit.Bukkit
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BukkitTeamSpawnDisplay : TeamSpawnDisplay, Listener {
    private val memberSpawns = ConcurrentHashMap<UUID, Location>()
    private val teamMembers = ConcurrentHashMap<String, Set<UUID>>()

    override fun synchronize(teams: List<Team>) {
        reset()
        teams.forEach(::update)
    }

    override fun update(team: Team) {
        val key = team.name.lowercase(Locale.ROOT)
        teamMembers.remove(key).orEmpty().forEach(memberSpawns::remove)
        val spawn = resolveSpawn(team) ?: return
        val memberIds = team.members.mapTo(mutableSetOf()) { it.uuid }
        teamMembers[key] = memberIds
        memberIds.forEach { memberSpawns[it] = spawn.clone() }
    }

    override fun remove(team: Team) {
        teamMembers
            .remove(team.name.lowercase(Locale.ROOT))
            .orEmpty()
            .forEach(memberSpawns::remove)
    }

    override fun reset() {
        memberSpawns.clear()
        teamMembers.clear()
    }

    @EventHandler
    fun onInitialSpawn(event: AsyncPlayerSpawnLocationEvent) {
        if (!event.isNewPlayer) return
        val uuid = event.connection.profile.id ?: return
        memberSpawns[uuid]?.clone()?.let(event::setSpawnLocation)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        if (event.isBedSpawn || event.isAnchorSpawn) return
        if (!event.isMissingRespawnBlock && event.player.getRespawnLocation(false) != null) return
        memberSpawns[event.player.uniqueId]?.clone()?.let(event::setRespawnLocation)
    }

    private fun resolveSpawn(team: Team): Location? {
        val world = Bukkit.getWorlds().firstOrNull { it.environment == World.Environment.NORMAL } ?: return null
        val worldSpawn = world.spawnLocation
        val borderCenter = world.worldBorder.center
        val offset = TeamSpawnLayout.offset(team.spawnIndex)
        val baseX = borderCenter.blockX + offset
        val baseZ = borderCenter.blockZ + offset

        candidateOffsets().forEach { (offsetX, offsetZ) ->
            safeSpawnAt(world, baseX + offsetX, baseZ + offsetZ, worldSpawn)?.let { return it }
        }

        return surfaceSpawnAt(world, baseX, baseZ, worldSpawn)
            .takeIf(world.worldBorder::isInside)
            ?: worldSpawn
    }

    private fun safeSpawnAt(
        world: World,
        x: Int,
        z: Int,
        orientation: Location,
    ): Location? {
        val highest = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES)
        val y = highest.y + 1
        if (y + 1 >= world.maxHeight) return null
        val ground = world.getBlockAt(x, y - 1, z)
        val feet = world.getBlockAt(x, y, z)
        val head = world.getBlockAt(x, y + 1, z)
        if (!ground.type.isSolid || ground.isLiquid || !feet.isPassable || !head.isPassable) return null

        return Location(world, x + 0.5, y.toDouble(), z + 0.5, orientation.yaw, orientation.pitch)
            .takeIf(world.worldBorder::isInside)
    }

    private fun surfaceSpawnAt(
        world: World,
        x: Int,
        z: Int,
        orientation: Location,
    ): Location {
        val y = world.getHighestBlockAt(x, z, HeightMap.WORLD_SURFACE).y + 1.0
        return Location(world, x + 0.5, y, z + 0.5, orientation.yaw, orientation.pitch)
    }

    private fun candidateOffsets(): Sequence<Pair<Int, Int>> =
        sequence {
            yield(0 to 0)
            for (radius in SEARCH_STEP..TeamSpawnLayout.SEARCH_RADIUS step SEARCH_STEP) {
                for (offset in -radius..radius step SEARCH_STEP) {
                    yield(offset to -radius)
                    yield(offset to radius)
                    if (offset != -radius && offset != radius) {
                        yield(-radius to offset)
                        yield(radius to offset)
                    }
                }
            }
        }

    private companion object {
        const val SEARCH_STEP = 4
    }
}
