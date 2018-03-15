package indi.yume.tools.fragmentmanager.model;

import java.io.Serializable;

import indi.yume.tools.fragmentmanager.R;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yume on 17-3-27.
 */
@Data
@Builder
@AllArgsConstructor
public class AnimData implements Serializable {
    @Builder.Default private int enterAnim = -1;
    @Builder.Default private int exitAnim = -1;
    @Builder.Default private int stayAnim = R.anim.stay_anim;

    public boolean isEmpty() {
        return enterAnim == -1 && exitAnim == -1;
    }

    //Make for Kotlin
    public AnimData() {
        this.enterAnim = -1;
        this.exitAnim = -1;
        this.stayAnim = R.anim.stay_anim;
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
