package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import indi.yume.tools.renderercalendar.R;

/**
 * Created by yume on 15/9/24.
 */
public abstract class BaseFragmentManagerActivity extends FragmentActivity {
    Map<String, List<BaseManagerFragment>> fragmentMap = new HashMap<>();
    Map<String, Class<?>> baseFragmentMap;
    String currentStackTag;
    FragmentManager fragmentManager;
    OnStackChangedListener mOnStackChangedListener;

    private boolean isStartForResult = false;

    public abstract int fragmentViewId();

    public abstract Map<String, Class<?>> BaseFragmentWithTag();

    public boolean clearStackWhenStackChanged(String targetTag, String currentTag){
        return false;
    }

    public void setOnStackChangedListener(OnStackChangedListener onStackChangedListener) {
        this.mOnStackChangedListener = onStackChangedListener;
    }

    protected String getCurrentStackTag() {
        return currentStackTag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        baseFragmentMap = BaseFragmentWithTag();
        if(baseFragmentMap == null)
            throw new Error("BaseFragmentWithTag() must return value");
    }

    public void startFragmentOnNewActivity(Intent intent, Class<? extends SingleBaseActivity> activityClazz){
        try {
            startActivity(SingleBaseActivity.createIntent(this, Class.forName(intent.getComponent().getClassName()), activityClazz, intent));
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void startFragmentOnNewActivityForResult(Intent intent, Class<? extends SingleBaseActivity> activityClazz, int resultCode){
        try {
            startActivityForResult(SingleBaseActivity.createIntent(this, Class.forName(intent.getComponent().getClassName()), activityClazz, intent), resultCode);
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            isStartForResult = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(isStartForResult)
            if(!TextUtils.isEmpty(currentStackTag) && fragmentMap != null && fragmentMap.keySet().contains(currentStackTag)){
                List<BaseManagerFragment> fragmentList = fragmentMap.get(currentStackTag);
                if(fragmentList != null && fragmentList.size() != 0)
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

    public void switchToStackByTag(String tag, boolean clearCurrentStack){
        if(!baseFragmentMap.containsKey(tag))
            throw new Error("Tag: " + tag + " not in baseFragmentMap. [BaseFragmentWithTag()]");

        if((fragmentMap.containsKey(tag) && !TextUtils.equals(tag, currentStackTag))
                || (!fragmentMap.containsKey(tag) || fragmentMap.get(tag).size() == 0)){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if((fragmentMap.containsKey(currentStackTag) && fragmentMap.get(currentStackTag).size() != 0))
                if(clearCurrentStack)
                    clearStackByTag(currentStackTag, fragmentTransaction);
                else
                    hideStackByTag(currentStackTag, fragmentTransaction);

            showStackByTag(tag, fragmentTransaction);
            currentStackTag = tag;
            fragmentTransaction.commit();
        }
    }

    public void clearCurrentStack(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if((fragmentMap.containsKey(currentStackTag) && fragmentMap.get(currentStackTag).size() != 0))
            clearStackByTag(currentStackTag, fragmentTransaction);

        showStackByTag(currentStackTag, fragmentTransaction);
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
//        if(currentStackTag != null && !TextUtils.equals(currentStackTag, targetTag)) {
        if(clearCurrentStack){
            clearStackByTag(currentStackTag, fragmentTransaction);
        } else {
            hideStackByTag(currentStackTag, fragmentTransaction);
        }
//        }

        currentStackTag = targetTag;
        fragmentMap.get(targetTag).add(fragment);
//        fragmentTransaction.setCustomAnimations(
//                R.anim.fragment_left_enter,
//                R.anim.fragment_left_exit);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.add(fragmentViewId(), fragment, fragment.toString());

        showStackByTag(targetTag, fragmentTransaction);
        fragmentTransaction.commit();
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
            fragmentTransaction.hide(fragment);
    }

    private void showStackByTag(String tag, FragmentTransaction fragmentTransaction){
        if(!fragmentMap.containsKey(tag))
            fragmentMap.put(tag, new ArrayList<BaseManagerFragment>());

        List<BaseManagerFragment> list = fragmentMap.get(tag);
        if(list.size() == 0) {
            BaseManagerFragment fragment = getFragmentByClass(baseFragmentMap.get(tag));
            if (fragment == null)
                throw new Error("baseFragmentMap [BaseFragmentWithTag()] has wrong");
            fragment.setIntent(getIntent());
            fragmentTransaction.add(fragmentViewId(), fragment, fragment.toString());
            list.add(fragment);
            return;
        } else{
            for(String key : fragmentMap.keySet())
                for(BaseManagerFragment f : fragmentMap.get(key))
                    fragmentTransaction.hide(f);
            fragmentTransaction.show(list.get(list.size() - 1));
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
                    fragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .remove(fragment)
                            .commit();
                    fragmentMap.get(key).remove(f);
                    break;
                }

        List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
        if(list.size() == 0) {
            Intent intent = new Intent();
            if(fragment.getResultData() != null)
                intent.putExtras(fragment.getResultData());
            setResult(fragment.getResultCode(), intent);

            supportFinishAfterTransition();
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
        } else{
            BaseManagerFragment fragment1 = list.get(list.size() - 1);
            if(fragment.getRequestCode() != -1)
                fragment1.onFragmentResult(fragment.getRequestCode(),
                        fragment.getResultCode(),
                        fragment.getResultData());

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            showStackByTag(currentStackTag, fragmentTransaction);
            fragmentTransaction.commit();
        };
    }

    @Override
    public void onBackPressed() {
        List<BaseManagerFragment> list = fragmentMap.get(currentStackTag);
        if(list.size() <= 1) {
            if(list.size() == 1) {
                BaseManagerFragment fragment = list.get(0);
                Intent intent = new Intent();
                if(fragment.getResultData() != null)
                    intent.putExtras(fragment.getResultData());
                setResult(fragment.getRequestCode(), intent);
            }
            supportFinishAfterTransition();
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
        } else {
            BaseManagerFragment fragment = list.get(list.size() - 1);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.setCustomAnimations(
//                    R.anim.fragment_left_enter,
//                    R.anim.fragment_left_exit);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            fragmentTransaction.remove(fragment)
                    .commit();
            list.remove(fragment);

            BaseManagerFragment fragment1 = list.get(list.size() - 1);
            if(fragment.getRequestCode() != -1)
                fragment1.onFragmentResult(fragment.getRequestCode(),
                        fragment.getResultCode(),
                        fragment.getResultData());

            FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
            showStackByTag(currentStackTag, fragmentTransaction1);
            fragmentTransaction1.commit();
        }
    }

    public static interface OnStackChangedListener {
        boolean onStackChanged(String targetTag, String currentTag);
    }
}
