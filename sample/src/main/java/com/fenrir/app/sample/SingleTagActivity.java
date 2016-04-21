package com.fenrir.app.sample;

import android.os.Bundle;

import indi.yume.tools.fragmentmanager.SingleBaseActivity;

/**
 * Created by yume on 16-4-21.
 */
public class SingleTagActivity extends SingleBaseActivity {

    @Override
    public int fragmentViewId() {
        return R.id.fragment_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tag);
    }
}
