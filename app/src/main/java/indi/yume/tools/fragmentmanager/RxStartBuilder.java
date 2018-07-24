package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import io.reactivex.Single;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

import static indi.yume.tools.fragmentmanager.ThrottleUtil.isThrottleOpen;

/**
 * Created by yume on 17-1-20.
 */
@AllArgsConstructor
@Data
public class RxStartBuilder {
    protected final Intent intent;
    @Wither protected boolean checkThrottle = isThrottleOpen();
    @Wither protected boolean enableAnimation = true;
    @Wither protected Class<? extends SingleBaseActivity> newActivity;
    @Wither @AnimRes protected int enterAnim = 0;
    @Wither @AnimRes protected int exitAnim = 0;
    @Wither @AnimRes protected int stayAnim = R.anim.stay_anim;

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
        if(enterAnim == 0 || exitAnim == 0)
            return null;

        return AnimData.builder()
                .enterAnim(enterAnim)
                .exitAnim(exitAnim)
                .stayAnim(stayAnim)
                .build();
    }

    public Single<Tuple2<Integer, Bundle>> startForObservable(BaseManagerFragment fragment) {
        return fragment.startForObservable(this);
    }
}
