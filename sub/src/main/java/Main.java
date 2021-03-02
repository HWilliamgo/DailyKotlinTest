import java.net.InetAddress;

/**
 * date: 2020/6/3
 * author: hwj
 * description:
 */
public class Main {
    public static void main(String[] args) {
        IdData<String> stringIdData = new IdData<>();
        IdData<Integer> integerIdData = new IdData<>();
        IdData<Thread> threadIdData = new IdData<>();

//        stringIdData.add()
        showKey(stringIdData);
        showKey(integerIdData);

        showKey2(integerIdData);


    }

    private static void showKey(IdData<?> param) {

    }

    private static void showKey2(IdData<? extends Number> param) {
        //取出是安全的
        Number number = param.data;
    }
}
