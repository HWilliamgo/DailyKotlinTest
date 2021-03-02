import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.lang.NullPointerException

private class A {

}

/**
 * 从resource目录下读取文件
 * [file]是文件名
 */
@Throws(Exception::class)
fun getResourceFile(file: String): File {
    val classLoader = A::class.java.classLoader
    val url = classLoader.getResource(file) ?: throw NullPointerException("file name = $file")
    val resultFile = File(url.toURI())
    if (!resultFile.exists()) {
        throw FileNotFoundException("file name = $file")
    }
    return resultFile
}

/**
 * 用[tag]来过滤日志文件[targetLogFile]中的内容并打印
 */
fun filterLogFilePrint(tag: String, targetLogFile: File, ignoreCase: Boolean = false) {
    targetLogFile.forEachLine {
        if (it.contains(tag, ignoreCase)) {
            println(it)
        }
    }
}

