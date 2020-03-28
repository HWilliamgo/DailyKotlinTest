interface Base {
    fun print()
}

class BaseImpl(val x: Int) : Base {
    override fun print() {
        print(x)
    }
}

class Derived(val b: Base) : Base{

    override fun print() {
        b.print()
        print("从Derived中打印")
    }
}

fun main() {
    val b = BaseImpl(10)
    Derived(b).print()
}