package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by yume on 15/9/24.
 */
public abstract class BaseManagerFragment extends Fragment {
    private static final Map<String, Object> savedInstanceStateMap = new HashMap<>();

    private static final Random random = new Random();

    private static final String INTENT_KEY_REQUEST_CODE = "requestCode";

    private static final String SAVE_STORE_HASH_CODE = "manager_hash_code";
    private static final String SAVE_STORE_REQUEST_CODE = "manager_request_code";
    private static final String SAVE_STORE_RESULT_CODE = "manager_result_code";
    private static final String SAVE_STORE_RESULT_DATA = "manager_result_data";
    private static final String SAVE_STORE_FROM_INTENT = "manager_from_intent";

    public static final String INTENT_KEY_STACK_TAG = "stackTag";

    private OnCreatedViewListener onCreatedViewListener;

    private String stackTag;
    private String hashTag;

    private Intent fromIntent;

    private int requestCode = -1;
    private int resultCode = -1;
    private Bundle resultData = null;

    private Subject<Tuple3<Integer, Integer, Bundle>, Tuple3<Integer, Integer, Bundle>> onResultSubject;

    public BaseManagerFragment() {
        super();
        stackTag = setDefaultStackTag();
        hashTag = String.valueOf(hashCode());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            hashTag = savedInstanceState.getString(SAVE_STORE_HASH_CODE, hashTag);
            requestCode = savedInstanceState.getInt(SAVE_STORE_REQUEST_CODE, requestCode);
            resultCode = savedInstanceState.getInt(SAVE_STORE_RESULT_CODE, resultCode);
            resultData = savedInstanceState.getBundle(SAVE_STORE_RESULT_DATA);
            fromIntent = savedInstanceState.getParcelable(SAVE_STORE_FROM_INTENT);

            Object subject = savedInstanceStateMap.remove(hashTag);
            if(subject != null) {
                onResultSubject = (Subject<Tuple3<Integer, Integer, Bundle>, Tuple3<Integer, Integer, Bundle>>) subject;
            }
        }

        if(onResultSubject == null)
            onResultSubject = new SerializedSubject<>(PublishSubject.<Tuple3<Integer, Integer, Bundle>>create());
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

    int getRequestCode() {
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

    public void setOnCreatedViewListener(OnCreatedViewListener onCreatedViewListener) {
        this.onCreatedViewListener = onCreatedViewListener;
    }

    public BaseFragmentManagerActivity getManagerActivity() {
        return (BaseFragmentManagerActivity) getActivity();
    }

    public Observable<Tuple2<Integer, Bundle>> startActivityForObservable(Intent intent) {
        return getManagerActivity().startActivityForObservable(intent);
    }

    //Override to handle event when {@link #startFragmentForResult(Intent, int)}
    public void onFragmentResult(int requestCode, int resultCode, Bundle data){
        onResultSubject.onNext(Tuple3.of(requestCode, resultCode, data));
    }

    public void startFragmentOnNewActivity(Intent intent, Class<? extends SingleBaseActivity> activityClazz){
        ((BaseFragmentManagerActivity)getActivity()).startFragmentOnNewActivity(
                intent,
                activityClazz);
    }

    public void startFragmentOnNewActivityForResult(Intent intent, Class<? extends SingleBaseActivity> activityClazz, int requestCode){
        ((BaseFragmentManagerActivity)getActivity()).startFragmentOnNewActivityForResult(
                intent,
                activityClazz,
                requestCode);
    }

    private void startFragmentOnNewActivityForResult(Intent intent,
                                                     Class<? extends SingleBaseActivity> activityClazz,
                                                     int requestCode,
                                                     boolean checkThrottle){
        if(checkThrottle && !ThrottleUtil.checkEvent())
            return;

        ((BaseFragmentManagerActivity)getActivity()).startFragmentOnNewActivityForResult(
                intent,
                activityClazz,
                requestCode,
                false);
    }

    public void startFragment(Intent intent){
        this.startFragment(intent, false);
    }

    public void startFragment(Intent intent, boolean clearCurrentStack){
        if(!ThrottleUtil.checkEvent())
            return;

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
        this.startFragmentForResult(intent, requestCode, false, true);
    }

    private void startFragmentForResult(Intent intent,
                                        int requestCode,
                                        boolean clearCurrentStack,
                                        boolean checkThrottle){
        if(checkThrottle && !ThrottleUtil.checkEvent())
            return;

        checkThread();

        BaseManagerFragment fragment = getFragmentByIntent(intent);
        if(fragment == null)
            return;

        intent.putExtra(INTENT_KEY_REQUEST_CODE, requestCode);
        fragment.setIntent(intent);
        ((BaseFragmentManagerActivity)getActivity()).addToStack(fragment, clearCurrentStack);
    }

    public Observable<Tuple2<Integer, Bundle>> startFragmentForObservable(final Intent intent) {
        if(!ThrottleUtil.checkEvent())
            return Observable.error(new ThrottleException());

        return Observable.create(new Observable.OnSubscribe<Tuple2<Integer, Bundle>>() {
            @Override
            public void call(final Subscriber<? super Tuple2<Integer, Bundle>> sub) {
                final int requestCode = random.nextInt();
                startFragmentForResult(intent, requestCode, false, false);
                onResultSubject.subscribe(
                        new Action1<Tuple3<Integer, Integer, Bundle>>() {
                            @Override
                            public void call(Tuple3<Integer, Integer, Bundle> tuple) {
                                if (requestCode == tuple.getData1())
                                    sub.onNext(Tuple2.of(tuple.getData2(), tuple.getData3()));
                                sub.onCompleted();
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                sub.onError(throwable);
                            }
                        });
            }
        });
    }

    public Observable<Tuple2<Integer, Bundle>> startFragmentOnNewActivityForObservable(Intent intent,
                                                                                       Class<? extends SingleBaseActivity> activityClazz){
        if(!ThrottleUtil.checkEvent())
            return Observable.error(new ThrottleException());

        return Observable.create(new Observable.OnSubscribe<Tuple2<Integer, Bundle>>() {
            @Override
            public void call(final Subscriber<? super Tuple2<Integer, Bundle>> sub) {
                final int requestCode = random.nextInt() & 0x0000ffff;
                startFragmentOnNewActivityForResult(intent, activityClazz, requestCode, false);
                onResultSubject.subscribe(
                        new Action1<Tuple3<Integer, Integer, Bundle>>() {
                            @Override
                            public void call(Tuple3<Integer, Integer, Bundle> tuple) {
                                if (requestCode == tuple.getData1())
                                    sub.onNext(Tuple2.of(tuple.getData2(), tuple.getData3()));
                                sub.onCompleted();
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                sub.onError(throwable);
                            }
                        });
            }
        });
    }

    @CallSuper
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(onCreatedViewListener != null)
            onCreatedViewListener.onCreatedView(view);
    }

    protected void onHide(){ }

    /**
     * start new fragment -> OnShowMode: onCreate
     * switch to a tag -> no fragment -> create new fragment -> OnShowMode: onCreate
     * switch to a tag -> has fragment -> OnShowMode: onSwitch
     * start new fragment -> back -> OnShowMode: onBack
     * start new fragment at new activity -> OnShowMode: onCreate and onResume
     * back from activity -> OnShowMode: onResume
     *
     * @param callMode: onCreate, onResume, onBack and onSwitch
     */
    protected void onShow(@OnShowMode int callMode){ }

    protected void preBackResultData(){ }

    protected boolean onBackPressed() {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
//        onHide();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        if(hidden) {
//            onHide();
//        } else{
//            onShow();
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_STORE_HASH_CODE, hashTag);
        outState.putInt(SAVE_STORE_REQUEST_CODE, requestCode);
        outState.putInt(SAVE_STORE_RESULT_CODE, resultCode);
        outState.putBundle(SAVE_STORE_RESULT_DATA, resultData);
        outState.putParcelable(SAVE_STORE_FROM_INTENT, fromIntent);
        savedInstanceStateMap.put(hashTag, onResultSubject);
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
            throw new RuntimeException("Must run on main thread!");
        }
    }

    interface OnCreatedViewListener {
        void onCreatedView(View view);
    }
}
