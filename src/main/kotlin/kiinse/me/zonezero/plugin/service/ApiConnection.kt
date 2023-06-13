package kiinse.me.zonezero.plugin.service

import io.sentry.Sentry
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.config.TomlTable
import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.exceptions.APIException
import kiinse.me.zonezero.plugin.exceptions.SecureException
import kiinse.me.zonezero.plugin.service.data.DataAnswer
import kiinse.me.zonezero.plugin.service.data.EncryptedMessage
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.KeyType
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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

    private val serviceServer = configuration.get<String>(ConfigKey.TOOLS_CUSTOM_IP) { Strings.DEFAULT_API.value }

    init {
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
            val strAddress = address.string
            val request = getRequestGet("$serviceServer/${strAddress}", hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey))
                                                                                 )).execute()
            getServerAnswer(strAddress, request.returnResponse(), true)
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(address.string, 500, "")
        }
    }

    override fun get(address: ServerAddress, player: Player): ServerAnswer {
        return try {
            val strAddress = address.string
            val request = getRequestGet("$serviceServer/${strAddress}", hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_PLAYER, getEncryptedPlayer(player, serverKey)),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey))
                                                                                 )).execute()
            getServerAnswer(strAddress, request.returnResponse(), true)
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(address.string, 500, "")
        }
    }

    @Throws(SecureException::class)
    override fun <T> post(address: ServerAddress, strategy: SerializationStrategy<T>, value: T): ServerAnswer {
        return try {
            val strAddress = address.string
            val encrypted = encrypt(Json.encodeToString(strategy, value), serverKey)
            val request = getRequestPost("$serviceServer/${strAddress}", encrypted.message, hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_SECURITY, encrypted.aes ?: ""),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey)))).execute()
            getServerAnswer(strAddress, request.returnResponse(), true)
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(address.string, 500, "")
        }
    }

    override fun <T> post(address: ServerAddress, strategy: SerializationStrategy<T>, value: T, player: Player): ServerAnswer {
        return try {
            val strAddress = address.string
            val encrypted = encrypt(Json.encodeToString(strategy, value), serverKey)
            val request = getRequestPost("$serviceServer/${strAddress}", encrypted.message, hashMapOf(
                Pair(Strings.HEADER_AUTH, "${Strings.TOKEN_PREFIX.value}${zoneZero.token}"),
                Pair(Strings.HEADER_SECURITY, encrypted.aes ?: ""),
                Pair(Strings.HEADER_PLAYER, getEncryptedPlayer(player, serverKey)),
                Pair(Strings.HEADER_ON_PL, getOnpl(serverKey)))).execute()
            getServerAnswer(strAddress, request.returnResponse(), true)
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            ServerAnswer(address.string, 500, "")
        }
    }

    private fun getRequestGet(address: String, headers: Map<Strings, String>): Request {
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
        return encryptRsa(player.name, publicKey)
    }


    private fun getOnpl(publicKey: PublicKey): String {
        return encryptRsa(Bukkit.getServer().onlinePlayers.size.toString(), publicKey)
    }

    private fun getServerAnswer(address: String, response: HttpResponse, isDebug: Boolean): ServerAnswer {
        val content = if (response.entity != null) {
            try {
                val body = IOUtils.toString(response.entity.content, StandardCharsets.UTF_8)
                try {
                    Json.decodeFromString<DataAnswer>(body).data ?: ""
                } catch (e: Exception) {
                    body
                }
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
        val responseCode = response.statusLine.statusCode
        val body = getBody(getEncryptedMessage(response, content))
        val answer = ServerAnswer(address, responseCode, body)
        if (isDebug) {
            ZoneZero.sendLog(Level.CONFIG, line)
            ZoneZero.sendLog(Level.CONFIG, "Address: $address")
            ZoneZero.sendLog(Level.CONFIG, "Status: ${answer.status}")
            ZoneZero.sendLog(Level.CONFIG, "Body: ${answer.data}")
            ZoneZero.sendLog(Level.CONFIG, line)
        }
        return answer
    }

    private fun getEncryptedMessage(response: HttpResponse, content: String): EncryptedMessage {
        val security = getSecurityHeader(response.allHeaders)
        return EncryptedMessage(security, content)
    }

    private fun getSecurityHeader(allHeaders: Array<Header>): String {
        allHeaders.forEach { if (it.name == Strings.HEADER_SECURITY.value) return it.value }
        return ""
    }

    private fun getBody(encrypted: EncryptedMessage): String {
        if (encrypted.message.isEmpty()) return ""
        if (encrypted.aes.isNullOrEmpty()) return encrypted.message
        return decrypt(encrypted)
    }

    @Throws(APIException::class)
    private fun decrypt(encrypted: EncryptedMessage): String = runBlocking {
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
            return@runBlocking String(aesCipher.await().doFinal(message.await()))
        } catch (e: Exception) {
            throw APIException(e.message)
        }
    }

    @Throws(APIException::class)
    private fun encrypt(text: String, publicKey: PublicKey): EncryptedMessage = runBlocking {
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
        val message = async { Base64.encodeBase64String(aesCipher.await().doFinal(text.toByteArray())) }
        return@runBlocking EncryptedMessage(aes.await(), message.await())
    }

    @Throws(APIException::class)
    private fun encryptRsa(string: String, publicKey: PublicKey): String = runBlocking {
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
            val request = getRequestGet("$serviceServer/server", EnumMap(Strings::class.java)).execute()
            val answer = getServerAnswer("server", request.returnResponse(), false)
            if (answer.status != 200) {
                throw SecureException(Strings.API_KEY_ERROR.value)
            }
            serverKey = KeyFactory.getInstance(Strings.RSA_INSTANCE_SECOND.value).generatePublic(
                X509EncodedKeySpec(Base64.decodeBase64(answer.getMessage()))
                                                                                                )
        } catch (e: Exception) {
            throw SecureException(e)
        }
    }
}