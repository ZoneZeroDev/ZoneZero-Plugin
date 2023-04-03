package kiinse.me.zonezero.plugin.exceptions

class VersioningException : ZoneZeroException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}