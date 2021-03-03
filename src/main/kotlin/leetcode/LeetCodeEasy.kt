/**
 * leetcode easy Algorithms
 */

fun reverse(input: Int): Int {
    var x = input
    var res: Long = 0
    while (x != 0) {
        res = res * 10 + x % 10
        x /= 10;
    }
    return if (res > Int.MAX_VALUE || res < Int.MIN_VALUE) 0 else res.toInt()
}

fun isPalindrome(input: Int): Boolean {
    var x: Int = input
    if (x < 0 || (x % 10 == 0)) {
        return false
    }
    var halfReverseX = 0
    while (x > halfReverseX) {
        halfReverseX = halfReverseX * 10 + x % 10
        x /= 10
    }
    return halfReverseX == x || halfReverseX / 10 == x
}

fun romanToInteger(input: String): Int {
    val map = HashMap<Char, Int>()
    map.apply {
        put('I', 1)
        put('V', 5)
        put('X', 10)
        put('L', 50)
        put('C', 100)
        put('D', 500)
        put('M', 1000)
    }
    var index = input.length - 2
    var sum = map[input[index + 1]]!!
    while (index >= 0) {
        val left = map[input[index]]!!
        val right = map[input[index + 1]]!!
        if (left < right) {
            sum -= left
        } else {
            sum += left
        }
        index--
    }
    return sum
}

/**
 * 待优化：可以先遍历一遍，找到input中长度最小的字符串，用长度最小的字符串来做外层遍历。
 */
fun longestCommonPrefix(input: Array<String>): String {
    if (input.isEmpty()) {
        return ""
    }
    var ret = ""
    val first = input[0];
    val firstCharArray = first.toCharArray()
    var validLength = 0
    out@ for ((i, c) in firstCharArray.withIndex()) {
        for ((j, s) in input.withIndex()) {
            if (i > s.length - 1) {
                break@out
            }
            if (c != s[i]) {
                break@out
            }
        }
        validLength++
    }
    return first.substring(0, validLength)
}