package io.twotle.presentation.command

import io.twotle.application.GameService
import io.twotle.application.TeamService

internal class InitCommand(
    private val service: TeamService,
) : ExactArgumentsCommand("init", "/etb init", 0) {
    override fun executeExact(context: CommandContext) {
        service.reset()
        context.sender.success("EnderTeamBattle configuration and team data have been reset.")
    }
}

internal class CreateTeamCommand(
    private val service: TeamService,
) : ExactArgumentsCommand("create", "/etb team create <teamname> <color>", 2) {
    override fun executeExact(context: CommandContext) {
        val (teamName, colorName) = context.arguments
        service.create(teamName, colorName)
        context.sender.success("Created team '$teamName' with the color '$colorName'.")
    }

    override val suggestionProviders = mapOf<Int, (List<String>) -> List<String>>(
        2 to { arguments -> matching(service.colorNames(), arguments[1]) },
    )
}

internal class JoinTeamCommand(
    private val service: TeamService,
) : ExactArgumentsCommand("join", "/etb team join <teamname> <username>", 2) {
    override fun executeExact(context: CommandContext) {
        val (teamName, username) = context.arguments
        service.join(teamName, username)
        context.sender.success("Added '$username' to team '$teamName'.")
    }

    override val suggestionProviders = mapOf<Int, (List<String>) -> List<String>>(
        1 to { arguments -> matching(service.teamNames(), arguments[0]) },
        2 to { arguments -> matching(service.onlineUsernames(), arguments[1]) },
    )
}

internal class KickTeamCommand(
    private val service: TeamService,
) : ExactArgumentsCommand("kick", "/etb team kick <teamname> <username>", 2) {
    override fun executeExact(context: CommandContext) {
        val (teamName, username) = context.arguments
        service.kick(teamName, username)
        context.sender.success("Removed '$username' from team '$teamName'.")
    }

    override val suggestionProviders = mapOf<Int, (List<String>) -> List<String>>(
        1 to { arguments -> matching(service.teamNames(), arguments[0]) },
        2 to { arguments -> matching(service.memberNames(arguments[0]), arguments[1]) },
    )
}

internal class DeleteTeamCommand(
    private val service: TeamService,
) : ExactArgumentsCommand("delete", "/etb team delete <teamname>", 1) {
    override fun executeExact(context: CommandContext) {
        val teamName = context.arguments[0]
        service.delete(teamName)
        context.sender.success("Deleted team '$teamName'. All members are now unassigned.")
    }

    override val suggestionProviders = mapOf<Int, (List<String>) -> List<String>>(
        1 to { arguments -> matching(service.teamNames(), arguments[0]) },
    )
}

internal class ListTeamCommand(
    private val service: TeamService,
) : ExactArgumentsCommand("list", "/etb team list", 0) {
    override fun executeExact(context: CommandContext) {
        val teams = service.list()
        val message = teams.takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n",
                prefix = "Teams (${teams.size}):\n",
            ) { "- ${it.name} [${it.color.commandName}] (${it.memberCount} members)" }
            ?: "No teams have been created."
        context.sender.info(message)
    }
}

internal fun createCommandTree(
    teamService: TeamService,
    gameService: GameService,
): CommandNode =
    CompositeCommand(
        name = "etb",
        children = listOf(
            InitCommand(teamService),
            StartGameCommand(gameService),
            PauseGameCommand(gameService),
            StopGameCommand(gameService),
            CompositeCommand(
                name = "team",
                children = listOf(
                    CreateTeamCommand(teamService),
                    JoinTeamCommand(teamService),
                    KickTeamCommand(teamService),
                    DeleteTeamCommand(teamService),
                    ListTeamCommand(teamService),
                ),
            ),
        ),
    )
