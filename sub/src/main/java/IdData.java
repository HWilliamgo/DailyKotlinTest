/**
 * date: 2020/6/3
 * author: hwj
 * description:
 */
public class IdData<T> {
    String id;
    T data;

    public <N extends Number> Number add(N a, N b) {
        return a.doubleValue() + b.doubleValue();
    }
}
