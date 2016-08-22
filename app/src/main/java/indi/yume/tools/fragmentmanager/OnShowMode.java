package indi.yume.tools.fragmentmanager;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static indi.yume.tools.fragmentmanager.OnShowMode.ON_BACK;
import static indi.yume.tools.fragmentmanager.OnShowMode.ON_CREATE;
import static indi.yume.tools.fragmentmanager.OnShowMode.ON_RESUME;
import static indi.yume.tools.fragmentmanager.OnShowMode.ON_SWITCH;

/**
 * Created by yume on 16-8-22.
 */
@IntDef({ON_RESUME, ON_BACK, ON_SWITCH, ON_CREATE})
@Retention(RetentionPolicy.SOURCE)
public @interface OnShowMode {
    int ON_RESUME = 0x11;
    int ON_BACK = 0x22;
    int ON_SWITCH = 0x33;
    int ON_CREATE = 0x44;

    public static class Util{
        public static String toString(@OnShowMode int showMode) {
            switch (showMode) {
                case ON_RESUME:
                    return "onResume";
                case ON_BACK:
                    return "onBack";
                case ON_SWITCH:
                    return "onSwitch";
                case ON_CREATE:
                    return "onCreate";
                default:
                    return "None";
            }
        }
    }
}
