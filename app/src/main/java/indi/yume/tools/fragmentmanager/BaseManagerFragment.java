package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yume on 15/9/24.
 */
public abstract class BaseManagerFragment extends Fragment {
    private static final String INTENT_KEY_REQUEST_CODE = "requestCode";
    private static final String SAVE_STORE_HASH_CODE = "hash_code";

    public static final String INTENT_KEY_STACK_TAG = "stackTag";

    private String stackTag;
    private String hashTag;

    private Intent fromIntent;

    private int requestCode = -1;
    private int resultCode = -1;
    private Bundle resultData = null;
    private List<OnFragmentResultListener> resultListenerList = new ArrayList<>();

    public BaseManagerFragment() {
        super();
        stackTag = setDefaultStackTag();
        hashTag = String.valueOf(hashCode());
    }

    protected String setDefaultStackTag(){
        return null;
    }

    public String getStackTag(){
        return stackTag;
    }

    public String getHashTag(){
        return hashTag;
    }

    void setHashTag(String hashTag){
        if(!TextUtils.isEmpty(hashTag))
            this.hashTag = hashTag;
    }

    void setIntent(Intent intent){
        fromIntent = intent;
        requestCode = intent.getIntExtra(INTENT_KEY_REQUEST_CODE, -1);
        stackTag = intent.getStringExtra(INTENT_KEY_STACK_TAG);
//        if(stackTag == null)
//            stackTag = setDefaultStackTag();
    }

    public void setResult(int resultCode, Bundle resultDate){
        this.resultCode = resultCode;
        this.resultData = resultDate;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Bundle getResultData() {
        return resultData;
    }

    public Intent getIntent(){
        return fromIntent;
    }

    @CallSuper
    //Override to handle event when {@link #startFragmentForResult(Intent, int)}
    public void onFragmentResult(int requestCode, int resultCode, Bundle data){
        for(int i = 0; i < resultListenerList.size(); i++)
            resultListenerList.get(i).onFragmentResult(requestCode, resultCode, data);
    }

    public void registerFragmentResultListener(OnFragmentResultListener listener) {
        if(listener != null)
            resultListenerList.add(listener);
    }

    public boolean unregisterFragmentResultListener(OnFragmentResultListener listener) {
        return listener != null && resultListenerList.remove(listener);
    }

    public void unregisterAllFragmentResultListener() {
        resultListenerList.clear();
    }

    protected void startFragmentOnNewActivity(Intent intent, Class<? extends SingleBaseActivity> activityClazz){
        ((BaseFragmentManagerActivity)getActivity()).startFragmentOnNewActivity(
                intent,
                activityClazz);
    }

    protected void startFragmentOnNewActivityForResult(Intent intent, Class<? extends SingleBaseActivity> activityClazz, int resultCode){
        ((BaseFragmentManagerActivity)getActivity()).startFragmentOnNewActivityForResult(
                intent,
                activityClazz,
                resultCode);
    }

    public void startFragment(Intent intent){
        this.startFragment(intent, false);
    }

    public void startFragment(Intent intent, boolean clearCurrentStack){
        checkThread();

        BaseManagerFragment fragment = getFragmentByIntent(intent);
        if(fragment == null)
            return;

        fragment.setIntent(intent);
        ((BaseFragmentManagerActivity)getActivity()).addToStack(fragment, clearCurrentStack);
    }

    public void startFragmentForResult(Intent intent, int requestCode){
        this.startFragmentForResult(intent, requestCode, false);
    }

    public void startFragmentForResult(Intent intent, int requestCode, boolean clearCurrentStack){
        checkThread();

        BaseManagerFragment fragment = getFragmentByIntent(intent);
        if(fragment == null)
            return;

        intent.putExtra(INTENT_KEY_REQUEST_CODE, requestCode);
        fragment.setIntent(intent);
        ((BaseFragmentManagerActivity)getActivity()).addToStack(fragment, clearCurrentStack);
    }

    protected void onHide(){ }

    protected void onShow(){ }

    protected void preBackResultData(){ }

    @Override
    public void onPause() {
        super.onPause();
        onHide();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            onHide();
        } else{
            onShow();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_STORE_HASH_CODE, hashTag);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
            hashTag = savedInstanceState.getString(SAVE_STORE_HASH_CODE, hashTag);
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
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return fragment;
    }

    public void finish(){
        checkThread();
        ((BaseFragmentManagerActivity)getActivity()).removeFragment(this);
    }

    private void checkThread(){
        if(Looper.myLooper() != Looper.getMainLooper()) {
            try {
                throw new Throwable("Must run on main thread!");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            throw new Error("Must run on main thread!");
        }
    }

    public static interface OnFragmentResultListener {
        void onFragmentResult(int requestCode, int resultCode, Bundle data);
    }
}
