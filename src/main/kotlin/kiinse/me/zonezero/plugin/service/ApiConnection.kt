package kiinse.me.zonezero.plugin.service

import io.sentry.Sentry
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.exceptions.APIException
import kiinse.me.zonezero.plugin.exceptions.SecureException
import kiinse.me.zonezero.plugin.service.data.EncryptedMessage
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.KeyType
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.json.JSONObject
import org.tomlj.TomlTable
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.logging.Level
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


class ApiConnection(private val zoneZero: ZoneZero, configuration: TomlTable) : ApiService {

    private val line = "-----------------------"
    private val keys: MutableMap<KeyType, Key> = EnumMap(KeyType::class.java)
    private var publicKeyString: String = String()
    private val timeout = 10000
    private var serverKey: PublicKey
    private val serviceServer = configuration.getString(Config.TOOLS_CUSTOM_IP.value) { Strings.DEFAULT_API.value }
    private val token: String

    init {
        val config = zoneZero.configuration
        val credentials = config.getTable(Config.TABLE_CREDENTIALS.value) ?: throw APIException(Strings.NULL_CREDENTIALS.value)
        token = credentials.getString(Config.CREDENTIALS_TOKEN.value) ?: throw APIException(Strings.NULL_SERVICE_TOKEN.value)
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance(Strings.RSA_INSTANCE_SECOND.value)
            keyPairGenerator.initialize(2048)
            val keyPair = keyPairGenerator.generateKeyPair()
            val privateKey = keyPair.private
            val publicKey = keyPair.public
            serverKey = publicKey
            keys[KeyType.PRIVATE] = privateKey
            keys[KeyType.PUBLIC] = publicKey
            publicKeyString = Base64.encodeBase64String(publicKey.encoded)
        } catch (e: Exception) {
            ZoneZero.sendLog(Level.CONFIG, e)
            throw SecureException(e)
        }
    }

    override fun get(address: ServerAddress): ServerAnswer {
        return try {
            ZoneZero.sendLog(Level.CONFIG, line)
            ZoneZero.sendLog(Level.CONFIG, "Address: ${address.string}")
            val request = getRequestGet("$serviceServer/${address.string}", hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey))
            )).execute()
            getServerAnswer(request.returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(500, JSONObject())
        }
    }

    override fun get(address: ServerAddress, player: Player): ServerAnswer {
        return try {
            ZoneZero.sendLog(Level.CONFIG, line)
            ZoneZero.sendLog(Level.CONFIG, "Address: ${address.string}")
            val request = getRequestGet("$serviceServer/${address.string}", hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_PLAYER, getEncryptedPlayer(player, serverKey)),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey))
            )).execute()
            getServerAnswer(request.returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(500, JSONObject())
        }
    }

    @Throws(SecureException::class)
    override fun post(address: ServerAddress, data: JSONObject): ServerAnswer {
        return try {
            val encrypted = encrypt(data, serverKey)
            ZoneZero.sendLog(Level.CONFIG, line)
            ZoneZero.sendLog(Level.CONFIG, "Address: ${address.string}")
            val request = getRequestPost("$serviceServer/${address.string}", encrypted.message, hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_SECURITY, encrypted.aes ?: ""),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey))
            )).execute()
            getServerAnswer(request.returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(500, JSONObject())
        }
    }

    override fun post(address: ServerAddress, data: JSONObject, player: Player): ServerAnswer {
        return try {
            val encrypted = encrypt(data, serverKey)
            ZoneZero.sendLog(Level.CONFIG, line)
            ZoneZero.sendLog(Level.CONFIG, "Address: ${address.string}")
            val request = getRequestPost("$serviceServer/${address.string}", encrypted.message, hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_SECURITY, encrypted.aes ?: ""),
                Pair(Strings.HEADER_PLAYER, getEncryptedPlayer(player, serverKey)),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey))
            )).execute()
            getServerAnswer(request.returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(500, JSONObject())
        }
    }

    private fun getRequestGet(address: String, headers: Map<Strings, String>): Request  {
        val request = Request.Get(address)
        request.connectTimeout(timeout)
        request.socketTimeout(timeout)
        request.setHeader(Strings.HEADER_CONTENT_KEY.value, Strings.HEADER_CONTENT_VALUE.value)
        request.setHeader(Strings.HEADER_PUBLIC_KEY.value, publicKeyString)
        headers.forEach { (key, value) -> request.setHeader(key.value, value) }
        return request
    }

    private fun getRequestPost(address: String, body: String, headers: Map<Strings, String>): Request {
        val request = Request.Post(address)
        request.connectTimeout(timeout)
        request.socketTimeout(timeout)
        request.useExpectContinue()
        request.setHeader(Strings.HEADER_CONTENT_KEY.value, Strings.HEADER_CONTENT_VALUE.value)
        request.setHeader(Strings.HEADER_PUBLIC_KEY.value, publicKeyString)
        request.version(HttpVersion.HTTP_1_1)
        request.bodyString(body, ContentType.APPLICATION_JSON)
        headers.forEach { (key, value) -> request.setHeader(key.value, value) }
        return request
    }

    private fun getEncryptedPlayer(player: Player, publicKey: PublicKey): String {
        return encrypt(player.name, publicKey)
    }

    private fun getOnpl(publicKey: PublicKey): String {
        return encrypt(Bukkit.getServer().onlinePlayers.size.toString(), publicKey)
    }

    private fun getServerAnswer(response: HttpResponse): ServerAnswer {
        val content = if (response.entity != null) {
            try {
                IOUtils.toString(response.entity.content, StandardCharsets.UTF_8)
            } catch (e: Exception) { "" }
        } else { "" }
        val responseCode = response.statusLine.statusCode
        val body = getBody(getEncryptedMessage(response, content))
        val answer = ServerAnswer(responseCode, body)
        ZoneZero.sendLog(Level.CONFIG, "Status: ${answer.status}")
        ZoneZero.sendLog(Level.CONFIG, "Body: ${answer.data}")
        ZoneZero.sendLog(Level.CONFIG, line)
        return answer
    }

    private fun getEncryptedMessage(response: HttpResponse, content: String): EncryptedMessage {
        val security = getSecurityHeader(response.allHeaders)
        return try {
            EncryptedMessage(security, JSONObject(content).getString(Strings.STRING_DATA.value))
        } catch (e: Exception) {
            EncryptedMessage(security, content)
        }
    }

    private fun getSecurityHeader(allHeaders: Array<Header>): String {
        allHeaders.forEach { if (it.name == Strings.HEADER_SECURITY.value) return it.value }
        return ""
    }

    private fun getBody(encrypted: EncryptedMessage): JSONObject {
        if (encrypted.message.isEmpty()) return JSONObject()
        if (encrypted.aes.isNullOrEmpty()) return JSONObject(encrypted.message)
        return decrypt(encrypted)
    }

    @Throws(APIException::class)
    private fun decrypt(encrypted: EncryptedMessage): JSONObject = runBlocking {
        try {
            val cipher = Cipher.getInstance(Strings.RSA_INSTANCE.value)
            cipher.init(Cipher.DECRYPT_MODE, keys[KeyType.PRIVATE])
            val aesKey = cipher.doFinal(Base64.decodeBase64(encrypted.aes))
            val originalKey = SecretKeySpec(aesKey, 0, aesKey.size, Strings.AES_INSTANCE.value)
            val aesCipher = async {
                val aesCipher = Cipher.getInstance(Strings.AES_INSTANCE.value)
                aesCipher.init(Cipher.DECRYPT_MODE, originalKey)
                return@async aesCipher
            }
            val message = async { Base64.decodeBase64(encrypted.message) }
            return@runBlocking JSONObject(String(aesCipher.await().doFinal(message.await())))
        } catch (e: Exception) {
            throw APIException(e.message)
        }
    }

    @Throws(APIException::class)
    private fun encrypt(json: JSONObject, publicKey: PublicKey): EncryptedMessage = runBlocking {
        val generator = async {
            val generator = KeyGenerator.getInstance(Strings.AES_INSTANCE.value)
            generator.init(128)
            return@async generator
        }
        val aesKey: SecretKey = generator.await().generateKey()
        val aesCipher = async {
            val cipher = Cipher.getInstance(Strings.AES_INSTANCE.value)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey)
            return@async cipher
        }
        val cipher = async {
            val cipher = Cipher.getInstance(Strings.RSA_INSTANCE.value)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return@async cipher
        }
        val aes = async { Base64.encodeBase64String(cipher.await().doFinal(aesKey.encoded)) }
        val message = async { Base64.encodeBase64String(aesCipher.await().doFinal(json.toString().toByteArray())) }
        return@runBlocking EncryptedMessage(aes.await(), message.await())
    }

    @Throws(APIException::class)
    private fun encrypt(string: String, publicKey: PublicKey): String = runBlocking {
        val cipher = async {
            val cipher = Cipher.getInstance(Strings.RSA_INSTANCE.value)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return@async cipher
        }
        val message = async { string.toByteArray() }
        return@runBlocking Base64.encodeBase64String(cipher.await().doFinal(message.await()))
    }

    @Throws(SecureException::class)
    override fun updateServerKey() {
        try {
            ZoneZero.sendLog(Level.CONFIG, line)
            ZoneZero.sendLog(Level.CONFIG, "Address: server")
            val request = getRequestGet("$serviceServer/server", EnumMap(Strings::class.java)).execute()
            val answer = getServerAnswer(request.returnResponse())
            if (answer.status != 200) { throw SecureException(Strings.API_KEY_ERROR.value) }
            serverKey = KeyFactory.getInstance(Strings.RSA_INSTANCE_SECOND.value).generatePublic(
                X509EncodedKeySpec(Base64.decodeBase64(answer.data.getString(Strings.STRING_MESSAGE.value)))
            )
        } catch (e: Exception) {
            throw SecureException(e)
        }
    }
}