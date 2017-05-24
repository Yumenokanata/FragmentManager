package com.fenrir.app.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import indi.yume.tools.fragmentmanager.BaseManagerFragment;
import indi.yume.tools.fragmentmanager.RxStartBuilder;
import indi.yume.tools.fragmentmanager.OnHideMode;
import indi.yume.tools.fragmentmanager.OnShowMode;
import indi.yume.tools.fragmentmanager.Tuple2;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

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
                RxStartBuilder.builder(new Intent(getContext(), BlankFragment12.class))
                        .withEnableAnimation(true)
                        .withEnterAnim(R.anim.fragment_left_enter)
                        .withExitAnim(R.anim.fragment_left_exit)
                        .startForObservable(BlankFragment11.this)
                        .subscribe(new Consumer<Tuple2<Integer,Bundle>>() {
                            @Override
                            public void accept(@NonNull Tuple2<Integer, Bundle> integerBundleTuple2) throws Exception {
                                Toast.makeText(getContext(), "Result11", Toast.LENGTH_SHORT).show();
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
        System.out.println(this.getClass().getSimpleName() + ": isTopOfStack " + isTopOfStack());
    }

    @Override
    protected void onHide(int hideMode) {
        super.onHide(hideMode);
        System.out.println(this.getClass().getSimpleName() + ": onHide " + OnHideMode.Util.toString(hideMode));
        System.out.println(this.getClass().getSimpleName() + ": isTopOfStack " + isTopOfStack());
    }
}
