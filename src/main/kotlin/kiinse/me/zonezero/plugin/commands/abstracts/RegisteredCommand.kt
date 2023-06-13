package kiinse.me.zonezero.plugin.commands.abstracts

import java.lang.reflect.Method

@Suppress("UNUSED")
abstract class RegisteredCommand protected constructor(val method: Method?, val instance: Any, val annotation: Any?)