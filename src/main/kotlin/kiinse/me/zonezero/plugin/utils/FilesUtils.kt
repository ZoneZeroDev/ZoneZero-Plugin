package kiinse.me.zonezero.plugin.utils

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.config.TomlFile
import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import kiinse.me.zonezero.plugin.config.enums.ConfigTable
import kiinse.me.zonezero.plugin.enums.Replace
import kiinse.me.zonezero.plugin.enums.Strings
import org.apache.commons.io.FileUtils
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.logging.Level

class FilesUtils(private val plugin: ZoneZero) {

    val defaultMessagesFiles = listOf(Strings.LANG_EN_FILE, Strings.LANG_RU_FILE)

    fun getTomlFile(fileName: String): TomlFile {
        val configFile = getFile(fileName)
        if (!configFile.exists()) {
            copyFile(fileName, configFile)
        } else {
            checkFilesVersion(configFile)
        }
        return TomlFile(Toml.parse(getFile(fileName).inputStream()))
    }

    fun getTomlFileWithoutCheck(fileName: String): TomlFile {
        val configFile = getFile(fileName)
        if (!configFile.exists()) {
            copyFile(fileName, configFile)
        }
        return TomlFile(Toml.parse(getFile(fileName).inputStream()))
    }

    private fun checkFilesVersion(configFile: File) {
        val oldToml = Toml.parse(configFile.inputStream())
        val fileName = configFile.name
        val tmpCfg = getFile("${fileName.replace(".toml", "")}${Strings.TMP_TOML_SUFFIX.value}")
        deleteFile(tmpCfg)
        copyFile(fileName, tmpCfg)
        val newToml = Toml.parse(getFile("${fileName.replace(".toml", "")}${Strings.TMP_TOML_SUFFIX.value}").inputStream())
        if (checkMessagesVersion(getMessagesVersion(newToml), getMessagesVersion(oldToml), fileName, configFile, tmpCfg)) {
            checkTomlVersion(getConfigVersion(newToml), getConfigVersion(oldToml), fileName, configFile, tmpCfg)
        }
    }

    private fun checkTomlVersion(newConfigVersion: Double, cfgVersion: Double, fileName: String, configFile: File, tmpCfg: File) {
        if (cfgVersion == 0.0) return
        if (newConfigVersion > cfgVersion || newConfigVersion < cfgVersion) {
            updateConfig(fileName, configFile)
        }
        deleteFile(tmpCfg)
    }

    private fun checkMessagesVersion(newMessagesVersion: Double, messagesVersion: Double, fileName: String, configFile: File, tmpCfg: File): Boolean {
        if (messagesVersion == 0.0) return true
        if (newMessagesVersion > messagesVersion || newMessagesVersion < messagesVersion) {
            updateMessages()
            updateConfig(fileName, configFile)
            deleteFile(tmpCfg)
            return false
        }
        return true
    }

    private fun updateConfig(fileName: String, configFile: File) {
        try {
            val oldCfg = getFile("${fileName.replace(".toml", "")}${Strings.OLD_TOML_SUFFIX.value}")
            deleteFile(oldCfg)
            copyFileInFolder(configFile, oldCfg)
            deleteFile(configFile)
            copyFile(fileName)
            ZoneZero.sendLog(Level.WARNING, Strings.TOML_MISMATCH_MESSAGE.value
                .replace(Replace.FILE.value, fileName, ignoreCase = true)
                .replace(Replace.OLD_FILE.value, oldCfg.name, ignoreCase = true))
        } catch (e: Exception) {
            ZoneZero.sendLog(Level.WARNING, Strings.NEW_TML_VERSION_COPY_ERROR.value
                .replace(Replace.FILE.value, fileName, ignoreCase = true), e)
        }
    }

    private fun updateMessages() {
        val dirName = Strings.MESSAGES_DIR.value
        try {
            val messages = getFile(dirName)
            val oldMessages = getFile("${dirName}${Strings.OLD_MESSAGES_SUFFIX.value}")
            deleteFile(oldMessages)
            oldMessages.mkdir()
            messages.listFiles()?.forEach {
                copyFileInFolder(it, getFile("${dirName}${Strings.OLD_MESSAGES_SUFFIX.value}", it.name))
            }
            deleteFile(messages)
            messages.mkdir()
            defaultMessagesFiles.forEach {
                copyFileFromDir(it)
            }
            ZoneZero.sendLog(Level.WARNING, Strings.MESSAGES_MISMATCH_MESSAGE.value
                .replace(Replace.FILE.value, dirName, ignoreCase = true)
                .replace(Replace.OLD_FILE.value, oldMessages.name, ignoreCase = true))
        } catch (e: Exception) {
            ZoneZero.sendLog(Level.WARNING, Strings.NEW_MESSAGES_VERSION_COPY_ERROR.value
                .replace(Replace.FILE.value, dirName, ignoreCase = true), e)
        }
    }

    private fun getConfigVersion(toml: TomlParseResult): Double {
        return try {
            toml.getTableOrEmpty(ConfigTable.CONFIG.value).getDouble(ConfigKey.VERSION.value) { 0.0 }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun getMessagesVersion(toml: TomlParseResult): Double {
        return try {
            toml.getTableOrEmpty(ConfigTable.MESSAGES.value).getDouble(ConfigKey.VERSION.value) { 0.0 }
        } catch (e: Exception) {
            0.0
        }
    }

    @Throws(IOException::class)
    fun createFile(file: File) {
        if (file.createNewFile()) ZoneZero.sendLog(Level.CONFIG, Strings.FILE_CREATED.value
            .replace(Replace.FILE.value, file.name, ignoreCase = true))
    }

    @Throws(SecurityException::class)
    fun createDirectory(file: File) {
        if (file.mkdirs()) ZoneZero.sendLog(Level.CONFIG, Strings.DIRECTORY_CREATED.value
            .replace(Replace.DIRECTORY.value, file.name, ignoreCase = true))
    }

    fun copyFile(file: String) {
        copyFileMethod(file, getFile(file))
    }

    fun copyFile(oldFile: String, newFile: File) {
        copyFileMethod(oldFile, newFile)
    }

    fun copyFileFromDir(directoryFile: Strings) {
        val dirFile = directoryFile.value
        val split = dirFile.split("/")
        copyFileMethod(dirFile, getFile(split[0], split[1]))
    }

    private fun copyFileMethod(oldFile: String, destFile: File) {
        if (destFile.exists()) deleteFile(destFile)
        val inputStream = accessFile(oldFile)
        if (inputStream != null) {
            try {
                FileUtils.copyInputStreamToFile(inputStream, destFile)
                ZoneZero.sendLog(Level.CONFIG, Strings.FILE_CREATED.value
                    .replace(Replace.FILE.value, destFile.name, ignoreCase = true))
            } catch (e: IOException) {
                ZoneZero.sendLog(Level.WARNING, Strings.FILE_COPY_ERROR.value
                    .replace(Replace.FILE.value, destFile.name, ignoreCase = true), e)
            }
        } else {
            ZoneZero.sendLog(Level.WARNING, Strings.FILE_NOT_FOUND_INSIDE_JAR.value
                .replace(Replace.FILE.value, oldFile, ignoreCase = true))
            try {
                createFile(destFile)
            } catch (e: IOException) {
                ZoneZero.sendLog(Level.WARNING, Strings.FILE_CREATE_ERROR.value
                    .replace(Replace.FILE.value, destFile.name, ignoreCase = true), e)
            }
        }
    }

    fun listFilesInDirectory(directory: File): List<File> {
        val list = ArrayList<File>()
        Collections.addAll(list, *Objects.requireNonNull(directory.listFiles()))
        return list
    }

    @Throws(IOException::class)
    fun copyFileInFolder(file1: File, file2: File) {
        if (!file2.exists()) {
            FileUtils.copyFile(file1, file2)
            ZoneZero.sendLog(Level.CONFIG, Strings.FILE_COPIED.value
                .replace(Replace.OLD_FILE.value, file1.name, ignoreCase = true)
                .replace(Replace.FILE.value, file2.name, ignoreCase = true))
        }
    }

    fun accessFile(file: String): InputStream? {
        return PathMatchingResourcePatternResolver(ZoneZero::class.java.classLoader).getResource("classpath:/$file").inputStream
    }


    private val dataFolder: String
        get() = plugin.dataFolder.absolutePath + File.separator

    fun getFile(file: String): File {
        return File(dataFolder + file)
    }

    fun getFile(directory: String, file: String): File {
        return File(dataFolder + directory + File.separator + file)
    }

    private fun deleteFile(file: File) {
        if (file.delete()) ZoneZero.sendLog(Level.CONFIG, Strings.FILE_DELETED.value
            .replace(Replace.FILE.value, file.name, ignoreCase = true))
    }
}