package indi.yume.tools.fragmentmanager;

import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;

/**
 * Created by yume on 17-3-1.
 */
@UtilityClass
public class LogUtil {
    public interface State<S, A> {
        Tuple2<S, A> run(S oldState);
    }

    public class Tag implements State<String, String> {
        @Override
        public Tuple2<String, String> run(String oldState) {
            return null;
        }
    }

//    static String stackLog(Map<String, List<BaseManagerFragment>> fragmentMap) {
//
//    }
}
