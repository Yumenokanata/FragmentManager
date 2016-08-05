package com.fenrir.app.sample;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;

import indi.yume.tools.fragmentmanager.SingleBaseActivity;
import indi.yume.tools.fragmentmanager.SwipeBackLayout;

import static indi.yume.tools.fragmentmanager.SwipeBackUtil.enableSwipeBackAtActivity;

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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        enableSwipeBackAtActivity(this);
    }
}
