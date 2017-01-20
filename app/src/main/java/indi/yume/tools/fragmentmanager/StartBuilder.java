package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import indi.yume.tools.renderercalendar.R;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

/**
 * Created by yume on 16-12-20.
 */
@AllArgsConstructor
@Data
public class StartBuilder {
    private final Intent intent;
    @Wither private int requestCode = -1;
    @Wither private boolean clearCurrentStack = false;
    @Wither private boolean checkThrottle = false;
    @Wither private boolean enableAnimation = true;
    @Wither private Class<? extends SingleBaseActivity> newActivity;
    @Wither @AnimRes private int enterAnim = -1;
    @Wither @AnimRes private int exitAnim = -1;
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
        if(enterAnim == -1 || exitAnim == -1)
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
