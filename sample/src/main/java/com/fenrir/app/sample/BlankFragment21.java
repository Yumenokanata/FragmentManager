package com.fenrir.app.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import indi.yume.tools.fragmentmanager.BaseManagerFragment;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank_fragment21, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.jump_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startFragment(new Intent(getContext(), BlankFragment22.class));
                startFragmentOnNewActivity(new Intent(getContext(), BlankFragment22.class),
                        SingleTagActivity.class);
            }
        });
    }
}
