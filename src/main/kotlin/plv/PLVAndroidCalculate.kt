package plv

import java.lang.StringBuilder

/**
 * date: 2021/10/13
 * author: hwj
 * description:
 */
/**
 * 皮球反弹
 *
 * 一个皮球自由落下，每次落地后反弹回原高度的一半，然后再落下，再反弹，一直持续。
 * 求皮球从高度为height的地方落下，第time是次反弹的高度。
 *
 *  输入
 *  1000 2
 *  输出
 *  250.00
 */
fun calculate1(height: Float, times: Int): String {
    val target = Math.pow(2.0, times.toDouble())
    val ret = height / target
    return String.format("%.2f", ret)
}

/**
 * 整数拼接
 *
 * 给定一组非负整数，重新排列它们的顺序使之组成一个最大的整数。
 *
 *  输入
 *  10 2
 *  输出
 *  210
 *
 *  输入
 *  3 30 34 5 9
 *  输出
 *  9534330
 */
fun calculate2(vararg input: Int): String {
    fun getNum(n: Int): Int {
        var f = n.toFloat()
        var ret = 0
        while (f >= 1) {
            f /= 10
            ret++
        }
        return ret
    }

    val list = input.toMutableList()
    list.sortWith { a, b ->
        var numA = getNum(a)
        var numB = getNum(b)
        val lengthSmallerOne = if (numA > numB) b else a
        var aF = 0f
        var bF = 0.1f
        while (numA > 0 && numB > 0 && aF != bF) {
            numA--
            numB--
            aF = (a.toFloat() / Math.pow(10.0, numA.toDouble())).toFloat()
            bF = (b.toFloat() / Math.pow(10.0, numB.toDouble())).toFloat()
        }
        if (aF == bF) {
            if (lengthSmallerOne == a) {
                -1
            } else {
                1
            }
        } else if (aF > bF) {
            -1
        } else {
            1
        }
    }

    val sb = StringBuilder()
    list.forEach {
        sb.append(it)
    }
    return sb.toString()
}

/**
 * 为保利威添砖加瓦
 *
 * 保利威简称“POLYV”，作为保利威的一份子，必须为公司添砖加瓦了。
 * 现在给你一个字符串，请你计算一下，从中选取字符，最多能组成多少个“POLYV”？
 *
 * 输入
 * POLYVPOLYV
 * 输出
 * 2
 */
fun calculate3(str: String, target: String = "POLYV"): Int {
    /* boolean是true表示是已经用过了，false表示还没有用过 */
    val hashMap = HashMap<Char, Boolean>()
    str.forEach {
        hashMap[it] = false
    }
    var hitNum = 0
    var keepSearch = true
    while (keepSearch) {
        target.forEach { c ->
            var hitChar = false
            hashMap.entries.forEach { entry ->
                if (c == entry.key) {
                    if (!entry.value) {
                        entry.setValue(true)
                        hitChar = true
                    }
                }
            }
            if (!hitChar) {
                keepSearch = false
            }
        }
        hitNum++
    }

    return hitNum
}

/**
 * 空瓶换汽水
 *
 * 某商店规定：三个空汽水瓶可以换一瓶汽水。小张手上有N个空汽水瓶，他最多可以换多少瓶汽水喝？
 */
fun calculate4(n: Int): Int {
    var ret = 0
    var left = n

    while (left >= 3) {
        val newBottle = left / 3
        ret += newBottle
        // 换后剩下的空瓶
        left %= 3
        // 换来的新瓶子喝完后的空瓶
        left += newBottle
    }
    return ret
}