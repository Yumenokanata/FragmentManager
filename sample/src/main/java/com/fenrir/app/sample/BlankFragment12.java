package com.fenrir.app.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import indi.yume.tools.fragmentmanager.BaseManagerFragment;
import indi.yume.tools.fragmentmanager.OnHideMode;
import indi.yume.tools.fragmentmanager.OnShowMode;
import indi.yume.tools.fragmentmanager.StartBuilder;
import indi.yume.tools.fragmentmanager.SwipeBackLayout;

import static indi.yume.tools.fragmentmanager.SwipeBackUtil.enableSwipeBackAtFragment;

/**
 * Created by yume on 16-4-21.
 */
public class BlankFragment12 extends BaseManagerFragment {


    public BlankFragment12() {
        // Required empty public constructor
    }

    @Override
    protected int provideEnterAnim() {
        return R.anim.fragment_bottom_enter;
    }

    @Override
    protected int provideExitAnim() {
        return R.anim.fragment_bottom_exit;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println(this.getClass().getSimpleName() + ": onCreateView");
        // Inflate the layout for this fragment
        setResult(2, new Bundle());
        View view = inflater.inflate(R.layout.fragment_blank_fragment12, container, false);

        return enableSwipeBackAtFragment(this, view);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        System.out.println(this.getClass().getSimpleName() + ": onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.finish_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finishWithoutAnim();
//                StartBuilder.builder(new Intent(getContext(), BlankFragment12.class))
//                        .withEnableAnimation(true)
//                        .withEnterAnim(R.anim.fragment_left_enter)
//                        .withExitAnim(R.anim.fragment_left_exit)
//                        .start(BlankFragment12.this);
                getManagerActivity().switchToStackByTag("tag2");
                backToFirst();
            }
        });
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
    protected void onHide(int hideMode) {
        super.onHide(hideMode);
        System.out.println(this.getClass().getSimpleName() + ": onHide " + OnHideMode.Util.toString(hideMode));
    }
}
