package indi.yume.tools.fragmentmanager.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import indi.yume.tools.fragmentmanager.DefaultManagerFragment;
import indi.yume.tools.fragmentmanager.StartBuilder;
import indi.yume.tools.fragmentmanager.anno.OnHideMode;
import indi.yume.tools.fragmentmanager.anno.OnShowMode;

/**
 * Created by yume on 16-4-21.
 */
public class BlankFragment11 extends DefaultManagerFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
                StartBuilder.builder(new Intent(getActivity(), BlankFragment12.class))
                        .withEnableAnimation(true)
                        .withEnterAnim(R.anim.fragment_left_enter)
                        .withExitAnim(R.anim.fragment_left_exit)
                        .start(BlankFragment11.this);
//                        .startForObservable(BlankFragment11.this)
//                        .subscribe(new Action1<Tuple2<Integer, Bundle>>() {
//                            @Override
//                            public void call(Tuple2<Integer, Bundle> integerBundleTuple2) {
//                                Toast.makeText(getContext(), "Result11", Toast.LENGTH_SHORT).show();
//                            }
//                        });
            }
        });
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
//        System.out.println(this.getClass().getSimpleName() + ": isTopOfStack " + isTopOfStack());
    }

        @Override
    public void onHide(@NonNull OnHideMode hideMode) {
            super.onHide(hideMode);
            System.out.println(this.getClass().getSimpleName() + ": onHide " + OnHideMode.toString(hideMode));
//        System.out.println(this.getClass().getSimpleName() + ": isTopOfStack " + isTopOfStack());
        }
}
