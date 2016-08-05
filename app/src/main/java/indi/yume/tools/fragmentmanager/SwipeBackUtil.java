package indi.yume.tools.fragmentmanager;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import lombok.experimental.UtilityClass;

/**
 * Created by yume on 16-8-5.
 *
 * Use this Util, you can add SwipeBack to your Fragment or Activity (SingleBaseActivity).
 *
 * But your Activity must use this style:
 * 1. NoActionBar
 * 2.<item name="android:windowIsTranslucent">true</item>
 *   (If you use SwipeBack for Activity, Translucent is necessary.)
 */
@UtilityClass
public class SwipeBackUtil {
    /*
     * Please use this func at BaseManagerFragment's onCreateView().
     *
     * eg:
     * <p>
     *     @Override
     *     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     *         View view = inflater.inflate(R.layout.fragment_blank_fragment12, container, false);
     *         return mSwipeBackLayout.attachToFragment(this, view);
     *     }
     * </p>
     */
    public static View enableSwipeBackAtFragment(BaseManagerFragment fragment, View view) {
        SwipeBackLayout mSwipeBackLayout = new SwipeBackLayout(fragment.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSwipeBackLayout.setLayoutParams(params);
        mSwipeBackLayout.setBackgroundColor(Color.TRANSPARENT);

        return mSwipeBackLayout.attachToFragment(fragment, view);
    }

    /*
     * Please use this func at BaseFragmentManagerActivity's onPostCreate().
     * Suggest use SwipeBack for SingleBaseActivity.
     *
     * eg:
     * <p>
     *     @Override
     *     protected void onPostCreate(Bundle savedInstanceState) {
     *         super.onPostCreate(savedInstanceState);
     *         enableSwipeBackAtActivity(this);
     *     }
     * </p>
     */
    public static void enableSwipeBackAtActivity(BaseFragmentManagerActivity activity) {
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        activity.getWindow().getDecorView().setBackgroundDrawable(null);
        SwipeBackLayout swipeBackLayout = new SwipeBackLayout(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        swipeBackLayout.setLayoutParams(params);

        swipeBackLayout.attachToActivity(activity);
    }
}
