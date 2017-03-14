package com.keyfinder.keyfinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.key_finder_fragment_container);

        if(fragment==null)
        {
            fragment = KeyFinderFragment.newInstance();

            fm.beginTransaction().add(R.id.key_finder_fragment_container,fragment).commit();
        }
    }
}
