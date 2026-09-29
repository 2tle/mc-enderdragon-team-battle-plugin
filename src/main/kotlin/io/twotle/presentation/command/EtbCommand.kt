package io.twotle.presentation.command

import io.twotle.application.AdministratorService
import io.twotle.application.AdministratorServiceException
import io.twotle.application.InvalidGameTransition
import io.twotle.application.TeamServiceException
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

internal class EtbCommand(
    private val root: CommandNode,
    private val administrators: AdministratorService,
    private val teamErrorMessages: TeamErrorMessageResolver = TeamErrorMessageResolver(),
    private val gameErrorMessages: GameErrorMessageResolver = GameErrorMessageResolver(),
) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!sender.canUseEnderTeamBattle()) {
            sender.error("You do not have permission to use this command.")
            return true
        }

        try {
            root.execute(CommandContext(sender, label, args.toList()))
        } catch (exception: CommandUsageException) {
            sender.error(requireNotNull(exception.message))
        } catch (exception: TeamServiceException) {
            sender.error(teamErrorMessages.resolve(exception))
        } catch (exception: InvalidGameTransition) {
            sender.error(gameErrorMessages.resolve(exception))
        } catch (exception: AdministratorServiceException) {
            sender.error(requireNotNull(exception.message))
        } catch (_: AdministratorManagementPermissionException) {
            sender.error("Only server operators can manage EnderTeamBattle administrators.")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> = sender
        .takeIf { it.canUseEnderTeamBattle() }
        ?.let { root.suggest(args.toList()) }
        .orEmpty()

    private fun CommandSender.canUseEnderTeamBattle(): Boolean =
        isOp ||
            this is ConsoleCommandSender ||
            hasPermission(PERMISSION) ||
            (this is Player && administrators.hasAccess(uniqueId))

    companion object {
        const val PERMISSION = "enderteambattle.admin"
    }
}
