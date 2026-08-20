package io.twotle.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object TeamSpawnLayout {
    const val SPACING = 550
    const val SEARCH_RADIUS = 20

    fun position(
        slot: Int,
        teamCount: Int,
    ): SpawnPoint {
        require(teamCount > 0) { "Team count must be positive." }
        require(slot in 0 until teamCount) { "Team slot must be within the team count." }
        if (teamCount == 1) return SpawnPoint(0, 0)

        val radius = SPACING / (2.0 * sin(PI / teamCount))
        val angle = -PI / 2.0 + 2.0 * PI * slot / teamCount
        return SpawnPoint(
            x = (radius * cos(angle)).roundToInt(),
            z = (radius * sin(angle)).roundToInt(),
        )
    }

    fun requiredBorderRadius(teamCount: Int): Int {
        if (teamCount == 0) return 0
        val farthestCoordinate =
            (0 until teamCount)
                .map { position(it, teamCount) }
                .maxOf { maxOf(abs(it.x), abs(it.z)) }
        return farthestCoordinate + SEARCH_RADIUS + 1
    }
}

data class SpawnPoint(
    val x: Int,
    val z: Int,
)
