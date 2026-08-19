package io.twotle.domain

interface PhantomSpawn {
    fun synchronize(allowed: Boolean)
}

object NoOpPhantomSpawn : PhantomSpawn {
    override fun synchronize(allowed: Boolean) = Unit
}
