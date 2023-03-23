package kiinse.me.oneconnect.plugin.exceptions

class CommandExceptions : OneConnectException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}