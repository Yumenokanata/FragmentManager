package com.fenrir.app.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ForObserableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_obserable);

        Intent intent = getIntent();
        intent.putExtra("result", "Observable Activity result OK");
        setResult(1, intent);
    }
}
