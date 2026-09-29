package io.twotle.presentation.command

import io.twotle.application.AdministratorService
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

internal class AddAdministratorCommand(
    private val service: AdministratorService,
) : ExactArgumentsCommand("add", "/etb admin add <username>", 1) {
    override fun executeExact(context: CommandContext) {
        context.sender.requireAdministratorManager()
        val administrator = service.add(context.arguments.single())
        context.sender.success("Granted EnderTeamBattle administrator access to '${administrator.username}'.")
    }

    override val suggestionProviders =
        mapOf<Int, (List<String>) -> List<String>>(
            1 to { arguments -> matching(service.onlineUsernames(), arguments[0]) },
        )
}

internal class RemoveAdministratorCommand(
    private val service: AdministratorService,
) : ExactArgumentsCommand("remove", "/etb admin remove <username>", 1) {
    override fun executeExact(context: CommandContext) {
        context.sender.requireAdministratorManager()
        val administrator = service.remove(context.arguments.single())
        context.sender.success("Revoked EnderTeamBattle administrator access from '${administrator.username}'.")
    }

    override val suggestionProviders =
        mapOf<Int, (List<String>) -> List<String>>(
            1 to { arguments -> matching(service.usernames(), arguments[0]) },
        )
}

internal class ListAdministratorsCommand(
    private val service: AdministratorService,
) : ExactArgumentsCommand("list", "/etb admin list", 0) {
    override fun executeExact(context: CommandContext) {
        context.sender.requireAdministratorManager()
        val administrators = service.list()
        val message = administrators
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n", prefix = "Administrators (${administrators.size}):\n") {
                "- ${it.username} (${it.uuid})"
            }
            ?: "No delegated administrators have been added."
        context.sender.info(message)
    }
}

internal class AdministratorManagementPermissionException : RuntimeException()

private fun CommandSender.requireAdministratorManager() {
    if (!isOp && this !is ConsoleCommandSender) throw AdministratorManagementPermissionException()
}
