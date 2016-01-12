package indi.yume.tools.fragmentmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yume on 15/11/10.
 */
public abstract class SingleBaseActivity extends BaseFragmentManagerActivity {
    protected static final String SINGLE_TAG = "tag";

    private Map<String, Class<?>> map = new HashMap<>();

    @Override
    public Map<String, Class<?>> BaseFragmentWithTag() {
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String className = getIntent().getStringExtra(INTENT_KEY_FRAGMENT_CLASS);
        try {
            map.put(SINGLE_TAG, Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        switchToStackByTag(SINGLE_TAG);
    }

    private static final String INTENT_KEY_FRAGMENT_CLASS = "fragmentClass";
    public static Intent createIntent(Context context, Class<?> clazz, Class<? extends SingleBaseActivity> activityClazz){
        Intent intent = new Intent(context, activityClazz);
        intent.putExtra(INTENT_KEY_FRAGMENT_CLASS, clazz.getName());
        return intent;
    }

    public static Intent createIntent(Context context, Class<?> clazz, Class<? extends SingleBaseActivity> activityClazz, Intent intent){
        intent.setClass(context, activityClazz);
        intent.putExtra(INTENT_KEY_FRAGMENT_CLASS, clazz.getName());
        return intent;
    }
}
