import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.io.FileReader
import java.io.RandomAccessFile

/**
 * date: 2019/12/11
 * author: hwj
 * description:
 */

fun main() {

}

fun takeTimeFromJianShuByJSoup() {
    val url = "https://www.jianshu.com/nb"

//    val categoryId = "";

    val categoryIdPairList = getCategoryAndId()

    //请求每一个分类下的每一篇文章的 标题和时间，并绑定。
    val allEssay = AllEssay()

    categoryIdPairList.forEach {
        val categoryId = it.id
        val categoryName = it.name

        //page 从1开始计数,遍历当前category下的所有的文章
        var currentPage = 1
        var tmpList = parseByJsoup(categoryId, currentPage)
        while (tmpList.isNotEmpty()) {
            currentPage++
            tmpList.forEach { essay ->
                allEssay.put(categoryName, essay)
            }
            tmpList = parseByJsoup(categoryId, currentPage)
        }
    }


    //https://www.jianshu.com/nb/16737585?order_by=added_at&page=2
}

fun parseByJsoup(id: String, page: Int): List<Essay> {
    val result: MutableList<Essay> = ArrayList()

    val url = "https://www.jianshu.com/nb"
    val finalUrl = "$url/$id?order_by=added_at&page=$page"
    val document = Jsoup.connect(finalUrl).get()

    return result
}

fun getCategoryAndId(): List<CategoryIdPair> {
    val jsonFilePath = "/Users/HWilliam/IdeaProjects/test/src/main/kotlin/category.json"
    val rootJson = JSONObject(File(jsonFilePath).readText())
    val arrays = rootJson.getJSONArray("notebooks")

    //<id,CategoryName>
    val list: MutableList<CategoryIdPair> = ArrayList()

    for (i in 0 until arrays.length()) {
        val id = arrays.getJSONObject(i).getString("id")
        val name = arrays.getJSONObject(i).getString("name")
        list.add(CategoryIdPair(id, name))
    }
    return list
}

fun timeFormatConvert(input: String): String {
    return ""
}

fun addHeadForMD(){
    val essayParentDir = "/Users/HWilliam/IdeaProjects/test/src/main/kotlin/_posts"
    val essayParentDirFile = File(essayParentDir)

    essayParentDirFile.listFiles()?.forEach {
        //it --> 目录文件
        println(it.name)
        it.listFiles()?.forEach { oneMDFile ->
            //文件名字
            val realFileNameOfMD = oneMDFile.name
            val fileNameWithoutMD = realFileNameOfMD.substringBefore(".")
            val tmpFileNameOfFinalMDFile = realFileNameOfMD + "tmp"


            val headerToAppend = "---\ntitle: ${fileNameWithoutMD}\n---\n\n"

            val resultFile = File(it, tmpFileNameOfFinalMDFile)

            val randomAccessFileOfResult = RandomAccessFile(resultFile, "rw")
            //写header
            randomAccessFileOfResult.write(headerToAppend.toByteArray())

            //将所有原文件的内容追加进去
            val randomAccessFileOfTarget = RandomAccessFile(oneMDFile, "rw")
            val buffer: ByteArray = ByteArray(10240)
            while (randomAccessFileOfTarget.read(buffer) != -1) {
                randomAccessFileOfResult.write(buffer)
            }

            randomAccessFileOfResult.close()
            randomAccessFileOfTarget.close()

            oneMDFile.delete()
            resultFile.renameTo(oneMDFile)
        }
    }
}

class CategoryIdPair(val id: String, val name: String)

class Essay(val title: String, val time: String)

class AllEssay() {
    // <Category , essayList>
    val map: HashMap<String, MutableList<Essay>> = HashMap()

    fun put(category: String, essay: Essay) {
        if (!map.containsKey(category)) {
            map.put(category, ArrayList())
        }
        map.get(category)?.add(essay)
    }
}

