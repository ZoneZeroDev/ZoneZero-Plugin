package kiinse.me.zonezero.plugin.utils

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.enums.Config
import org.apache.commons.io.FileUtils
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.logging.Level

class FilesUtils(private val plugin: ZoneZero) {

    fun getTomlFile(fileName: String): TomlParseResult {
        val configFile = getFile(fileName)
        if (!configFile.exists()) {
            copyFile(fileName, configFile)
        } else {
            checkTomlFileVersion(configFile)
        }
        return Toml.parse(getFile(fileName).inputStream())
    }

    private fun checkTomlFileVersion(configFile: File) {
        val cfgVersion = getConfigVersion(configFile)
        if (cfgVersion == 0.0) return;
        val fileName = configFile.name
        val tmpCfg = getFile("${fileName}_tmp.toml")
        deleteFile(tmpCfg)
        copyFile(fileName, tmpCfg)
        val newVersion: Double = getConfigVersion(getFile("${fileName}_tmp.toml"))
        if (newVersion > cfgVersion || newVersion < cfgVersion) {
            try {
                val oldCfg = getFile("${fileName}_old.toml")
                deleteFile(oldCfg)
                copyFileInFolder(configFile, oldCfg)
                deleteFile(configFile)
                copyFile(fileName)
                ZoneZero.sendLog(Level.WARNING, "Version mismatch found for file '&c${fileName}&6'. This file has been renamed to '&c${oldCfg.name}&6' and a new file '&c${fileName}&6' has been created")
            } catch (e: Exception) {
                ZoneZero.sendLog(Level.WARNING, "An error occurred while copying the new version of the file '&c${fileName}&6'! Message:", e)
            }
        }
        deleteFile(tmpCfg)
    }

    private fun getConfigVersion(file: File): Double {
        return try {
            Toml.parse(file.inputStream()).getTableOrEmpty(Config.TABLE_CONFIG.value).getDouble(Config.CONFIG_VERSION.value) { 0.0 }
        } catch (e: Exception) {
            0.0
        }
    }

    @Throws(IOException::class)
    fun createFile(file: File) {
        if (file.createNewFile()) ZoneZero.sendLog(Level.CONFIG, "File '&d" + file.name + "&6' created")
    }

    @Throws(SecurityException::class)
    fun createDirectory(file: File) {
        if (file.exists()) deleteFile(file)
        if (file.mkdirs()) ZoneZero.sendLog(Level.CONFIG, "Directory '&d" + file.name + "&6' created")
    }

    fun copyFile(file: String) {
        copyFileMethod(file, getFile(file))
    }

    fun copyFile(oldFile: String, newFile: File) {
        copyFileMethod(oldFile, newFile)
    }

    private fun copyFileMethod(oldFile: String, destFile: File) {
        if (destFile.exists()) deleteFile(destFile)
        val inputStream = accessFile(oldFile)
        if (inputStream != null) {
            try {
                FileUtils.copyInputStreamToFile(inputStream, destFile)
                ZoneZero.sendLog(Level.CONFIG, "File '&d" + destFile.name + "&6' created")
            } catch (e: IOException) {
                ZoneZero.sendLog(Level.WARNING, "Error on copying file '&c" + destFile.name + "&6'! Message:", e)
            }
        } else {
            ZoneZero.sendLog(Level.WARNING, "File '&c$oldFile&6' not found inside plugin jar. Creating a new file...")
            try {
                createFile(destFile)
            } catch (e: IOException) {
                ZoneZero.sendLog(Level.WARNING, "Error on creating file '" + destFile.name + "'! Message:", e)
            }
        }
    }

    fun copyFile(destFile: File) {
        if (!destFile.exists() || listFilesInDirectory(destFile).isEmpty()) {
            val directoryName = destFile.name
            try {
                createDirectory(destFile)
                for (file in getFilesInDirectoryInJar(directoryName)) {
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(accessFile("$directoryName/$file")),
                                                    getFile(directoryName, file)
                    )
                }
            } catch (e: Exception) {
                ZoneZero.sendLog(Level.WARNING, "Error on copying directory '&c$directoryName&6'! Message:", e)
                deleteFile(destFile)
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
            ZoneZero.sendLog(Level.CONFIG, "File '&d" + file1.name + "&6' copied to file '&d" + file2.name + "&6'")
        }
    }

    private fun accessFile(file: String): InputStream? {
        var input = ZoneZero::class.java.getResourceAsStream(file)
        if (input == null) input = ZoneZero::class.java.classLoader.getResourceAsStream(file)
        return input
    }

    @Throws(Exception::class)
    fun getFilesInDirectoryInJar(directory: String): List<String> {
        val list = ArrayList<String>()
        getResourceUrls("classpath:/$directory/*.*").forEach { name ->
            val split = name!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            list.add(split[split.size - 1])
        }
        return list
    }

    @Throws(IOException::class)
    private fun getResourceUrls(locationPattern: String): List<String?> {
        return Arrays.stream(PathMatchingResourcePatternResolver(ZoneZero::class.java.classLoader).getResources(locationPattern))
            .map { r: Resource -> toURL(r) }
            .filter { obj: String? -> Objects.nonNull(obj) }
            .toList()
    }

    private fun toURL(r: Resource): String? {
        return try {
            r.url.toExternalForm()
        } catch (e: IOException) { null }
    }

    fun isDirectoryEmpty(file: File): Boolean {
        return listFilesInDirectory(file).isEmpty()
    }

    private val dataFolder: String
        get() = plugin.dataFolder.absolutePath + File.separator

    fun getFile(file: String): File {
        return File(dataFolder + file)
    }

    fun getFile(folder: String, file: String): File {
        return File(dataFolder + folder + File.separator + file)
    }

    fun deleteFile(file: File) {
        if (file.delete()) ZoneZero.sendLog(Level.CONFIG, "File '&d" + file.name + "&6' deleted")
    }
}