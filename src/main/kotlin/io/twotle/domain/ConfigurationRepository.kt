package io.twotle.domain

interface ConfigurationRepository {
    fun initialize()

    fun reset()

    fun teamAttackAllowed(): Boolean

    fun saveTeamAttackAllowed(allowed: Boolean)
}
