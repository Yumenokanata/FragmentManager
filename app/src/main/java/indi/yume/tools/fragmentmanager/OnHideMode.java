package indi.yume.tools.fragmentmanager;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static indi.yume.tools.fragmentmanager.OnHideMode.ON_PAUSE;
import static indi.yume.tools.fragmentmanager.OnHideMode.ON_START_NEW;
import static indi.yume.tools.fragmentmanager.OnHideMode.ON_START_NEW_AFTER_ANIM;
import static indi.yume.tools.fragmentmanager.OnHideMode.ON_SWITCH;

/**
 * Created by yume on 16-8-31.
 *
 * ON_PAUSE: 1. when start new activity. 2. program switch to background.(just this activity is top)
 * ON_START_NEW: when start new fragment, always on call hide() and next fragment is created.
 * ON_START_NEW_AFTER_ANIM: when start new fragment and next fragment enter anim is over(if has anim).
 * ON_SWITCH: when switch to other tag.
 */
@IntDef({ON_PAUSE, ON_START_NEW, ON_SWITCH, ON_START_NEW_AFTER_ANIM})
@Retention(RetentionPolicy.SOURCE)
public @interface OnHideMode {
    int ON_PAUSE = 0x11;
    int ON_START_NEW = 0x22;
    int ON_SWITCH = 0x33;
    int ON_START_NEW_AFTER_ANIM = 0x44;

    public static class Util{
        public static String toString(@OnShowMode int showMode) {
            switch (showMode) {
                case ON_PAUSE:
                    return "onPause";
                case ON_START_NEW:
                    return "onStartNew";
                case ON_START_NEW_AFTER_ANIM:
                    return "onStartNewAfterAnim";
                case ON_SWITCH:
                    return "onSwitch";
                default:
                    return "None";
            }
        }
    }
}
