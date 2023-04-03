package kiinse.me.zonezero.plugin.exceptions

class SecureException : ZoneZeroException {

    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}