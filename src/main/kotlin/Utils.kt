import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.lang.NullPointerException

private class A {

}

/**
 * 从resource目录下读取文件
 * [fileName]是文件名
 */
@Throws(Exception::class)
fun getResourceFile(fileName: String): File {
    val classLoader = A::class.java.classLoader
    val url = classLoader.getResource(fileName) ?: throw NullPointerException("file name = $fileName")
    val resultFile = File(url.toURI())
    if (!resultFile.exists()) {
        throw FileNotFoundException("file name = $fileName")
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

fun filterLogFilePrintByLineLength(lineLength: Int, filterToThrow: Array<String>, inputFile: File, outputFile: File) {
    val outputWriter = outputFile.writer()
    inputFile.forEachLine { inputLine ->
        if (inputLine.length >= lineLength) {
            // 词汇过滤
            var shouldThrow = false
            filterToThrow.forEach {
                if (inputLine.contains(it)) {
                    shouldThrow = true
                }
                if (inputLine.first() == ' ') {
                    shouldThrow = true
                }
            }

            if (!shouldThrow) {
                outputWriter.appendLine(inputLine)
            }
        }
    }
    outputWriter.close()
}
