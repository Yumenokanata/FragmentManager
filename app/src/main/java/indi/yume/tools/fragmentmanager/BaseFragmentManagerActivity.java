package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import io.reactivex.Single;

import java.util.*;
import java.lang.InstantiationException;

import static indi.yume.tools.fragmentmanager.BaseManagerFragment.*;
import static indi.yume.tools.fragmentmanager.Utils.checkThread;

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

    public abstract Map<String, Class<? extends BaseManagerFragment>> baseFragmentWithTag();

    public boolean clearStackWhenStackChanged(String targetTag, String currentTag){
        return false;
    }

    public void setOnStackChangedListener(OnStackChangedListener onStackChangedListener) {
        this.mOnStackChangedListener = onStackChangedListener;
    }

    @AnimRes
    private int getExitAnim(BaseManagerFragment fragment) {
        AnimData anim = fragment.getIntentAnim();
        int fragmentAnim = fragment.provideExitAnim();
        if(anim != null && !anim.isEmpty())
            return anim.getExitAnim();
        else if(fragmentAnim == -1)
            return fragmentExitAnim;
        else
            return fragmentAnim;
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

        commitFragmentTransaction(fragmentManager.beginTransaction()
                .show(fragment));
    }

    void hidePreFragment() {
        BaseManagerFragment fragment = getPreFragment();
        if(fragment == null)
            return;

        commitFragmentTransaction(fragmentManager.beginTransaction()
                .hide(fragment));
    }

    public void setDefaultFragmentAnim(@AnimRes int enterAnim,
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
    public void setDefaultFragmentAnim(@AnimRes int enterAnim,
                                       @AnimRes int exitAnim,
                                       @AnimRes int activityEnterStyAnim) {
        this.fragmentEnterAnim = enterAnim;
        this.fragmentExitAnim = exitAnim;
        this.activityEnterStayAnim = activityEnterStyAnim;
    }

    public void commitFragmentTransaction(FragmentTransaction transaction) {
        transaction.commit();
    }

    public boolean isShowAnimWhenFinish() {
        return isShowAnimWhenFinish;
    }

    public void setIsShowAnimWhenFinish(boolean isShowAnimWhenFinish) {
        this.isShowAnimWhenFinish = isShowAnimWhenFinish;
    }

    private void fragmentOnCreateShow(final BaseManagerFragment fragment) {
        fragment.setOnCreatedViewListener(new BaseManagerFragment.OnCreatedViewListener() {

            @Override
            public void onCreatedView(View view) {
                fragment.onShow(OnShowMode.ON_CREATE);
                fragment.setOnCreatedViewListener(null);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forObservableTag = ActivityForObservableHelper.onCreate(savedInstanceState, this);
        fragmentManager = getSupportFragmentManager();
        baseFragmentMap = baseFragmentWithTag();
        if(baseFragmentMap == null)
            throw new Error("baseFragmentWithTag() must return value");

        if(savedInstanceState != null) {
            isStartForResult = savedInstanceState.getBoolean(SAVE_STATE_KEY_IS_START_FOR_RESULT, false);
            restoreManageData(savedInstanceState);
            String stackTag = savedInstanceState.getString(SAVE_STATE_KEY_CURRENT_STACK_TAG);
            if (!TextUtils.isEmpty(stackTag)) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                for(String tag : fragmentMap.keySet())
                    if(!TextUtils.equals(tag, stackTag))
                        hideStackByTag(tag, transaction);
                BaseManagerFragment fragment = showStackByTagNoAnim(stackTag, transaction);
                commitFragmentTransaction(transaction);
                if(fragment != null)
                    fragmentOnCreateShow(fragment);
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
                list.get(list.size() - 1).onShow(OnShowMode.ON_RESUME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(currentStackTag != null && fragmentMap != null) {
            List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
            if(list != null && !list.isEmpty())
                list.get(list.size() - 1).onHide(OnHideMode.ON_PAUSE);
        }
    }

    @Nullable
    private AnimData compareAnim(AnimData builderAnim, Class fragmentClass, AnimData defaultAnim) {
        if(builderAnim != null && !builderAnim.isEmpty())
            return builderAnim;

        BaseManagerFragment fragment = getFragmentByClass(fragmentClass);
        if(fragment != null
                && (fragment.provideEnterAnim() != -1 || fragment.provideExitAnim() != -1))
            return AnimData.builder()
                    .enterAnim(fragment.provideEnterAnim())
                    .exitAnim(fragment.provideExitAnim())
                    .stayAnim(fragment.provideStayAnim())
                    .build();

        return defaultAnim != null && !defaultAnim.isEmpty() ? defaultAnim : null;
    }

    public void start(StartBuilder builder) {
        if(builder.isCheckThrottle() && !ThrottleUtil.checkEvent())
            return;

        final Intent intent = builder.getIntent();
        int requestCode = builder.getRequestCode();
        boolean clearCurrentStack = builder.isClearCurrentStack();
        boolean enableAnimation = builder.isEnableAnimation();
        Class<? extends SingleBaseActivity> newActivity = builder.getNewActivity();

        Class fragmentClazz;
        try {
            fragmentClazz = Class.forName(intent.getComponent().getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if(newActivity != null) {
            if(requestCode != -1) {
                startActivityForResult(SingleBaseActivity.createIntent(this, fragmentClazz, newActivity, intent), requestCode);
                isStartForResult = true;
            } else {
                startActivity(SingleBaseActivity.createIntent(this, fragmentClazz, newActivity, intent));
            }
            if(enableAnimation) {
                AnimData anim = compareAnim(builder.getAnim(), fragmentClazz, defaultAnimData());
                overridePendingTransition(anim);
                intent.putExtra(INTENT_KEY_ANIM_DATA, anim);
            } else
                overridePendingTransition(-1, -1);
        } else {
            checkThread();
            AnimData anim = enableAnimation ?
                    compareAnim(builder.getAnim(), fragmentClazz, defaultAnimData())
                    : null;

            BaseManagerFragment fragment = getFragmentByIntent(intent);
            if(fragment == null)
                return;

            if(requestCode != -1)
                intent.putExtra(INTENT_KEY_REQUEST_CODE, requestCode);
            intent.putExtra(INTENT_KEY_ANIM_DATA, anim);
            getIntent().putExtra(INTENT_KEY_STACK_TAG,
                    fragment.getStackTag() == null ? getCurrentStackTag() : fragment.getStackTag());

            fragment.setIntent(intent);
            addToStack(fragment, clearCurrentStack, anim);
        }
    }

    private void overridePendingTransition(AnimData anim) {
        if(anim != null && !anim.isEmpty())
            overridePendingTransition(anim.getEnterAnim(), anim.getStayAnim());
        else
            overridePendingTransition(-1, -1);
    }

    @Nullable
    private AnimData defaultAnimData() {
        if(fragmentEnterAnim == -1 || fragmentExitAnim == -1)
            return null;
        return AnimData.builder()
                .enterAnim(fragmentEnterAnim)
                .exitAnim(fragmentExitAnim)
                .stayAnim(activityEnterStayAnim)
                .build();
    }

    public void startFragmentOnNewActivity(Intent intent, Class<? extends SingleBaseActivity> activityClazz){
        startFragmentOnNewActivity(intent, activityClazz, defaultAnimData());
    }

    public void startFragmentOnNewActivity(Intent intent,
                                           Class<? extends SingleBaseActivity> activityClazz,
                                           @Nullable AnimData anim){
        start(StartBuilder.builder(intent)
                .withCheckThrottle(true)
                .withNewActivity(activityClazz)
                .withAnimData(anim));
    }

    public void startFragmentOnNewActivityForResult(Intent intent, Class<? extends SingleBaseActivity> activityClazz, int requestCode){
        startFragmentOnNewActivityForResult(intent, activityClazz, requestCode, true);
    }

    void startFragmentOnNewActivityForResult(Intent intent,
                                             Class<? extends SingleBaseActivity> activityClazz,
                                             int requestCode,
                                             boolean checkThrottle) {
        startFragmentOnNewActivityForResult(intent, activityClazz, requestCode, checkThrottle, true);
    }

    void startFragmentOnNewActivityForResult(Intent intent,
                                             Class<? extends SingleBaseActivity> activityClazz,
                                             int requestCode,
                                             boolean checkThrottle,
                                             boolean withAnimation){
        start(StartBuilder.builder(intent)
                .withNewActivity(activityClazz)
                .withRequestCode(requestCode)
                .withCheckThrottle(checkThrottle)
                .withAnimData(withAnimation ? defaultAnimData() : null));
    }

    public Single<Tuple2<Integer, Bundle>> startActivityForObservable(Intent intent) {
        if(!ThrottleUtil.checkEvent())
            return Single.error(new ThrottleException());

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
        this.startFragment(intent, clearCurrentStack, true);
    }
    public void startFragment(Intent intent, boolean clearCurrentStack, boolean withAnimation){
        start(StartBuilder.builder(intent)
                .withClearCurrentStack(clearCurrentStack)
                .withEnableAnimation(withAnimation));
    }

    void startFragmentForResult(Intent intent,
                                int requestCode,
                                boolean clearCurrentStack,
                                boolean checkThrottle,
                                boolean withAnimation){
        BaseManagerFragment.startFragmentForResult(this, intent, requestCode, clearCurrentStack,
                checkThrottle, withAnimation);
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
        switchToStackByTag(tag, clearCurrentStack, forceSwitch, null);
    }

    private void switchToStackByTag(String tag, boolean clearCurrentStack, boolean forceSwitch, @Nullable Intent newIntent){
        if(!baseFragmentMap.containsKey(tag))
            throw new Error("Tag: " + tag + " not in baseFragmentMap. [baseFragmentWithTag()]");

        if((fragmentMap.containsKey(tag) && (forceSwitch || !TextUtils.equals(tag, currentStackTag)))
                || (!fragmentMap.containsKey(tag) || fragmentMap.get(tag).isEmpty())){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if((fragmentMap.containsKey(currentStackTag) && !fragmentMap.get(currentStackTag).isEmpty()))
                if(clearCurrentStack)
                    clearStackByTag(currentStackTag, fragmentTransaction);
                else
                    hideStackByTag(currentStackTag, fragmentTransaction);

            BaseManagerFragment fragment = showStackByTagNoAnim(tag, newIntent, fragmentTransaction);
            String oldTag = currentStackTag;
            currentStackTag = tag;
            commitFragmentTransaction(fragmentTransaction);

            if(fragmentMap.containsKey(oldTag) && !fragmentMap.get(oldTag).isEmpty()) {
                List<BaseManagerFragment> list = fragmentMap.get(oldTag);
                list.get(list.size() - 1).onHide(OnHideMode.ON_SWITCH);
            }

            if(fragment != null) {
                fragmentOnCreateShow(fragment);
            } else {
                BaseManagerFragment currentFragment = getCurrentFragment();
                if(currentFragment != null) {
                    if(newIntent != null)
                        currentFragment.onNewIntent(newIntent);
                    currentFragment.onShow(OnShowMode.ON_SWITCH);
                }
            }
        }
    }

    public void switchToAndClear(String tag, boolean resetTag, Intent newIntent){
        currentStackTag = tag;
        clearStack(tag, resetTag, newIntent);
    }

    public void clearStack(String tag, boolean resetTag, Intent newIntent){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if((fragmentMap.containsKey(tag) && fragmentMap.get(tag).size() != 0))
            clearStackByTag(tag, fragmentTransaction);

        if(!resetTag) {
            commitFragmentTransaction(fragmentTransaction);
            return;
        }

        BaseManagerFragment fragment = showStackByTagNoAnim(tag, newIntent, fragmentTransaction);
        if(fragment != null && !TextUtils.equals(tag, currentStackTag))
            fragmentTransaction.hide(fragment);
        commitFragmentTransaction(fragmentTransaction);

        if(fragment != null) {
            fragment.onShow(OnShowMode.ON_CREATE);
        } else {
            BaseManagerFragment currentFragment = getCurrentFragment();
            if(currentFragment != null) {
                if(newIntent != null)
                    currentFragment.onNewIntent(newIntent);
                currentFragment.onShow(OnShowMode.ON_SWITCH);
            }
        }
    }

    public void clearCurrentStack(){
        clearCurrentStack(false);
    }

    public void clearCurrentStack(boolean resetCurrentTag){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if((fragmentMap.containsKey(currentStackTag) && !fragmentMap.get(currentStackTag).isEmpty()))
            clearStackByTag(currentStackTag, fragmentTransaction);

        if(!resetCurrentTag) {
            commitFragmentTransaction(fragmentTransaction);
            return;
        }

        BaseManagerFragment fragment = showStackByTagNoAnim(currentStackTag, fragmentTransaction);
        commitFragmentTransaction(fragmentTransaction);
        if(fragment != null) {
            fragmentOnCreateShow(fragment);
        } else {
            BaseManagerFragment currentFragment = getCurrentFragment();
            if(currentFragment != null)
                currentFragment.onShow(OnShowMode.ON_SWITCH);
        }
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
        addToStack(fragment, clearCurrentStack, true);
    }

    public void addToStack(BaseManagerFragment fragment, boolean clearCurrentStack, boolean withAnimation){
        addToStack(fragment, clearCurrentStack, withAnimation ? defaultAnimData() : null);
    }

    public void addToStack(BaseManagerFragment fragment, boolean clearCurrentStack, AnimData animData){
        String targetTag = fragment.getStackTag();
        if(targetTag == null)
            targetTag = currentStackTag;

        if(!fragmentMap.containsKey(targetTag))
            fragmentMap.put(targetTag, new ArrayList<BaseManagerFragment>());

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(clearCurrentStack)
            clearStackByTag(currentStackTag, fragmentTransaction);

        currentStackTag = targetTag;
        addFragment(currentStackTag, fragment, animData);
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

    private BaseManagerFragment showStackByTagNoAnim(String tag, FragmentTransaction fragmentTransaction){
        return showStackByTagNoAnim(tag, null, fragmentTransaction);
    }

    /**
     *
     * @param tag
     * @param fragmentTransaction
     *
     * @return is create new.
     */
    private BaseManagerFragment showStackByTagNoAnim(String tag,
                                                     @Nullable Intent intent,
                                                     FragmentTransaction fragmentTransaction){
        if(!fragmentMap.containsKey(tag))
            fragmentMap.put(tag, new LinkedList<BaseManagerFragment>());

        List<BaseManagerFragment> list = fragmentMap.get(tag);
        if(list.isEmpty()) {
            BaseManagerFragment fragment = getFragmentByClass(baseFragmentMap.get(tag));
            if (fragment == null)
                throw new Error("baseFragmentMap [baseFragmentWithTag()] has wrong");
            getIntent().putExtra(INTENT_KEY_STACK_TAG, tag);
            fragment.setIntent(intent != null ? intent : getIntent());
            fragmentTransaction.add(fragmentViewId(), fragment, fragment.getHashTag());
            list.add(fragment);
            return fragment;
        } else{
            BaseManagerFragment willShowFragment = list.get(list.size() - 1);
            for(Map.Entry<String, List<BaseManagerFragment>> entry : fragmentMap.entrySet())
                for(BaseManagerFragment f : entry.getValue())
                    if(willShowFragment != f && !f.isHidden()) {
                        fragmentTransaction.hide(f);
                    }
            if(intent != null)
                willShowFragment.onNewIntent(intent);
            fragmentTransaction.show(willShowFragment);
            return null;
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
                        int preIndex;
                        if ((preIndex = fragmentMap.get(key).indexOf(f) - 1) >= 0) {
                            BaseManagerFragment fragment1 = fragmentMap.get(key).get(preIndex);
                            fragment.preBackResultData();
                            if(fragment.getRequestCode() != -1)
                                fragment1.onFragmentResult(fragment.getRequestCode(),
                                        fragment.getResultCode(),
                                        fragment.getResultData());
                        }

                        commitFragmentTransaction(
                                fragmentManager.beginTransaction()
                                        .remove(fragment));
                        fragmentMap.get(key).remove(f);
                    }
                    break;
                }
    }

    public void removeFragmentWithoutAnim(BaseManagerFragment fragment){
        for(String key : fragmentMap.keySet())
            for(BaseManagerFragment f : fragmentMap.get(key))
                if(f == fragment) {
                    removeFragmentWithoutAnim(key);
                    return;
                }
    }

    public boolean isTopOfStack(BaseManagerFragment fragment) {
        for(List<BaseManagerFragment> stack : fragmentMap.values())
            if(!stack.isEmpty() && stack.get(stack.size() - 1) == fragment)
                return true;

        return false;
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
                setResult(fragment.getResultCode(), intent);
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

            commitFragmentTransaction(fragmentTransaction.remove(fragment));
            fragment1.onShow(OnShowMode.ON_BACK);
        }
    }

    @Override
    public void onBackPressed() {
        List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
        if(list != null && !list.isEmpty()) {
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
        list = list == null ? new LinkedList<BaseManagerFragment>() : list;
        if(list.size() <= 1) {
            int exitAnim = -1;
            if(list.size() == 1) {
                BaseManagerFragment fragment = list.get(0);
                fragment.preBackResultData();
                Intent intent = new Intent();
                if(fragment.getResultData() != null)
                    intent.putExtras(fragment.getResultData());
                setResult(fragment.getResultCode(), intent);
                exitAnim = getExitAnim(fragment);
            }
            supportFinishAfterTransition();
            if(isShowAnimWhenFinish)
                overridePendingTransition(0, exitAnim);
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

            int exitAnim = getExitAnim(fragment);
            if(exitAnim == -1) {
                commitFragmentTransaction(fragmentTransaction.remove(fragment));
            } else {
                commitFragmentTransaction(fragmentTransaction);

                startAnimation(exitAnim,
                        fragment.getView(),
                        new Runnable() {
                            @Override
                            public void run() {
                                commitFragmentTransaction(fragmentManager.beginTransaction()
                                        .remove(fragment));
                                fragment1.onShow(OnShowMode.ON_BACK);
                            }
                        });
            }
        }
    }

    private void addFragment(String tag,
                             final BaseManagerFragment nextFragment,
                             final @Nullable AnimData anim) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        List<BaseManagerFragment> list = fragmentMap.get(tag);
        if(!list.isEmpty() && anim != null && !anim.isEmpty()) {
            final BaseManagerFragment backFragment = list.get(list.size() - 1);
            nextFragment.setOnCreatedViewListener(new BaseManagerFragment.OnCreatedViewListener() {
                @Override
                public void onCreatedView(View view) {
                    backFragment.onHide(OnHideMode.ON_START_NEW_AFTER_ANIM);
                    nextFragment.onShow(OnShowMode.ON_CREATE);
                    startAnimation(anim.getEnterAnim(),
                            view,
                            new Runnable() {
                                @Override
                                public void run() {
                                    commitFragmentTransaction(fragmentManager.beginTransaction()
                                            .hide(backFragment));
                                    backFragment.onHide(OnHideMode.ON_START_NEW);
                                    nextFragment.onShow(OnShowMode.ON_CREATE_AFTER_ANIM);
                                    nextFragment.setOnCreatedViewListener(null);
                                }
                            });
                }
            });

            fragmentTransaction.show(backFragment);
            fragmentTransaction.add(fragmentViewId(), nextFragment, nextFragment.getHashTag());
            commitFragmentTransaction(fragmentTransaction);
        } else if(!list.isEmpty()) {
            final BaseManagerFragment backFragment = list.get(list.size() - 1);

            fragmentTransaction.hide(backFragment);
            fragmentTransaction.add(fragmentViewId(), nextFragment, nextFragment.getHashTag());
            commitFragmentTransaction(fragmentTransaction);

            nextFragment.setOnCreatedViewListener(new BaseManagerFragment.OnCreatedViewListener() {
                @Override
                public void onCreatedView(View view) {
                    backFragment.onHide(OnHideMode.ON_START_NEW);
                    nextFragment.onShow(OnShowMode.ON_CREATE);
                    nextFragment.setOnCreatedViewListener(null);
                }
            });
        } else {
            fragmentTransaction.add(fragmentViewId(), nextFragment, nextFragment.getHashTag());
            commitFragmentTransaction(fragmentTransaction);
            fragmentOnCreateShow(nextFragment);
        }
        list.add(nextFragment);
    }

    private void startAnimation(@AnimRes int animRes, View view, final Runnable doOnOver) {
        if(view == null) {
            doOnOver.run();
            return;
        }

        if(view.getBackground() == null)
            view.setBackgroundColor(Color.WHITE);

        Animation animation = AnimationUtils.loadAnimation(this, animRes);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doOnOver.run();
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
