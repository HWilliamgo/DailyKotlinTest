/**
 * date: 2020/12/29
 * author: hwj
 * description:
 */
public class Teacher extends Person {
    public boolean canTeach = false;

    {
        System.out.println("Teacher instance init bloack");
    }

    @Override
    public void init() {
        super.init();
        System.out.println("Teacher.init");
        canTeach = true;
    }
}
