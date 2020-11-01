package com.example.bottombar.sample;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;

public class IconsOnlyActivity extends AppCompatActivity {
    private static final int TABS_COUNT = 5;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons_only);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return SampleFragment.newInstance(Integer.toString(position));
            }

            @Override
            public int getCount() {
                return TABS_COUNT;
            }
        });

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setupWithViewPager(viewPager);

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                Toast.makeText(getApplicationContext(), TabMessage.get(tabId, true), Toast.LENGTH_LONG).show();
            }
        });
    }
}