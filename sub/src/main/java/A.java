/**
 * date: 2020/6/1
 * author: hwj
 * description:
 */
class A {
    public static void a() {
        String a = "sdfs.txt";
        int indexOfDot = a.indexOf(".");
        String first = a.substring(0, indexOfDot);
        String second = a.substring(indexOfDot);
        System.out.print(first+"  "+second);
    }
}
