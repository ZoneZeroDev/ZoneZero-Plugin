package kiinse.me.oneconnect.plugin.commands.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class SubCommand(
        val command: String,
        val parameters: Int = 0,
        val overrideParameterLimit: Boolean = false,
        val disallowNonPlayer: Boolean = false,
        val permission: String = "")