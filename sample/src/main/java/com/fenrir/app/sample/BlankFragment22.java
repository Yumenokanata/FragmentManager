package com.fenrir.app.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import indi.yume.tools.fragmentmanager.BaseManagerFragment;
import indi.yume.tools.fragmentmanager.OnShowMode;

/**
 * Created by yume on 16-4-21.
 */
public class BlankFragment22 extends BaseManagerFragment {


    public BlankFragment22() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println(this.getClass().getSimpleName() + ": onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank_fragment22, container, false);
    }

    @Override
    protected void preBackResultData() {
        super.preBackResultData();

        Bundle data = new Bundle();
        data.putString("result", "Observable result OK");
        setResult(1, data);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println(this.getClass().getSimpleName() + ": onViewCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println(this.getClass().getSimpleName() + ": onResume");
    }

    @Override
    protected void onShow(int callMode) {
        super.onShow(callMode);
        System.out.println(this.getClass().getSimpleName() + ": onShow " + OnShowMode.Util.toString(callMode));
    }

    @Override
    protected void onHide() {
        super.onHide();
        System.out.println(this.getClass().getSimpleName() + ": onHide");
    }
}
