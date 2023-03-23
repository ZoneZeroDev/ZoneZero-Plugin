package kiinse.me.oneconnect.plugin.commands.core

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.commands.annotations.Command
import kiinse.me.oneconnect.plugin.commands.annotations.SubCommand
import kiinse.me.oneconnect.plugin.commands.enums.CommandFailReason
import kiinse.me.oneconnect.plugin.commands.abstracts.MineCommand
import kiinse.me.oneconnect.plugin.commands.abstracts.MineCommandManager
import kiinse.me.oneconnect.plugin.commands.abstracts.RegisteredCommand
import org.bukkit.command.CommandException
import org.bukkit.command.CommandSender
import java.util.*
import java.util.logging.Level

@Suppress("unused")
open class CommandManager(plugin: OneConnect) : MineCommandManager(plugin) {

    @Throws(CommandException::class)
    override fun registerCommand(commandClass: MineCommand): MineCommandManager {
        val clazz = commandClass.javaClass
        mainCommandTable[commandClass] = if (clazz.getAnnotation(Command::class.java) != null)
            registerMainCommand(commandClass) else registerMainCommand(commandClass, getMainCommandMethod(clazz))
        clazz.methods.forEach { registerSubCommand(commandClass, it) }
        return this
    }

    override fun onExecute(sender: CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        return if (isMainCommand(command, args)) execute(sender, executeMainCommand(sender, command, args)) else execute(sender, executeSubCommand(sender, command, args))
    }

    private fun execute(sender: CommandSender, value: Boolean): Boolean {
        if (!value) failureHandler.handleFailure(CommandFailReason.COMMAND_NOT_FOUND, sender, null)
        return true
    }

    private fun isMainCommand(command: org.bukkit.command.Command, args: Array<String>): Boolean {
        registeredMainCommandTable.forEach { (_, registeredCommand) ->
            if (registeredCommand.method != null) {
                val annotation: Command? = registeredCommand.annotation as Command?
                if (annotation != null && annotation.command.equals(command.name, ignoreCase = true)) {
                    if (args.isNotEmpty() && isSubCommand(command, args)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun isSubCommand(command: org.bukkit.command.Command, args: Array<String>): Boolean {
        return registeredSubCommandTable.containsKey(StringBuilder(command.name.lowercase(Locale.getDefault())).append(" ").append(args[0].lowercase(Locale.getDefault())).toString())
    }

    private fun executeMainCommand(sender: CommandSender, command: org.bukkit.command.Command, args: Array<String>): Boolean {
        registeredMainCommandTable.forEach { (_, registeredCommand) ->
            if (registeredCommand.method != null) {
                val annotation: Command? = registeredCommand.annotation as Command?
                if (annotation != null && annotation.command.equals(command.name, ignoreCase = true)) {
                    if (isDisAllowNonPlayer(registeredCommand, sender, annotation.disallowNonPlayer)
                        || hasNotPermissions(registeredCommand, sender, annotation.permission)) return true
                    if (args.size != annotation.parameters && !annotation.overrideParameterLimit) {
                        if (args.size > annotation.parameters)
                            failureHandler.handleFailure(CommandFailReason.REDUNDANT_PARAMETER, sender, registeredCommand)
                        else
                            failureHandler.handleFailure(CommandFailReason.INSUFFICIENT_PARAMETER, sender, registeredCommand)
                        return true
                    }
                    invokeWrapper(registeredCommand, sender, args)
                    return true
                }
            }
        }
        return false
    }

    private fun executeSubCommand(sender: CommandSender, command: org.bukkit.command.Command, args: Array<String>): Boolean {
        val sb = StringBuilder(command.name.lowercase(Locale.getDefault()))
        args.forEach {
            sb.append(" ").append(it.lowercase(Locale.getDefault()))
            registeredSubCommandTable.forEach { (str, registeredCommand) ->
                if (str == sb.toString()) {
                    val annotation: SubCommand? = registeredCommand.annotation as SubCommand?
                    if (annotation != null) {
                        val actualParams = args.copyOfRange(annotation.command.split(" ").size, args.size)
                        if (isDisAllowNonPlayer(registeredCommand, sender, annotation.disallowNonPlayer)
                            || hasNotPermissions(registeredCommand, sender, annotation.permission)) return true
                        if (actualParams.size != annotation.parameters && !annotation.overrideParameterLimit) {
                            if (actualParams.size > annotation.parameters)
                                failureHandler.handleFailure(CommandFailReason.REDUNDANT_PARAMETER, sender, registeredCommand)
                            else
                                failureHandler.handleFailure(CommandFailReason.INSUFFICIENT_PARAMETER, sender, registeredCommand)
                            return true
                        }
                        invokeWrapper(registeredCommand, sender, actualParams)
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun invokeWrapper(wrapper: RegisteredCommand, sender: CommandSender, args: Array<String>) {
        try {
            wrapper.method?.invoke(wrapper.instance, CommandContext(sender, args))
        } catch (e: Exception) {
            failureHandler.handleFailure(CommandFailReason.REFLECTION_ERROR, sender, wrapper)
            OneConnect.sendLog(Level.WARNING, "Error on command usage! Message:", e)
        }
    }
}