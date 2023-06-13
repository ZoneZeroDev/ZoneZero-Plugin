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
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.logging.Level

class FilesUtils(private val plugin: ZoneZero) {

    fun getTomlFile(fileName: String): TomlFile {
        val configFile = getFile(fileName)
        if (!configFile.exists()) {
            copyFile(fileName, configFile)
        } else {
            checkTomlFileVersion(configFile)
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

    private fun checkTomlFileVersion(configFile: File) {
        val cfgVersion = getConfigVersion(configFile)
        if (cfgVersion == 0.0) return
        val fileName = configFile.name
        val tmpCfg = getFile("${fileName.replace(".toml", "")}${Strings.TMP_TOML_SUFFIX.value}")
        deleteFile(tmpCfg)
        copyFile(fileName, tmpCfg)
        val newVersion: Double = getConfigVersion(getFile("${fileName.replace(".toml", "")}${Strings.TMP_TOML_SUFFIX.value}"))
        if (newVersion > cfgVersion || newVersion < cfgVersion) {
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
        deleteFile(tmpCfg)
    }

    private fun getConfigVersion(file: File): Double {
        return try {
            Toml.parse(file.inputStream()).getTableOrEmpty(ConfigTable.CONFIG.value).getDouble(ConfigKey.CONFIG_VERSION.value) { 0.0 }
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