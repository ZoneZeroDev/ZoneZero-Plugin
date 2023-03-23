package kiinse.me.oneconnect.plugin.commands.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Command(
        val command: String = "",
        val disallowNonPlayer: Boolean = false,
        val permission: String = "",
        val parameters: Int = 0,
        val overrideParameterLimit: Boolean = false)