package indi.yume.tools.fragmentmanager.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import indi.yume.tools.fragmentmanager.DefaultManagerFragment;
import indi.yume.tools.fragmentmanager.anno.OnHideMode;
import indi.yume.tools.fragmentmanager.anno.OnShowMode;

/**
 * Created by yume on 16-4-21.
 */
public class BlankFragment12 extends DefaultManagerFragment {


    public BlankFragment12() {
        // Required empty public constructor
    }

//    @Override
//    protected int provideEnterAnim() {
//        return R.anim.fragment_bottom_enter;
//    }
//
//    @Override
//    protected int provideExitAnim() {
//        return R.anim.fragment_bottom_exit;
//    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        System.out.println(this.getClass().getSimpleName() + ": onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank_fragment12, container, false);

//        return enableSwipeBackAtFragment(this, view);
        return view;
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

                finish();
            }
        });
    }

    @Override
    public void preBackResultData() {
        setResult(2, new Bundle());
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println(this.getClass().getSimpleName() + ": onResume");
    }

    @Override
    public void onShow(@NonNull OnShowMode callMode) {
        super.onShow(callMode);
        System.out.println(this.getClass().getSimpleName() + ": onShow " + OnShowMode.toString(callMode));
    }

    @Override
    public void onHide(@NonNull OnHideMode hideMode) {
        super.onHide(hideMode);
        System.out.println(this.getClass().getSimpleName() + ": onHide " + OnHideMode.toString(hideMode));
    }

    @Override
    @NonNull
    public Fragment getFragment() {
        return this;
    }
}
