import java.util.List;

/**
 * date: 2020/6/1
 * author: hwj
 * description:
 */
interface ISupder<T extends Number> {

    void add(T n);

    <E extends Number> void b(E e);

    void c(List<? extends Number> c);
}
