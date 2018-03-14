package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import indi.yume.tools.fragmentmanager.model.AnimData;
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
    @Wither
    private int requestCode = -1;
    @Wither
    private boolean enableAnimation = true;
    @Wither
    @AnimRes
    private int enterAnim = -1;
    @Wither
    @AnimRes
    private int exitAnim = -1;
    @Wither
    @AnimRes
    private int stayAnim = R.anim.stay_anim;

    private StartBuilder(Intent intent) {
        this.intent = intent;
    }

    public StartBuilder(Intent intent, int requestCode, boolean enableAnimation) {
        this.intent = intent;
        this.requestCode = requestCode;
        this.enableAnimation = enableAnimation;
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

    public void start(ManageableFragment fragment) {
        fragment.start(this);
    }

    public void start(ManageableActivity activity) {
        activity.start(this);
    }

    //Make for Kotlin
    public Intent getIntent() {
        return intent;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public boolean isEnableAnimation() {
        return enableAnimation;
    }

    public void setEnableAnimation(boolean enableAnimation) {
        this.enableAnimation = enableAnimation;
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public void setEnterAnim(int enterAnim) {
        this.enterAnim = enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public void setExitAnim(int exitAnim) {
        this.exitAnim = exitAnim;
    }

    public int getStayAnim() {
        return stayAnim;
    }

    public void setStayAnim(int stayAnim) {
        this.stayAnim = stayAnim;
    }
}
