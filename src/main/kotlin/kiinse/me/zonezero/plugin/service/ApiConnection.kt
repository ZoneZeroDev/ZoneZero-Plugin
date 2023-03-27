package kiinse.me.zonezero.plugin.service

import io.sentry.Sentry
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.service.enums.KeyType
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.exceptions.APIException
import kiinse.me.zonezero.plugin.exceptions.SecureException
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.bukkit.Bukkit
import org.json.JSONObject
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

class ApiConnection(private val zoneZero: ZoneZero) : ApiService {

    private val keys: MutableMap<KeyType, Key> = EnumMap(KeyType::class.java)
    private var publicKeyString: String = String()
    private var serverKey: PublicKey
    private val serviceServer = "http://localhost:7223" // TODO: Изменить api.kiinse.me
    private val token: String

    init {
        val config = zoneZero.configuration
        val credentials = config.getTable(Config.TABLE_CREDENTIALS.value) ?: throw APIException("Credentials is null!")
        token = credentials.getString(Config.CREDENTIALS_TOKEN.value) ?: throw APIException("Service token is null!")
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
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
        try {
            ZoneZero.sendLog(Level.CONFIG, "-----------------------") // TODO: Убрать при релизе
            ZoneZero.sendLog(Level.CONFIG, "Address: ${address.string}") // TODO: Убрать при релизе
            val request = Request.Get("$serviceServer/${address.string}")
                .connectTimeout(50000)
                .socketTimeout(50000)
                .addHeader("Content-Type", "application/json")
                .addHeader("publicKey", publicKeyString)
                .addHeader("Authorization", "Bearer ${zoneZero.token}")
                .addHeader("onpl", encrypt(Bukkit.getServer().onlinePlayers.size.toString(), serverKey))
                .execute()
            return getServerAnswer(request.returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            return ServerAnswer(500, JSONObject())
        }
    }

    @Throws(SecureException::class)
    override fun post(address: ServerAddress, data: JSONObject): ServerAnswer {
        try {
            val encrypted = encrypt(data, serverKey)
            ZoneZero.sendLog(Level.CONFIG, "-----------------------") // TODO: Убрать при релизе
            ZoneZero.sendLog(Level.CONFIG, "Address: ${address.string}") // TODO: Убрать при релизе
            val request = Request.Post("$serviceServer/${address.string}")
                .connectTimeout(50000)
                .socketTimeout(50000)
                .addHeader("Content-Type", "application/json")
                .addHeader("publicKey", publicKeyString)
                .addHeader("Authorization", "Bearer ${zoneZero.token}")
                .addHeader("security", encrypted.aes)
                .addHeader("onpl", getOnpl(serverKey))
                .useExpectContinue()
                .version(HttpVersion.HTTP_1_1)
                .bodyString(encrypted.message, ContentType.APPLICATION_JSON)
                .execute()
            return getServerAnswer(request.returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            return ServerAnswer(500, JSONObject())
        }
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
        ZoneZero.sendLog(Level.CONFIG, "Status: ${answer.status}") // TODO: Убрать при релизе
        ZoneZero.sendLog(Level.CONFIG, "Body: ${answer.data}") // TODO: Убрать при релизе
        ZoneZero.sendLog(Level.CONFIG, "-----------------------") // TODO: Убрать при релизе
        if (responseCode == 401) {
            if (body.has("message") && body.has("jwt") && body.getString("message").lowercase() == "your jwt has been updated!") {
                zoneZero.filesUtils.updateTomlKey("config.toml", body.getString("jwt"))
                zoneZero.onReload()
            }
        }
        return answer
    }

    private fun getEncryptedMessage(response: HttpResponse, content: String): EncryptedMessage {
        val security = getSecurityHeader(response.allHeaders)
        return try {
            EncryptedMessage(security, JSONObject(content).getString("data"))
        } catch (e: Exception) {
            EncryptedMessage(security, content)
        }
    }

    private fun getSecurityHeader(allHeaders: Array<Header>): String {
        for (header in allHeaders) {
            if (header.name == "security") return header.value
        }
        return ""
    }

    private fun getBody(encrypted: EncryptedMessage): JSONObject {
        if (encrypted.message.isEmpty()) return JSONObject()
        if (encrypted.aes.isNullOrEmpty()) return JSONObject(encrypted.message)
        return decrypt(encrypted)
    }

    @Throws(APIException::class)
    private fun decrypt(encrypted: EncryptedMessage): JSONObject {
        try {
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, keys[KeyType.PRIVATE])
            val aesKey = cipher.doFinal(Base64.decodeBase64(encrypted.aes))
            val originalKey = SecretKeySpec(aesKey , 0, aesKey.size, "AES")
            val aesCipher = Cipher.getInstance("AES")
            aesCipher.init(Cipher.DECRYPT_MODE, originalKey)
            return JSONObject(String(aesCipher.doFinal(Base64.decodeBase64(encrypted.message))))
        } catch (e: Exception) {
            Sentry.captureException(e)
            throw APIException(e.message)
        }
    }

    @Throws(APIException::class)
    private fun encrypt(json: JSONObject, publicKey: PublicKey): EncryptedMessage {
        val generator = KeyGenerator.getInstance("AES")
        generator.init(128)
        val aesKey: SecretKey = generator.generateKey()
        val aesCipher = Cipher.getInstance("AES")
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return EncryptedMessage(Base64.encodeBase64String(cipher.doFinal(aesKey.encoded)),
                                Base64.encodeBase64String(aesCipher.doFinal(json.toString().toByteArray())))
    }

    @Throws(APIException::class)
    private fun encrypt(string: String, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.encodeBase64String(cipher.doFinal(string.toByteArray()))
    }

    @Throws(SecureException::class)
    override fun updateServerKey() {
        try {
            ZoneZero.sendLog(Level.CONFIG, "-----------------------") // TODO: Убрать при релизе
            ZoneZero.sendLog(Level.CONFIG, "Address: server") // TODO: Убрать при релизе
            val request = Request.Get("$serviceServer/server")
                .connectTimeout(50000)
                .socketTimeout(50000)
                .addHeader("Content-Type", "application/json")
                .addHeader("publicKey", publicKeyString)
                .execute()
            val answer = getServerAnswer(request.returnResponse())
            if (answer.status != 200) { throw SecureException("Something gone wrong with api server") }
            serverKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Base64.decodeBase64(answer.data.getString("message"))))
        } catch (e: Exception) {
            Sentry.captureException(e)
            ZoneZero.sendLog(Level.CONFIG, e)
            throw SecureException(e)
        }
    }
}