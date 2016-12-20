package indi.yume.tools.fragmentmanager;

import android.content.Intent;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

/**
 * Created by yume on 16-12-20.
 */
@AllArgsConstructor
public class StartBuilder {
    private final Intent intent;
    @Wither
    private int resultCode = -1;
    @Wither private boolean clearCurrentStack = false;
    @Wither private boolean checkThrottle = false;
    @Wither private boolean enableAnimation = true;
    @Wither private Class<? extends SingleBaseActivity> newActivity;

    private StartBuilder(Intent intent) {
        this.intent = intent;
    }

    public static StartBuilder builder(Intent intent) {
        return new StartBuilder(intent);
    }

    public void start(BaseManagerFragment fragment) {
        if(resultCode != -1) {
            if(newActivity != null)
                fragment.startFragmentOnNewActivityForResult(intent, newActivity, resultCode, checkThrottle, enableAnimation);
            else
                fragment.startFragmentForResult(intent, resultCode, false, checkThrottle, enableAnimation);
        } else {
            if(newActivity != null)
                fragment.startFragmentOnNewActivity(intent, newActivity, enableAnimation);
            else
                fragment.startFragment(intent, checkThrottle, enableAnimation);
        }
    }

    public void start(BaseFragmentManagerActivity activity) {
        if(resultCode != -1) {
            if(newActivity != null)
                activity.startFragmentOnNewActivityForResult(intent, newActivity, resultCode, checkThrottle, enableAnimation);
            else
                activity.startFragmentForResult(intent, resultCode, false, checkThrottle, enableAnimation);
        } else {
            if(newActivity != null)
                activity.startFragmentOnNewActivity(intent, newActivity, enableAnimation);
            else
                activity.startFragment(intent, checkThrottle, enableAnimation);
        }
    }
}
