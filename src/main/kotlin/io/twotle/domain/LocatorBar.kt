package io.twotle.domain

interface LocatorBar {
    fun synchronize(enabled: Boolean)
}

object NoOpLocatorBar : LocatorBar {
    override fun synchronize(enabled: Boolean) = Unit
}
