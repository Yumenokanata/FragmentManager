package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import indi.yume.tools.fragmentmanager.model.AnimData;
import io.reactivex.Maybe;
import kotlin.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

/**
 * Created by yume on 17-1-20.
 */
@AllArgsConstructor
@Data
public class RxStartBuilder {
    @Wither
    @Nullable
    private FragmentCreator creator = null;

    protected final Intent intent;
    @Wither
    private Class<? extends SingleBaseActivity> newActivity;
    @Wither
    @Nullable
    private String targetStack = null;
    @Wither
    protected boolean enableAnimation = true;
    @Wither
    @AnimRes
    protected int enterAnim = -1;
    @Wither
    @AnimRes
    protected int exitAnim = -1;
    @Wither
    @AnimRes
    protected int stayAnim = R.anim.stay_anim;

    protected RxStartBuilder(Intent intent) {
        this.intent = intent;
    }

    public static RxStartBuilder builder(Intent intent) {
        return new RxStartBuilder(intent);
    }

    public RxStartBuilder withAnimData(@Nullable AnimData anim) {
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

    public Maybe<Pair<Integer, Bundle>> startForObservable(FragmentItem fragment) {
        return ActionUtilKt.startForObservable(fragment, this).toMaybe();
    }


    //Make for Kotlin
    public Intent getIntent() {
        return intent;
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

    @Nullable
    public FragmentCreator getCreator() {
        return creator;
    }

    public void setCreator(@Nullable FragmentCreator creator) {
        this.creator = creator;
    }

    public Class<? extends SingleBaseActivity> getNewActivity() {
        return newActivity;
    }

    public void setNewActivity(Class<? extends SingleBaseActivity> newActivity) {
        this.newActivity = newActivity;
    }

    @Nullable
    public String getTargetStack() {
        return targetStack;
    }

    public void setTargetStack(@Nullable String targetStack) {
        this.targetStack = targetStack;
    }
}
