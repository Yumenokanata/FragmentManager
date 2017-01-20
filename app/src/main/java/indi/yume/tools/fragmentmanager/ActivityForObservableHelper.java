package indi.yume.tools.fragmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import lombok.Data;
import lombok.experimental.UtilityClass;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by yume on 16-8-8.
 */
@UtilityClass
public class ActivityForObservableHelper {
    private static final String SAVE_STATE_KEY_TAG = "save_state";
    private static final Random random = new Random();
    private static final Map<String, ResultData> savedInstanceStateMap = new HashMap<>();

    public static String onCreate(Bundle savedInstanceState, Object activity) {
        String tag = savedInstanceState != null
                ? savedInstanceState.getString(SAVE_STATE_KEY_TAG, String.valueOf(activity.hashCode()))
                : String.valueOf(activity.hashCode());

        ResultData data = savedInstanceStateMap.containsKey(tag) ? savedInstanceStateMap.get(tag) : new ResultData();
        data.setHasSaveSate(false);
        savedInstanceStateMap.put(tag, data);

        return tag;
    }

    public static Bundle onSaveInstanceState(String tag, Bundle outState) {
        outState.putString(SAVE_STATE_KEY_TAG, tag);

        if(savedInstanceStateMap.containsKey(tag)) {
            savedInstanceStateMap.get(tag).setHasSaveSate(true);
        }

        return outState;
    }

    public static void onDestroy(String tag) {
        if(savedInstanceStateMap.containsKey(tag))
            if(!savedInstanceStateMap.get(tag).isHasSaveSate())
                savedInstanceStateMap.remove(tag);
            else
                savedInstanceStateMap.get(tag).setHasSaveSate(false);
    }

    public static Observable<Tuple2<Integer, Bundle>> startActivityForObservable(String tag, Activity activity, Intent intent) {
        final ResultData resultData = savedInstanceStateMap.get(tag);
        if(resultData == null)
            return Observable.error(new RuntimeException("Do not have this Activity state: tag=" + tag));

        return Observable.create(new Observable.OnSubscribe<Tuple2<Integer, Bundle>>() {
            @Override
            public void call(final Subscriber<? super Tuple2<Integer, Bundle>> sub) {
                final int requestCode = random.nextInt() & 0x0000ffff;
                activity.startActivityForResult(intent, requestCode);
                resultData.getOnResultSubject().subscribe(
                        tuple -> {
                            if (requestCode == tuple.getData1())
                                sub.onNext(Tuple2.of(tuple.getData2(), tuple.getData3()));
                            sub.onCompleted();
                        },
                        sub::onError);
            }
        });
    }

    public static void onActivityResult(String tag, int requestCode, int resultCode, Bundle data){
        final ResultData resultData = savedInstanceStateMap.get(tag);
        if(resultData != null)
            resultData.getOnResultSubject().onNext(Tuple3.of(requestCode, resultCode, data));
    }
}

@Data
class ResultData{
    private final Subject<Tuple3<Integer, Integer, Bundle>, Tuple3<Integer, Integer, Bundle>> onResultSubject;
    private boolean hasSaveSate = false;

    ResultData() {
        onResultSubject = new SerializedSubject<>(PublishSubject.<Tuple3<Integer, Integer, Bundle>>create());
    }

    ResultData(Subject<Tuple3<Integer, Integer, Bundle>, Tuple3<Integer, Integer, Bundle>> onResultSubject) {
        this.onResultSubject = onResultSubject;
    }
}
