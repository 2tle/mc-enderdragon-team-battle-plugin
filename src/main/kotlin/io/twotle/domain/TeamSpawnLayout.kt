package io.twotle.domain

import kotlin.math.abs

object TeamSpawnLayout {
    const val SPACING = 550
    const val SEARCH_RADIUS = 20

    fun offset(spawnIndex: Int): Int {
        if (spawnIndex == 0) return 0
        val distance = ((spawnIndex + 1) / 2) * SPACING
        return if (spawnIndex % 2 == 1) distance else -distance
    }

    fun requiredBorderRadius(spawnIndex: Int): Int = abs(offset(spawnIndex)) + SEARCH_RADIUS + 1
}
