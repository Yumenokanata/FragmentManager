package indi.yume.tools.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import indi.yume.tools.renderercalendar.R;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

import static indi.yume.tools.fragmentmanager.Utils.checkThread;

/**
 * Created by yume on 15/9/24.
 */
public abstract class BaseManagerFragment extends Fragment {
    private static final Map<String, Object> savedInstanceStateMap = new HashMap<>();

    private static final Random random = new Random();

    static final String INTENT_KEY_REQUEST_CODE = "request_code";
    static final String INTENT_KEY_ANIM_DATA = "anim_data";

    private static final String SAVE_STORE_HASH_CODE = "manager_hash_code";
    private static final String SAVE_STORE_REQUEST_CODE = "manager_request_code";
    private static final String SAVE_STORE_RESULT_CODE = "manager_result_code";
    private static final String SAVE_STORE_RESULT_DATA = "manager_result_data";
    private static final String SAVE_STORE_FROM_INTENT = "manager_from_intent";

    public static final String INTENT_KEY_STACK_TAG = "stackTag";

    private OnCreatedViewListener onCreatedViewListener;

    private volatile boolean isShow = false;

    @Getter(lazy = true)
    private final AnimData intentAnim = genIntentAnim();

    @AnimRes
    protected int provideEnterAnim() {
        return -1;
    }

    @AnimRes
    protected int provideExitAnim() {
        return -1;
    }

    @AnimRes
    protected int provideStayAnim() {
        return R.anim.stay_anim;
    }

    private String stackTag;
    private String hashTag;

    private Intent fromIntent;

    private int requestCode = -1;
    private int resultCode = -1;
    private Bundle resultData = null;

    private Subject<Tuple3<Integer, Integer, Bundle>> onResultSubject;

    public BaseManagerFragment() {
        super();
        stackTag = setDefaultStackTag();
        hashTag = String.valueOf(hashCode());
    }

    AnimData genIntentAnim() {
        Intent intent = getIntent();
        if(intent != null) {
            return (AnimData) intent.getSerializableExtra(INTENT_KEY_ANIM_DATA);
        }
        return null;
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
                onResultSubject = (Subject<Tuple3<Integer, Integer, Bundle>>) subject;
            }
        }

        if(onResultSubject == null)
            onResultSubject = PublishSubject.<Tuple3<Integer, Integer, Bundle>>create().toSerialized();
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

    public boolean isStackSingle() {
        return getManagerActivity().getCurrentStackSize() <= 1;
    }

    public Single<Tuple2<Integer, Bundle>> startActivityForObservable(Intent intent) {
        return getManagerActivity().startActivityForObservable(intent);
    }

    //Override to handle event when {@link #startFragmentForResult(Intent, int)}
    public void onFragmentResult(int requestCode, int resultCode, Bundle data){
        onResultSubject.onNext(Tuple3.of(requestCode, resultCode, data));
    }

    public void start(StartBuilder builder) {
        getManagerActivity().start(builder);
    }

    public void startFragmentOnNewActivity(Intent intent, Class<? extends SingleBaseActivity> activityClazz){
        start(StartBuilder.builder(intent)
                .withNewActivity(activityClazz));
    }

    public void startFragmentOnNewActivity(Intent intent,
                                           Class<? extends SingleBaseActivity> activityClazz,
                                           boolean withAnimation){
        start(StartBuilder.builder(intent)
                .withNewActivity(activityClazz)
                .withEnableAnimation(withAnimation));
    }

    public void startFragmentOnNewActivityForResult(Intent intent, Class<? extends SingleBaseActivity> activityClazz, int requestCode){
        start(StartBuilder.builder(intent)
                .withNewActivity(activityClazz)
                .withRequestCode(requestCode));
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
                .withEnableAnimation(withAnimation));
    }

    public void startFragment(Intent intent){
        this.startFragment(intent, false);
    }

    public void startFragment(Intent intent, boolean clearCurrentStack){
        this.startFragment(intent, clearCurrentStack, true);
    }

    public void startFragment(Intent intent, boolean clearCurrentStack, boolean withAnimation){
        getManagerActivity().start(StartBuilder.builder(intent)
                .withClearCurrentStack(clearCurrentStack)
                .withEnableAnimation(withAnimation));
    }

    public void startFragmentForResult(Intent intent, int requestCode){
        this.startFragmentForResult(intent, requestCode, false);
    }

    public void startFragmentForResult(Intent intent, int requestCode, boolean clearCurrentStack){
        this.startFragmentForResult(intent, requestCode, false, true, true);
    }

    private void startFragmentForResult(Intent intent,
                                        int requestCode,
                                        boolean clearCurrentStack,
                                        boolean checkThrottle){
        this.startFragmentForResult(intent, requestCode, clearCurrentStack, checkThrottle, true);
    }

    void startFragmentForResult(Intent intent,
                                int requestCode,
                                boolean clearCurrentStack,
                                boolean checkThrottle,
                                boolean withAnimation){
        startFragmentForResult(getManagerActivity(),
                intent,
                requestCode,
                clearCurrentStack,
                checkThrottle,
                withAnimation);
    }

    static void startFragmentForResult(BaseFragmentManagerActivity activity,
                                Intent intent,
                                int requestCode,
                                boolean clearCurrentStack,
                                boolean checkThrottle,
                                boolean withAnimation){
        activity.start(StartBuilder.builder(intent)
                .withRequestCode(requestCode)
                .withClearCurrentStack(clearCurrentStack)
                .withCheckThrottle(checkThrottle)
                .withEnableAnimation(withAnimation));
    }

    public Single<Tuple2<Integer, Bundle>> startForObservable(RxStartBuilder builder) {
        final Intent intent = builder.getIntent();
        boolean checkThrottle = builder.isCheckThrottle();
        boolean enableAnimation = builder.isEnableAnimation();
        Class<? extends SingleBaseActivity> newActivity = builder.getNewActivity();
        AnimData anim = enableAnimation ? builder.getAnim() : null;

        return Single.create(emitter -> {
            if(checkThrottle && !ThrottleUtil.checkEvent())
                throw new ThrottleException();

            final int requestCode1 = random.nextInt() & 0x0000ffff;
            start(StartBuilder.builder(intent)
                    .withNewActivity(newActivity)
                    .withRequestCode(requestCode1)
                    .withCheckThrottle(false)
                    .withAnimData(anim));
            onResultSubject.firstOrError()
                    .subscribe(tuple -> {
                                if (requestCode1 == tuple.getData1())
                                    emitter.onSuccess(Tuple2.of(tuple.getData2(), tuple.getData3()));
                                else
                                    emitter.onError(new RuntimeException(getClass().getSimpleName()
                                            + " has error requestCode: " + requestCode1 + " != " + tuple.getData1()));
                            },
                            emitter::onError);
        });
    }

    public Single<Tuple2<Integer, Bundle>> startFragmentForObservable(final Intent intent) {
        return startFragmentForObservable(intent, true);
    }

    public Single<Tuple2<Integer, Bundle>> startFragmentForObservable(final Intent intent,
                                                                          boolean withAnimation) {
        return startForObservable(RxStartBuilder.builder(intent).withEnableAnimation(withAnimation));
    }

    public Single<Tuple2<Integer, Bundle>> startFragmentOnNewActivityForObservable(
            Intent intent,
            Class<? extends SingleBaseActivity> activityClazz){
        return startFragmentOnNewActivityForObservable(intent, activityClazz, true);
    }

    public Single<Tuple2<Integer, Bundle>> startFragmentOnNewActivityForObservable(
            Intent intent,
            Class<? extends SingleBaseActivity> activityClazz,
            boolean withAnimation){
        return startForObservable(RxStartBuilder.builder(intent)
                .withNewActivity(activityClazz)
                .withEnableAnimation(withAnimation));
    }

    @CallSuper
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(onCreatedViewListener != null)
            onCreatedViewListener.onCreatedView(view);
    }

    public boolean isShow() {
        return isShow;
    }

    @CallSuper
    protected void onHide(@OnHideMode int hideMode){
        isShow = false;
    }

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
    @CallSuper
    protected void onShow(@OnShowMode int callMode){
        isShow = true;
    }

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

    private static BaseManagerFragment getFragmentByIntent(Intent intent){
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

    public void finishWithoutAnim(){
        checkThread();
        ((BaseFragmentManagerActivity)getActivity()).removeFragmentWithoutAnim(this);
    }

    public boolean isTopOfStack() {
        return getManagerActivity().isTopOfStack(this);
    }

    interface OnCreatedViewListener {
        void onCreatedView(View view);
    }
}
