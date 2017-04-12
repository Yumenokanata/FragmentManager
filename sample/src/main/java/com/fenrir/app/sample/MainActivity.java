package com.fenrir.app.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import indi.yume.tools.fragmentmanager.BaseFragmentManagerActivity;
import indi.yume.tools.fragmentmanager.BaseManagerFragment;
import indi.yume.tools.fragmentmanager.ThrottleUtil;

public class MainActivity extends BaseFragmentManagerActivity {

    @Override
    public int fragmentViewId() {
        return R.id.fragment_layout;
    }

    @Override
    public Map<String, Class<? extends BaseManagerFragment>> baseFragmentWithTag() {
        Map<String, Class<? extends BaseManagerFragment>> map = new HashMap<>();
        map.put("tag1", BlankFragment11.class);
        map.put("tag2", BlankFragment21.class);
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ThrottleUtil.setThrottleTime(1000);

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
    }
}
