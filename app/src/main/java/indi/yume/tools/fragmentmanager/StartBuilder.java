package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

import static indi.yume.tools.fragmentmanager.ThrottleUtil.isThrottleOpen;

/**
 * Created by yume on 16-12-20.
 */
@AllArgsConstructor
@Data
public class StartBuilder {
    public static boolean DEFAULT_ENABLE_ANIMATION = true;

    private final Intent intent;
    @Wither private int requestCode = -1;
    @Wither private boolean clearCurrentStack = false;
    @Wither private boolean checkThrottle = isThrottleOpen();
    @Wither private boolean enableAnimation = DEFAULT_ENABLE_ANIMATION;
    @Wither private Class<? extends SingleBaseActivity> newActivity;
    @Wither @AnimRes private int enterAnim = 0;
    @Wither @AnimRes private int exitAnim = 0;
    @Wither @AnimRes private int stayAnim = R.anim.stay_anim;

    private StartBuilder(Intent intent) {
        this.intent = intent;
    }

    public static StartBuilder builder(Intent intent) {
        return new StartBuilder(intent);
    }

    public StartBuilder withAnimData(@Nullable AnimData anim) {
        if(anim == null)
            return this;

        return withEnterAnim(anim.getEnterAnim())
                .withExitAnim(anim.getExitAnim())
                .withStayAnim(anim.getStayAnim());
    }

    @Nullable
    public AnimData getAnim() {
        if(enterAnim == 0 || exitAnim == 0)
            return null;

        return AnimData.builder()
                .enterAnim(enterAnim)
                .exitAnim(exitAnim)
                .stayAnim(stayAnim)
                .build();
    }

    public void start(BaseManagerFragment fragment) {
        fragment.start(this);
    }

    public void start(BaseFragmentManagerActivity activity) {
        activity.start(this);
    }
}
