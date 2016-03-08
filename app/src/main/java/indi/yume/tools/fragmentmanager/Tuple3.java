package indi.yume.tools.fragmentmanager;

/**
 * Created by yume on 16/3/8.
 */
public class Tuple3<T1, T2, T3> {
    private T1 data1;
    private T2 data2;
    private T3 data3;

    public Tuple3() {
    }

    private Tuple3(T1 data1, T2 data2, T3 data3) {
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
    }

    public static <K1, K2, K3> Tuple3<K1, K2, K3> of(K1 data1, K2 data2, K3 data3) {
        return new Tuple3<>(data1, data2, data3);
    }

    public T1 getData1() {
        return data1;
    }

    public T2 getData2() {
        return data2;
    }

    public T3 getData3() {
        return data3;
    }
}
