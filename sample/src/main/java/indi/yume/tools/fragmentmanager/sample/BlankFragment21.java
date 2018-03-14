package indi.yume.tools.fragmentmanager.sample;

import android.content.Context;
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
public class BlankFragment21 extends DefaultManagerFragment {


    public BlankFragment21() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        System.out.println(this.getClass().getSimpleName() + ": onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank_fragment21, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println(this.getClass().getSimpleName() + ": onViewCreated");

        final Context context = getContext();
//        view.findViewById(R.id.jump_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RxStartBuilder.builder(new Intent(getContext(), BlankFragment22.class))
//                        .withEnableAnimation(true)
//                        .withNewActivity(SingleTagActivity.class)
//                        .startForObservable(BlankFragment21.this)
//                        .subscribe(new Action1<Tuple2<Integer, Bundle>>() {
//                            @Override
//                            public void call(Tuple2<Integer, Bundle> t) {
//                                Toast.makeText(context, t.getData2().getString("result"), Toast.LENGTH_LONG).show();
//                            }
//                        });
//            }
//        });
//        view.findViewById(R.id.jump_activity_button)
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        startActivityForObservable(
//                                new Intent(getContext(), ForObserableActivity.class))
//                                .subscribe(new Action1<Tuple2<Integer, Bundle>>() {
//                                               @Override
//                                               public void call(Tuple2<Integer, Bundle> event) {
//                                                   Toast.makeText(context, event.getData2().getString("result"), Toast.LENGTH_LONG).show();
//                                               }
//                                           },
//                                        new Action1<Throwable>() {
//                                            @Override
//                                            public void call(Throwable throwable) {
//                                                throwable.printStackTrace();
//                                            }
//                                        });
//                    }
//                });
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
