package indi.yume.tools.fragmentmanager.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import indi.yume.tools.fragmentmanager.ActionUtilKt;
import indi.yume.tools.fragmentmanager.BaseFragmentManagerActivity;
import indi.yume.tools.fragmentmanager.BaseManagerFragment;
import indi.yume.tools.fragmentmanager.event.CallbackAction;
import indi.yume.tools.fragmentmanager.model.ManagerState;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends BaseFragmentManagerActivity {
    private final static String SHARED_FILE_NAME = "test_sate_file";
    private final static String SAVE_STATE_KEY = "saved_sate_key";
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tag1_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToStackByTag("tag1");
            }
        });
        findViewById(R.id.tag2_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToStackByTag("tag2");
            }
        });

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getApplicationContext();
                getState(new Consumer<ManagerState>() {
                    @Override
                    public void accept(@NonNull ManagerState managerState) throws Exception {
                        String state = gson.toJson(managerState);

                        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_FILE_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(SAVE_STATE_KEY, state);
                        editor.apply();
                        System.out.println("save state: " + state);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "State is saved", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
        findViewById(R.id.restore_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, Context.MODE_PRIVATE);
                String stateJson = sharedPreferences.getString(SAVE_STATE_KEY, null);
                ManagerState savedState = gson.fromJson(stateJson, ManagerState.class);
                System.out.println("get state: " + stateJson);

                ActionUtilKt.restore(getStackManager(), savedState);
            }
        });
        findViewById(R.id.print_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getState(new Consumer<ManagerState>() {
                    @Override
                    public void accept(@NonNull ManagerState managerState) throws Exception {
                        String state = gson.toJson(managerState);
                        System.out.println("save state: \n" + state);
                    }
                });
            }
        });

        switchToStackByTag("tag1");
    }

    @Override
    public Map<String, Class<? extends BaseManagerFragment>> baseFragmentWithTag() {
        Map<String, Class<? extends BaseManagerFragment>> map = new HashMap<>();
        map.put("tag1", BlankFragment11.class);
        map.put("tag2", BlankFragment21.class);
        return map;
    }

    @Override
    protected int provideFragmentId() {
        return R.id.fragment_layout;
    }
}
