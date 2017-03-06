package indi.yume.tools.fragmentmanager;

import java.io.Serializable;

import indi.yume.tools.renderercalendar.R;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by yume on 17-1-20.
 */
@Data
@Builder
@AllArgsConstructor
public class AnimData implements Serializable {
    private int enterAnim = -1;
    private int exitAnim = -1;
    private int stayAnim = R.anim.stay_anim;

    public boolean isEmpty() {
        return enterAnim == -1 && exitAnim == -1;
    }
}
