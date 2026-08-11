package io.twotle.presentation.command

import org.bukkit.command.CommandSender

internal data class CommandContext(
    val sender: CommandSender,
    val label: String,
    val arguments: List<String>,
)

internal interface CommandNode {
    val name: String
    val usage: String

    fun execute(context: CommandContext)

    fun suggest(arguments: List<String>): List<String> = emptyList()
}

internal class CompositeCommand(
    override val name: String,
    children: List<CommandNode>,
) : CommandNode {
    private val children = children.associateBy { it.name.lowercase() }

    override val usage: String = children.joinToString("\n") { it.usage }

    override fun execute(context: CommandContext) {
        val requestedName = context.arguments.firstOrNull()
            ?: throw CommandUsageException(usage)
        val child = children[requestedName.lowercase()]
            ?: throw CommandUsageException(usage)
        child.execute(context.copy(arguments = context.arguments.drop(1)))
    }

    override fun suggest(arguments: List<String>): List<String> {
        val prefix = arguments.firstOrNull().orEmpty()
        if (arguments.size <= 1) {
            return children.keys.filter { it.startsWith(prefix, ignoreCase = true) }.sorted()
        }
        return children[arguments.first().lowercase()]
            ?.suggest(arguments.drop(1))
            .orEmpty()
    }
}

internal abstract class ExactArgumentsCommand(
    final override val name: String,
    final override val usage: String,
    private val argumentCount: Int,
) : CommandNode {
    final override fun execute(context: CommandContext) {
        if (context.arguments.size != argumentCount) {
            throw CommandUsageException(usage)
        }
        executeExact(context)
    }

    protected open val suggestionProviders: Map<Int, (List<String>) -> List<String>> = emptyMap()

    final override fun suggest(arguments: List<String>): List<String> =
        suggestionProviders[arguments.size]?.invoke(arguments).orEmpty()

    protected abstract fun executeExact(context: CommandContext)

    protected fun matching(candidates: List<String>, prefix: String): List<String> =
        candidates.filter { it.startsWith(prefix, ignoreCase = true) }.sorted()
}

internal class CommandUsageException(usage: String) :
    RuntimeException("사용법:\n$usage")
