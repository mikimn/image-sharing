package com.mikimn.instakiller;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // UI
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.feed:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new FeedFragment()).commit();
                    break;
                case R.id.my_photos:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new PrivateFragment()).commit();
                    break;
            }
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.feed);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, CameraActivity.class));
        });
    }
}
