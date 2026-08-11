package io.twotle.presentation.command

import io.twotle.application.GameService

internal class StartGameCommand(
    private val service: GameService,
) : ExactArgumentsCommand("start", "/etb start", 0) {
    override fun executeExact(context: CommandContext) = service.start()
}

internal class PauseGameCommand(
    private val service: GameService,
) : ExactArgumentsCommand("pause", "/etb pause", 0) {
    override fun executeExact(context: CommandContext) = service.pause()
}

internal class StopGameCommand(
    private val service: GameService,
) : ExactArgumentsCommand("stop", "/etb stop", 0) {
    override fun executeExact(context: CommandContext) = service.stop()
}
