package io.twotle.domain

interface ConfigurationRepository {
    fun initialize()
    fun reset()
}
