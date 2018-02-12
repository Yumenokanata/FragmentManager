package indi.yume.tools.fragmentmanager;

import indi.yume.tools.renderercalendar.R;
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
    @Builder.Default private int enterAnim = -1;
    @Builder.Default private int exitAnim = -1;
    @Builder.Default private int stayAnim = R.anim.stay_anim;

    public boolean isEmpty() {
        return enterAnim == -1 && exitAnim == -1;
    }
}
