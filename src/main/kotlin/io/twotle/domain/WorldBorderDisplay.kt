package io.twotle.domain

interface WorldBorderDisplay {
    fun synchronize(radius: Int)
}

object NoOpWorldBorderDisplay : WorldBorderDisplay {
    override fun synchronize(radius: Int) = Unit
}
