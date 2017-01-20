package com.fenrir.app.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import indi.yume.tools.fragmentmanager.BaseManagerFragment;
import indi.yume.tools.fragmentmanager.OnHideMode;
import indi.yume.tools.fragmentmanager.OnShowMode;
import indi.yume.tools.fragmentmanager.StartBuilder;
import indi.yume.tools.fragmentmanager.Tuple2;
import rx.functions.Action1;

/**
 * Created by yume on 16-4-21.
 */
public class BlankFragment21 extends BaseManagerFragment {


    public BlankFragment21() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println(this.getClass().getSimpleName() + ": onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank_fragment21, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println(this.getClass().getSimpleName() + ": onViewCreated");

        final Context context = getContext();
        view.findViewById(R.id.jump_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startFragment(new Intent(getContext(), BlankFragment22.class));
//                startFragmentOnNewActivityForObservable(
//                        new Intent(getContext(), BlankFragment22.class),
//                        SingleTagActivity.class)
//                        .subscribe(new Action1<Tuple2<Integer, Bundle>>() {
//                                       @Override
//                                       public void call(Tuple2<Integer, Bundle> event) {
//                                           Toast.makeText(context, event.getData2().getString("result"), Toast.LENGTH_LONG).show();
//                                       }
//                                   },
//                                new Action1<Throwable>() {
//                                    @Override
//                                    public void call(Throwable throwable) {
//                                        throwable.printStackTrace();
//                                    }
//                                });
                StartBuilder.builder(new Intent(getContext(), BlankFragment22.class))
                        .withEnableAnimation(true)
                        .withNewActivity(SingleTagActivity.class)
                        .start(BlankFragment21.this);
            }
        });
        view.findViewById(R.id.jump_activity_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForObservable(
                                new Intent(getContext(), ForObserableActivity.class))
                                .subscribe(new Action1<Tuple2<Integer, Bundle>>() {
                                               @Override
                                               public void call(Tuple2<Integer, Bundle> event) {
                                                   Toast.makeText(context, event.getData2().getString("result"), Toast.LENGTH_LONG).show();
                                               }
                                           },
                                        new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                throwable.printStackTrace();
                                            }
                                        });
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
