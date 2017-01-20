package indi.yume.tools.fragmentmanager;

import android.os.Parcel;
import android.os.Parcelable;

import indi.yume.tools.renderercalendar.R;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created by yume on 17-1-20.
 */
@Data
@Builder
@AllArgsConstructor
public class AnimData implements Parcelable {
    private int enterAnim = -1;
    private int exitAnim = -1;
    private int stayAnim = R.anim.stay_anim;

    public boolean isEmpty() {
        return enterAnim == -1 && exitAnim == -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.enterAnim);
        dest.writeInt(this.exitAnim);
        dest.writeInt(this.stayAnim);
    }

    protected AnimData(Parcel in) {
        this.enterAnim = in.readInt();
        this.exitAnim = in.readInt();
        this.stayAnim = in.readInt();
    }

    public static final Parcelable.Creator<AnimData> CREATOR = new Parcelable.Creator<AnimData>() {
        @Override
        public AnimData createFromParcel(Parcel source) {
            return new AnimData(source);
        }

        @Override
        public AnimData[] newArray(int size) {
            return new AnimData[size];
        }
    };
}
