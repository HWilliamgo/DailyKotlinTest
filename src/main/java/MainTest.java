import java.util.HashMap;

/**
 * date: 2020/12/5
 * author: hwj
 * description:
 */
public class MainTest {
    public static void main(String[] args) {
        Base base = new Derived();
        base.foo();
    }
}

abstract class Base {
    public int x = 1;

    {
        System.out.println("Base init block");
    }

    public Base() {
        System.out.println("Base construct");
        foo();
    }

    public abstract void foo();
}

class Derived extends Base {
    int x = 2;
    {
        System.out.println("Derived init block");
    }

    @Override
    public void foo() {
        System.out.println("Derived :" + x);
    }
}