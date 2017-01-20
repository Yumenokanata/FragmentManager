package indi.yume.tools.fragmentmanager;

import android.os.Looper;

import lombok.experimental.UtilityClass;

/**
 * Created by yume on 17-1-20.
 */
@UtilityClass
public class Utils {
    public static void checkThread(){
        if(Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("Must run on main thread!");
        }
    }
}
