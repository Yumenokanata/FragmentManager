package indi.yume.tools.fragmentmanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by yume on 17-1-20.
 */
@Data
@Builder
@AllArgsConstructor
public class AnimData implements Serializable {
    @Builder.Default private int enterAnim = 0;
    @Builder.Default private int exitAnim = 0;
    @Builder.Default private int stayAnim = R.anim.stay_anim;

    public boolean isEmpty() {
        return enterAnim == 0 && exitAnim == 0;
    }
}
