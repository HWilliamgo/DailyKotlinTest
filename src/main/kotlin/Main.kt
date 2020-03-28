import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.io.FileReader
import java.io.RandomAccessFile
import java.lang.RuntimeException

/**
 * date: 2019/12/11
 * author: hwj
 * description:
 */

fun main() {
    val allEssay = takeTimeFromJianShuByJSoup()
    addHeadForMD(allEssay)
}

fun takeTimeFromJianShuByJSoup(): AllEssay {
    //https://www.jianshu.com/nb/16737585?order_by=added_at&page=2

    val categoryIdPairList = getCategoryAndId()

    //请求每一个分类下的每一篇文章的 标题和时间，并绑定。
    val allEssay = AllEssay()

    categoryIdPairList.forEach {
        val categoryId = it.id
        val categoryName = it.name

        //page 从1开始计数,遍历当前category下的所有的文章
        var currentPage = 1
        var tmpList = parseByJsoup(categoryId, currentPage, categoryName)
        while (tmpList.isNotEmpty()) {
            currentPage++
            tmpList.forEach { essay ->
                allEssay.put(categoryName, essay)
            }
            tmpList = parseByJsoup(categoryId, currentPage, categoryName)
        }
    }
    return allEssay
}

fun parseByJsoup(id: String, page: Int, categoryName: String): List<Essay> {
    println("======类别：$categoryName")
    val result: MutableList<Essay> = ArrayList()

    val url = "https://www.jianshu.com/nb"
    val finalUrl = "$url/$id?order_by=added_at&page=$page"
    val document = Jsoup.connect(finalUrl).get()
    document.getElementsByTag("li").forEach { li ->
        val div = li.getElementsByTag("div")
        div.forEach { eachDiv ->
            if (eachDiv.attr("class").trim() == "content") {
                val title = eachDiv.getElementsByTag("a").first().text()
                var time = eachDiv
                    .getElementsByTag("div").first()
                    .getElementsByTag("span").filter {
                        it.attr("class").trim() == "time"
                    }.first().attr("data-shared-at")
                time = timeFormatConvert(time)

                println("标题：$title 创建日期：$time")
                result.add(Essay(title, time))
            }
        }
    }
    return result
}

fun getCategoryAndId(): List<CategoryIdPair> {
    val jsonFilePath = "/Users/HWilliam/IdeaProjects/test/src/main/kotlin/category.json"
    val rootJson = JSONObject(File(jsonFilePath).readText())
    val arrays = rootJson.getJSONArray("notebooks")

    //<id,CategoryName>
    val list: MutableList<CategoryIdPair> = ArrayList()

    for (i in 0 until arrays.length()) {
        val id = arrays.getJSONObject(i).getInt("id").toString()
        val name = arrays.getJSONObject(i).getString("name")
        list.add(CategoryIdPair(id, name))
    }
    return list
}

fun timeFormatConvert(i: String): String {
    val input = i.replace("-", "/")
    val date = input.split("T")[0]
    val time = input.split("T")[1].split("+").first()
    return "$date $time"
}

fun addHeadForMD(allEssay: AllEssay) {
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

            val date = allEssay.findTime(it.name, fileNameWithoutMD)
            val headerToAppend = "---\ntitle: ${fileNameWithoutMD}\ndate: $date\n---\n\n"

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

    fun findTime(category: String, essayName: String): String {
        map.get(category)?.forEach {
            if (it.title == essayName) {
                return it.time
            } else if (it.title.replace("(?=\\\\pP)[^-_]", "-") == essayName) {
                return it.time
            }
        }
//        throw RuntimeException("没有找到时间 $category, $essayName")
        println("文章无时间：$category -> $essayName")
        return ""
    }
}

