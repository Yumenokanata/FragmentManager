package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import indi.yume.tools.renderercalendar.R;
import rx.Observable;
import rx.functions.Action0;

/**
 * Created by yume on 15/9/24.
 */
public abstract class BaseFragmentManagerActivity extends AppCompatActivity {
    private static final String SAVE_STATE_KEY_IS_START_FOR_RESULT = "is_start_for_result";
    private static final String SAVE_STATE_KEY_CURRENT_STACK_TAG = "current_stack_tag";
    private static final String SAVE_STATE_KEY_TAG_LIST_TAG = "tag_list_key";

    private Map<String, List<BaseManagerFragment>> fragmentMap = new HashMap<>();
    private Map<String, Class<? extends BaseManagerFragment>> baseFragmentMap;
    private String currentStackTag;
    private FragmentManager fragmentManager;
    private OnStackChangedListener mOnStackChangedListener;

    private String forObservableTag;

    private boolean isStartForResult = false;

    @AnimRes
    private int fragmentExitAnim = R.anim.fragment_left_exit;
    @AnimRes
    private int fragmentEnterAnim = R.anim.fragment_left_enter;
    @AnimRes
    private int activityEnterStayAnim = R.anim.stay_anim;

    private boolean isShowAnimWhenFinish = true;

    public abstract int fragmentViewId();

    public abstract Map<String, Class<? extends BaseManagerFragment>> BaseFragmentWithTag();

    public boolean clearStackWhenStackChanged(String targetTag, String currentTag){
        return false;
    }

    public void setOnStackChangedListener(OnStackChangedListener onStackChangedListener) {
        this.mOnStackChangedListener = onStackChangedListener;
    }

    protected String getCurrentStackTag() {
        return currentStackTag;
    }

    public int getCurrentStackSize() {
        String currentTag = getCurrentStackTag();
        if(currentTag == null)
            return 0;

        List<BaseManagerFragment> list = fragmentMap.get(currentTag);
        if(list == null || list.isEmpty())
            return 0;

        return list.size();
    }

    @Nullable
    public BaseManagerFragment getCurrentFragment() {
        String currentTag = getCurrentStackTag();
        if(currentTag == null)
            return null;

        List<BaseManagerFragment> list = fragmentMap.get(currentTag);
        if(list == null || list.isEmpty())
            return null;

        return list.get(list.size() - 1);
    }

    BaseManagerFragment getPreFragment() {
        List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
        if(list == null || list.size() <= 1)
            return null;

        return list.get(list.size() - 2);
    }

    void showPreFragment() {
        BaseManagerFragment fragment = getPreFragment();
        if(fragment == null)
            return;

        fragmentManager.beginTransaction()
                .show(fragment)
                .commitAllowingStateLoss();
    }

    void hidePreFragment() {
        BaseManagerFragment fragment = getPreFragment();
        if(fragment == null)
            return;

        fragmentManager.beginTransaction()
                .hide(fragment)
                .commitAllowingStateLoss();
    }

    public void setFragmentAnim(@AnimRes int enterAnim,
                                @AnimRes int exitAnim) {
        this.fragmentEnterAnim = enterAnim;
        this.fragmentExitAnim = exitAnim;
    }

    /**
     *
     * @param activityEnterStyAnim set anim when startNewActivity
     *                             {@link this#startFragmentOnNewActivity(Intent, Class)}
     *                             {@link this#startFragmentOnNewActivityForResult(Intent, Class, int)}
     *                             you can set 0 will not have anim (background is black),
     *                             this anim just work for stay old activity when start a new activity,
     *                             default this time is 2000ms:
     *                             <translate xmlns:android="http://schemas.android.com/apk/res/android"
     *                                 android:fromYDelta="0%p" android:toYDelta="0%p"
     *                                 android:duration="2000" />
     *                             please set animation time to fit your enter anim for start new activity.
     */
    public void setFragmentAnim(@AnimRes int enterAnim,
                                @AnimRes int exitAnim,
                                @AnimRes int activityEnterStyAnim) {
        this.fragmentEnterAnim = enterAnim;
        this.fragmentExitAnim = exitAnim;
        this.activityEnterStayAnim = activityEnterStyAnim;
    }

    public boolean isShowAnimWhenFinish() {
        return isShowAnimWhenFinish;
    }

    public void setIsShowAnimWhenFinish(boolean isShowAnimWhenFinish) {
        this.isShowAnimWhenFinish = isShowAnimWhenFinish;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forObservableTag = ActivityForObservableHelper.onCreate(savedInstanceState, this);
        fragmentManager = getSupportFragmentManager();
        baseFragmentMap = BaseFragmentWithTag();
        if(baseFragmentMap == null)
            throw new Error("BaseFragmentWithTag() must return value");

        if(savedInstanceState != null) {
            isStartForResult = savedInstanceState.getBoolean(SAVE_STATE_KEY_IS_START_FOR_RESULT, false);
            restoreManageData(savedInstanceState);
            String stackTag = savedInstanceState.getString(SAVE_STATE_KEY_CURRENT_STACK_TAG);
            if (!TextUtils.isEmpty(stackTag)) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                for(String tag : fragmentMap.keySet())
                    if(!TextUtils.equals(tag, stackTag))
                        hideStackByTag(tag, transaction);
                showStackByTagNoAnim(stackTag, transaction);
                transaction.commit();
                currentStackTag = stackTag;
            }
        }
    }

    private void restoreManageData(Bundle savedInstanceState) {
        List<String> tagList = savedInstanceState.getStringArrayList(SAVE_STATE_KEY_TAG_LIST_TAG);
        if(tagList == null)
            return;

        for(String tag : tagList) {
            List<String> fragmentHashList = savedInstanceState.getStringArrayList(tag);
            if(fragmentHashList != null) {
                List<BaseManagerFragment> fragmentList = new ArrayList<>();
                for(String fragmentHash : fragmentHashList) {
                    Fragment fragment = fragmentManager.findFragmentByTag(fragmentHash);
                    if(fragment instanceof BaseManagerFragment) {
                        ((BaseManagerFragment) fragment).setHashTag(fragmentHash);
                        fragmentList.add((BaseManagerFragment) fragment);
                    }
                }
                fragmentMap.put(tag, fragmentList);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ActivityForObservableHelper.onSaveInstanceState(forObservableTag, outState);
        outState.putBoolean(SAVE_STATE_KEY_IS_START_FOR_RESULT, isStartForResult);

        outState.putString(SAVE_STATE_KEY_CURRENT_STACK_TAG, currentStackTag);

        ArrayList<String> tagList = new ArrayList<>();
        for(Map.Entry<String, List<BaseManagerFragment>> entry : fragmentMap.entrySet()) {
            List<BaseManagerFragment> fragmentList = entry.getValue();
            if(fragmentList != null){
                ArrayList<String> fragmentCodeList = new ArrayList<>();
                for(BaseManagerFragment fragment : fragmentList)
                    fragmentCodeList.add(fragment.getHashTag());
                outState.putStringArrayList(entry.getKey(), fragmentCodeList);
                tagList.add(entry.getKey());
            }
        }
        outState.putStringArrayList(SAVE_STATE_KEY_TAG_LIST_TAG, tagList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityForObservableHelper.onDestroy(forObservableTag);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if(currentStackTag != null && fragmentMap != null) {
            List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
            if(list != null && !list.isEmpty())
                list.get(list.size() - 1).onShow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(currentStackTag != null && fragmentMap != null) {
            List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
            if(list != null && !list.isEmpty())
                list.get(0).onHide();
        }
    }

    public void startFragmentOnNewActivity(Intent intent, Class<? extends SingleBaseActivity> activityClazz){
        try {
            startActivity(SingleBaseActivity.createIntent(this, Class.forName(intent.getComponent().getClassName()), activityClazz, intent));
            overridePendingTransition(fragmentEnterAnim, activityEnterStayAnim);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void startFragmentOnNewActivityForResult(Intent intent, Class<? extends SingleBaseActivity> activityClazz, int requestCode){
        try {
            startActivityForResult(SingleBaseActivity.createIntent(this, Class.forName(intent.getComponent().getClassName()), activityClazz, intent), requestCode);
            overridePendingTransition(fragmentEnterAnim, activityEnterStayAnim);
            isStartForResult = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Observable<Tuple2<Integer, Bundle>> startActivityForObservable(Intent intent) {
        return ActivityForObservableHelper.startActivityForObservable(forObservableTag, this, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityForObservableHelper.onActivityResult(forObservableTag, requestCode, resultCode, data != null ? data.getExtras() : null);

        if(isStartForResult)
            if(!TextUtils.isEmpty(currentStackTag) && fragmentMap != null && fragmentMap.keySet().contains(currentStackTag)){
                List<BaseManagerFragment> fragmentList = fragmentMap.get(currentStackTag);
                if(fragmentList != null && !fragmentList.isEmpty())
                    fragmentList.get(fragmentList.size() - 1).onFragmentResult(requestCode,
                            resultCode,
                            data != null ? data.getExtras() : null);
            }
        isStartForResult = false;
    }

    public void startFragment(Intent intent){
        this.startFragment(intent, false);
    }

    public void startFragment(Intent intent, boolean clearCurrentStack){
        BaseManagerFragment fragment = getFragmentByIntent(intent);
        if(fragment == null)
            return;

        fragment.setIntent(intent);
        addToStack(fragment, clearCurrentStack);
    }

    private BaseManagerFragment getFragmentByIntent(Intent intent){
        Class clazz;
        try {
            clazz = Class.forName(intent.getComponent().getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        if(!BaseManagerFragment.class.isAssignableFrom(clazz))
            return null;
        BaseManagerFragment fragment;
        try {
            fragment = (BaseManagerFragment) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return fragment;
    }

    public boolean isSameWithCurrentStackTag(String tag){
        return TextUtils.equals(tag, currentStackTag);
    }

    public void switchToStackByTag(String tag){
        if(TextUtils.equals(tag, currentStackTag))
            this.switchToStackByTag(tag, false);
        else
            this.switchToStackByTag(tag, clearStackWhenStackChanged(tag, currentStackTag));
    }

    public void switchToStackByTag(String tag, boolean clearCurrentStack) {
        switchToStackByTag(tag, clearCurrentStack, false);
    }

    private void switchToStackByTag(String tag, boolean clearCurrentStack, boolean forceSwitch){
        if(!baseFragmentMap.containsKey(tag))
            throw new Error("Tag: " + tag + " not in baseFragmentMap. [BaseFragmentWithTag()]");

        if((fragmentMap.containsKey(tag) && (forceSwitch || !TextUtils.equals(tag, currentStackTag)))
                || (!fragmentMap.containsKey(tag) || fragmentMap.get(tag).size() == 0)){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if((fragmentMap.containsKey(currentStackTag) && fragmentMap.get(currentStackTag).size() != 0))
                if(clearCurrentStack)
                    clearStackByTag(currentStackTag, fragmentTransaction);
                else
                    hideStackByTag(currentStackTag, fragmentTransaction);

            showStackByTagNoAnim(tag, fragmentTransaction);
            currentStackTag = tag;
            fragmentTransaction.commit();
        }
    }

    public void clearCurrentStack(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if((fragmentMap.containsKey(currentStackTag) && fragmentMap.get(currentStackTag).size() != 0))
            clearStackByTag(currentStackTag, fragmentTransaction);

        showStackByTagNoAnim(currentStackTag, fragmentTransaction);
        fragmentTransaction.commit();
    }

    public void addToStack(BaseManagerFragment fragment){
        String tag = fragment.getStackTag();
        if(tag == null)
            tag = currentStackTag;

        if(TextUtils.equals(tag, currentStackTag))
            this.addToStack(fragment, false);
        else
            this.addToStack(fragment, clearStackWhenStackChanged(tag, currentStackTag));
    }

    public void addToStack(BaseManagerFragment fragment, boolean clearCurrentStack){
        String targetTag = fragment.getStackTag();
        if(targetTag == null)
            targetTag = currentStackTag;

        if(!fragmentMap.containsKey(targetTag))
            fragmentMap.put(targetTag, new ArrayList<BaseManagerFragment>());

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(clearCurrentStack)
            clearStackByTag(currentStackTag, fragmentTransaction);

        currentStackTag = targetTag;
        addFragmentWithAnim(currentStackTag, fragment);
    }

    private void clearStackByTag(String tag, FragmentTransaction fragmentTransaction){
        List<BaseManagerFragment> list = fragmentMap.get(tag);
        for(BaseManagerFragment fragment : list)
            fragmentTransaction.remove(fragment);
        list.clear();
    }

    private void hideStackByTag(String tag, FragmentTransaction fragmentTransaction){
        List<BaseManagerFragment> list = fragmentMap.get(tag);
        for(BaseManagerFragment fragment : list)
            if(!fragment.isHidden()) {
                fragmentTransaction.hide(fragment);
                //在每个调用本方法的位置之后都会调用showStackByTagNoAnim();而在此方法中会统一调用onHide方法。
//                fragment.onHide();
            }
    }

    private void showStackByTagNoAnim(String tag, FragmentTransaction fragmentTransaction){
        if(!fragmentMap.containsKey(tag))
            fragmentMap.put(tag, new LinkedList<BaseManagerFragment>());

        List<BaseManagerFragment> list = fragmentMap.get(tag);
        if(list.size() == 0) {
            BaseManagerFragment fragment = getFragmentByClass(baseFragmentMap.get(tag));
            if (fragment == null)
                throw new Error("baseFragmentMap [BaseFragmentWithTag()] has wrong");
            fragment.setIntent(getIntent());
            fragmentTransaction.add(fragmentViewId(), fragment, fragment.getHashTag());
            list.add(fragment);
            return;
        } else{
            BaseManagerFragment willShowFragment = list.get(list.size() - 1);
            for(Map.Entry<String, List<BaseManagerFragment>> entry : fragmentMap.entrySet())
                for(BaseManagerFragment f : entry.getValue())
                    if(willShowFragment != f && !f.isHidden()) {
                        fragmentTransaction.hide(f);
                        f.onHide();
                    }
            fragmentTransaction.show(willShowFragment);
            willShowFragment.onShow();
        }

//        for(BaseManagerFragment fragment : list)
//            fragmentTransaction.show(fragment);
    }

    private BaseManagerFragment getFragmentByClass(Class clazz){
        if(!BaseManagerFragment.class.isAssignableFrom(clazz))
            return null;
        BaseManagerFragment fragment;
        try {
            fragment = (BaseManagerFragment) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return fragment;
    }

    public void removeFragment(BaseManagerFragment fragment){
        for(String key : fragmentMap.keySet())
            for(BaseManagerFragment f : fragmentMap.get(key))
                if(f == fragment) {
                    if (key != null
                            && key.equals(currentStackTag)
                            && f == fragmentMap.get(key).get(fragmentMap.get(key).size() - 1)) {
                        removeFragmentWithAnim(currentStackTag);
                    } else {
                        fragmentManager.beginTransaction()
                                .remove(fragment)
                                .commit();
                        fragmentMap.get(key).remove(f);
                    }
                    break;
                }
    }

    public void removeFragmentWithoutAnim(String tag) {
        List<BaseManagerFragment> list = fragmentMap.get(tag);
        if(list.size() <= 1) {
            if(list.size() == 1) {
                BaseManagerFragment fragment = list.get(0);
                fragment.preBackResultData();
                Intent intent = new Intent();
                if(fragment.getResultData() != null)
                    intent.putExtras(fragment.getResultData());
                setResult(fragment.getRequestCode(), intent);
            }
            supportFinishAfterTransition();
        } else {
            final BaseManagerFragment fragment = list.get(list.size() - 1);
            list.remove(fragment);

            final BaseManagerFragment fragment1 = list.get(list.size() - 1);
            fragment.preBackResultData();
            if(fragment.getRequestCode() != -1)
                fragment1.onFragmentResult(fragment.getRequestCode(),
                        fragment.getResultCode(),
                        fragment.getResultData());

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            showStackByTagNoAnim(currentStackTag, fragmentTransaction1);
            fragmentTransaction.show(fragment1);

            fragmentTransaction.remove(fragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
        if(!list.isEmpty()) {
            BaseManagerFragment fragment = list.get(list.size() - 1);
            if(fragment.onBackPressed())
                return;
        }
        if(onBackPressed(getCurrentStackSize()))
            return;

        removeFragmentWithAnim(currentStackTag);
    }

    public boolean onBackPressed(int currentStackSize) {
        return false;
    }

    private void removeFragmentWithAnim(String tag) {
        List<BaseManagerFragment> list = fragmentMap.get(tag);
        if(list.size() <= 1) {
            if(list.size() == 1) {
                BaseManagerFragment fragment = list.get(0);
                fragment.preBackResultData();
                Intent intent = new Intent();
                if(fragment.getResultData() != null)
                    intent.putExtras(fragment.getResultData());
                setResult(fragment.getRequestCode(), intent);
            }
            supportFinishAfterTransition();
            if(isShowAnimWhenFinish)
                overridePendingTransition(0, fragmentExitAnim);
        } else {
            final BaseManagerFragment fragment = list.get(list.size() - 1);
            list.remove(fragment);

            final BaseManagerFragment fragment1 = list.get(list.size() - 1);
            fragment.preBackResultData();
            if(fragment.getRequestCode() != -1)
                fragment1.onFragmentResult(fragment.getRequestCode(),
                        fragment.getResultCode(),
                        fragment.getResultData());

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            showStackByTagNoAnim(currentStackTag, fragmentTransaction1);
            fragmentTransaction.show(fragment1);

            if(fragmentExitAnim == 0) {
                fragmentTransaction.remove(fragment)
                        .commit();
            } else {
                fragmentTransaction.commit();

                startAnimation(fragmentExitAnim,
                        fragment.getView(),
                        new Action0() {
                            @Override
                            public void call() {
                                fragmentManager.beginTransaction()
                                        .remove(fragment)
                                        .commit();
                                fragment1.onShow();
                            }
                        });
            }
        }
    }

    private void addFragmentWithAnim(String tag,
                                     final BaseManagerFragment nextFragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        List<BaseManagerFragment> list = fragmentMap.get(tag);
        if(list.size() > 0 && fragmentEnterAnim != 0) {
            final BaseManagerFragment backFragment = list.get(list.size() - 1);
            nextFragment.setOnCreatedViewListener(new BaseManagerFragment.OnCreatedViewListener() {
                @Override
                public void onCreatedView(View view) {
                    startAnimation(fragmentEnterAnim,
                            view,
                            new Action0() {
                                @Override
                                public void call() {
                                    fragmentManager.beginTransaction()
                                            .hide(backFragment)
                                            .commit();
                                    backFragment.onHide();
                                    nextFragment.onShow();
                                }
                            });
                }
            });

            fragmentTransaction.show(backFragment);
            fragmentTransaction.add(fragmentViewId(), nextFragment, nextFragment.getHashTag());
            fragmentTransaction.commit();
        } else {
            fragmentTransaction.add(fragmentViewId(), nextFragment, nextFragment.getHashTag());
            fragmentTransaction.commit();
        }
        list.add(nextFragment);
    }

    private void startAnimation(@AnimRes int animRes, View view, final Action0 doOnOver) {
        if(view.getBackground() == null)
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

        Animation animation = AnimationUtils.loadAnimation(this, animRes);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doOnOver.call();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    public static interface OnStackChangedListener {
        boolean onStackChanged(String targetTag, String currentTag);
    }
}
