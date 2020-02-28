package com.example.testmapaps.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.testmapaps.R;
import com.example.testmapaps.ui.select.SelectFragment;

public class selectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SelectFragment.newInstance())
                    .commitNow();
        }
    }
}
