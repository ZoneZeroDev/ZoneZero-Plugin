package kiinse.me.zonezero.plugin.enums

enum class Strings(val value: String) {
    SPIGOT_URL("https://api.spigotmc.org/legacy/update.php?resource=108992"),
    VERSION_ERROR("Failed to get the latest version SpigotMC"),
    PLUGIN_PREFIX("&6&l[&bZoneZero&6&l]&f:"),
    MESSAGES_FILE("messages.toml"),
    DATA_FILE("data.zz"),
    VERSION_FILE("version-message.txt"),
    TMP_TOML_SUFFIX("_tmp.toml"),
    OLD_TOML_SUFFIX("_old.toml"),
    TOML_MISMATCH_MESSAGE("Version mismatch found for file '&c<FILE>&6'. This file has been renamed to '&c<OLD_FILE>&6' and a new file '&c<FILE>&6' has been created"),
    NEW_TML_VERSION_COPY_ERROR("An error occurred while copying the new version of the file '&c<FILE>&6'! Message:"),
    FILE_CREATED("File '&d<FILE>&6' created"),
    DIRECTORY_CREATED("Directory '&d<DIRECTORY>&6' created"),
    FILE_COPY_ERROR("Error on copying file '&c<FILE>&6'! Message:"),
    FILE_CREATE_ERROR("Error on creating file '<FILE>'! Message:"),
    FILE_NOT_FOUND_INSIDE_JAR("File '&c<FILE>&6' not found inside plugin jar. Creating a new file..."),
    DIRECTORY_COPY_ERROR("Error on copying directory '&c<DIRECTORY>&6'! Message:"),
    FILE_COPIED("File '&d<OLD_FILE>&6' copied to file '&d<FILE>&6'"),
    FILE_DELETED("File '&d<FILE>&6' deleted"),
    RSA_INSTANCE("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"),
    RSA_INSTANCE_SECOND("RSA"),
    AES_INSTANCE("AES"),
    API_KEY_ERROR("Something gone wrong with api server"),
    STRING_DATA("data"),
    STRING_MESSAGE("message"),
    NULL_CREDENTIALS("Credentials is null!"),
    NULL_SERVICE_TOKEN("Service token is null!"),
    DEFAULT_API("https://api.zonezero.dev"),
    HEADER_CONTENT_KEY("Content-Type"),
    HEADER_CONTENT_VALUE("application/json"),
    HEADER_PUBLIC_KEY("publicKey"),
    HEADER_AUTH("Authorization"),
    HEADER_ON_PL("onpl"),
    HEADER_PLAYER("player"),
    HEADER_SECURITY("security"),
    TOKEN_PREFIX("Bearer "),
    SERVER_KEY_UPDATED_MESSAGE("Server key updated!"),
    SERVER_KEY_UPDATE_ERROR("Error on updating server key! Message:"),
    SCHEDULER_ALREADY_EXISTS("Scheduler with same name '<SCHEDULER>' already exist!"),
    SCHEDULER_REGISTERED("Scheduler '&d<SCHEDULER>&6' by plugin '&d<PLUGIN>&6' has been registered!"),
    SCHEDULER_ALREADY_STARTED("This scheduler '<SCHEDULER>' already started!"),
    SCHEDULER_ALREADY_STOPPED("This scheduler '<SCHEDULER>' already stopped!"),
    SCHEDULER_NOT_FOUND("This scheduler '<SCHEDULER>' not found!"),
    SCHEDULER_UNREGISTERED("Scheduler '&d<SCHEDULER>&6' by plugin '&d<PLUGIN>&6' has been unregistered!"),
    SCHEDULER_STARTED("Scheduler '&d<SCHEDULER>&6' started!"),
    SCHEDULER_CANT_START("Scheduler '&d<SCHEDULER>&6' cannot be started because the '&dcanStart()&6' method returns &cfalse"),
    SCHEDULER_STOPPED("Scheduler '&d$<SCHEDULER>&6' stopped!"),
    ZONE_ZERO_TOP("&d=========== &6&l[&bZoneZero&6&l] &d===========\n"),
    COMMAND_USAGE_ERROR("Error on command usage! Message:"),
    STRING_NULL("null"),
    STATUSES_LOADING("Loading player statuses..."),
    STATUSES_LOADED("Player statuses has been loaded!"),
    COMMAND_REGISTERED("Command '&d<COMMAND>&6' registered!"),
    COMMAND_ALREADY_REGISTERED("Command '<COMMAND>' already registered!"),
    COMMAND_UNABLE_REGISTER("Unable to register command command '<COMMAND>'. Did you put it in plugin.yml?"),
    COMMAND_MAIN_CLASS_NOT_FOUND("Main command in class '<CLASS>' not found!"),
    PLUGIN_VERSION_CHECK_ERROR("&cError on checking plugin version!"),
    LATEST_VERSION("&aYou have the latest version of the plugin installed, well done!"),
    PREFIX_MESSAGE("&cMessage: "),
    ERROR_ON_GETTING_CODE("&cError on getting server code (Serverside)!"),
    SERVER_CORE_NOT_ALLOWED("&cServer core is not allowed!"),
    SENTRY_TOKEN("https://a95dac05fc644cc1b2bd70f05e0fc5b9@o1138855.ingest.sentry.io/4504871295582208")
}