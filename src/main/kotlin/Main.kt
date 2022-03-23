import test.Animal
import test.Leg
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

/**
 * author: HWilliamgo
 * description:
 */
fun main() {
   for(i in 0 until 3){
        println("$i")
   }
}

fun a(): String {
    return ""
}

// 非空参数调用
fun test_1(str: String) = str.length

// 可空参数调用
fun test_2(str: String?) = str?.length

// 可空参数断言
fun test_3(str: String?) = str!!.length

// 可空参数强转非空参数
fun test_4(str: Any?) {
    str as String
}

// 非空参数强转非空参数
fun test_5(str: Any?) {
    str as? String
}

fun test(input: String?): String {
    return ""
}