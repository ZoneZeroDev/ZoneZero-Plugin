package kiinse.me.zonezero.plugin.commands.abstracts

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.annotations.SubCommand
import kiinse.me.zonezero.plugin.commands.core.CommandFailureHandler
import kiinse.me.zonezero.plugin.commands.enums.CommandFailReason
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandFailureHandler
import kiinse.me.zonezero.plugin.enums.Replace
import kiinse.me.zonezero.plugin.enums.Strings
import org.bukkit.command.CommandException
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Player
import java.lang.reflect.Method
import java.util.logging.Level

@Suppress("UNUSED")
abstract class MineCommandManager protected constructor(protected val plugin: ZoneZero) : CommandExecutor {
    protected val failureHandler: MineCommandFailureHandler = CommandFailureHandler(plugin)
    protected val registeredSubCommandTable: MutableMap<String, RegisteredCommand> = HashMap()
    protected val registeredMainCommandTable: MutableMap<String, RegisteredCommand> = HashMap()
    protected val mainCommandTable: MutableMap<MineCommand, String> = HashMap()

    @Throws(CommandException::class)
    abstract fun registerCommand(commandClass: MineCommand): MineCommandManager

    @Throws(CommandException::class)
    protected fun registerMainCommand(commandClass: MineCommand, method: Method): String {
        val mainCommand = method.getAnnotation(Command::class.java)
        val command = mainCommand.command
        register(commandClass, method, plugin.server.getPluginCommand(command), command, mainCommand, true)
        return command
    }

    @Throws(CommandException::class)
    protected fun registerMainCommand(mineCommand: MineCommand): String {
        val mainCommand = mineCommand.javaClass.getAnnotation(Command::class.java)
        val command = mainCommand.command
        register(mineCommand, plugin.server.getPluginCommand(command), mainCommand)
        return command
    }

    @Throws(CommandException::class)
    protected fun registerSubCommand(commandClass: MineCommand, method: Method) {
        val annotation = method.getAnnotation(SubCommand::class.java)
        val mainCommand = mainCommandTable[commandClass]
        if (annotation != null && annotation.command != mainCommand) {
            val cmd = mainCommand + " " + annotation.command
            register(commandClass, method, plugin.server.getPluginCommand(cmd), cmd, annotation, false)
        }
    }

    @Throws(CommandException::class)
    protected fun register(commandClass: MineCommand, method: Method, pluginCommand: PluginCommand?, command: String, annotation: Any, isMainCommand: Boolean) {
        register(pluginCommand, command)
        (if (isMainCommand) registeredMainCommandTable else registeredSubCommandTable)[command] = object : RegisteredCommand(method, commandClass, annotation) {}
        ZoneZero.sendLog(Level.CONFIG, Strings.COMMAND_REGISTERED.value.replace(Replace.COMMAND.value, command, ignoreCase = true))
    }

    @Throws(CommandException::class)
    protected fun register(commandClass: MineCommand, pluginCommand: PluginCommand?, annotation: Command) {
        val command = annotation.command
        register(pluginCommand, command)
        registeredMainCommandTable[command] = object : RegisteredCommand(null, commandClass, annotation) {}
    }

    @Throws(CommandException::class)
    protected fun register(pluginCommand: PluginCommand?, command: String) {
        if (registeredSubCommandTable.containsKey(command) || registeredMainCommandTable.containsKey(command))
            throw CommandException(Strings.COMMAND_ALREADY_REGISTERED.value.replace(Replace.COMMAND.value, command, ignoreCase = true))
        if (pluginCommand == null) throw CommandException(Strings.COMMAND_UNABLE_REGISTER.value.replace(Replace.SCHEDULER.value, command, ignoreCase = true))
        pluginCommand.setExecutor(this)
    }

    @Throws(CommandException::class)
    protected fun getMainCommandMethod(mineCommand: Class<out MineCommand?>): Method {
        mineCommand.methods.forEach { if (it.getAnnotation(Command::class.java) != null) return it }
        val name = mineCommand.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        throw CommandException(Strings.COMMAND_MAIN_CLASS_NOT_FOUND.value.replace(Replace.CLASS.value, name[name.size - 1], ignoreCase = true))
    }

    protected fun isDisAllowNonPlayer(sender: CommandSender, disAllowNonPlayer: Boolean): Boolean {
        val result = sender !is Player && disAllowNonPlayer
        if (result) {
            failureHandler.handleFailure(CommandFailReason.NOT_PLAYER, sender)
        }
        return result
    }

    protected fun hasNotPermissions(sender: CommandSender, permission: String): Boolean {
        val result = permission != "" && !sender.hasPermission(permission)
        if (result) {
            failureHandler.handleFailure(CommandFailReason.NO_PERMISSION, sender)
        }
        return result
    }

    override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        return onExecute(sender, command, args)
    }

    protected abstract fun onExecute(sender: CommandSender, command: org.bukkit.command.Command, args: Array<String>): Boolean
}