package com.fenrir.app.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import indi.yume.tools.fragmentmanager.BaseManagerFragment;
import indi.yume.tools.fragmentmanager.StartBuilder;
import indi.yume.tools.fragmentmanager.OnHideMode;
import indi.yume.tools.fragmentmanager.OnShowMode;

/**
 * Created by yume on 16-4-21.
 */
public class BlankFragment11 extends BaseManagerFragment {


    public BlankFragment11() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println(this.getClass().getSimpleName() + ": onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank_fragment11, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        System.out.println(this.getClass().getSimpleName() + ": onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.jump_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startFragmentForObservable(new Intent(getContext(), BlankFragment12.class))
//                        .subscribe(new Action1<Tuple2<Integer, Bundle>>() {
//                                       @Override
//                                       public void call(Tuple2<Integer, Bundle> integerBundleTuple2) {
//                                           System.out.println("onResult: " + integerBundleTuple2.toString());
//                                       }
//                                   },
//                                new Action1<Throwable>() {
//                                    @Override
//                                    public void call(Throwable throwable) {
//                                        throwable.printStackTrace();
//                                    }
//                                });
                StartBuilder.builder(new Intent(getContext(), BlankFragment12.class))
                        .withEnableAnimation(true)
                        .start(BlankFragment11.this);
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
