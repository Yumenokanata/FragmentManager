package indi.yume.tools.fragmentmanager;

/**
 * Created by yume on 16/3/8.
 */
public class Tuple2<T1, T2> {
    private T1 data1;
    private T2 data2;

    public Tuple2() {
    }

    private Tuple2(T1 data1, T2 data2) {
        this.data1 = data1;
        this.data2 = data2;
    }

    public static <K1, K2> Tuple2<K1, K2> of(K1 data1, K2 data2) {
        return new Tuple2<>(data1, data2);
    }

    public T1 getData1() {
        return data1;
    }

    public T2 getData2() {
        return data2;
    }

    @Override
    public String toString() {
        return "Tuple2[data1=" + String.valueOf(data1) + ", data2=" + String.valueOf(data2) + "]";
    }
}
