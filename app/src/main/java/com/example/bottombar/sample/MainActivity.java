package com.example.bottombar.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarFragment;

public class MainActivity extends AppCompatActivity {
    private BottomBar mBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setFragmentItems(getSupportFragmentManager(), R.id.fragmentContainer,
                new BottomBarFragment(SampleFragment. class,
                        R.drawable.ic_recents, "Recents").setArgs(SampleFragment.args("Content for recents.")),
                new BottomBarFragment(SampleFragment. class
                        ,R.drawable.ic_favorites, "Favorites").setArgs(SampleFragment.args("Content for favorites.")),
                new BottomBarFragment(SampleFragment. class
                        , R.drawable.ic_nearby, "Nearby").setArgs(SampleFragment.args("Content for stuff.")),
                new BottomBarFragment(SampleFragment. class
                        , R.drawable.ic_friends, "Friends").setArgs(SampleFragment.args("Content for friends.")),
                new BottomBarFragment(SampleFragment. class
                        , R.drawable.ic_restaurants, "Food").setArgs(SampleFragment.args("Content for food."))
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
