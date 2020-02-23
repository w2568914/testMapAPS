package com.example.testmapaps;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.testmapaps.ui.select.SelectFragment;

public class Select_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select__activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SelectFragment.newInstance())
                    .commitNow();
        }
    }
}
