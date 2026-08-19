package io.twotle.domain

interface ConfigurationRepository {
    fun initialize()

    fun reset()

    fun teamAttackAllowed(): Boolean

    fun saveTeamAttackAllowed(allowed: Boolean)

    fun locatorBarEnabled(): Boolean

    fun saveLocatorBarEnabled(enabled: Boolean)

    fun phantomSpawnAllowed(): Boolean

    fun savePhantomSpawnAllowed(allowed: Boolean)

    fun worldBorderRadius(): Int

    fun saveWorldBorderRadius(radius: Int)
}
