package com.example.bottombar.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by mikemilla on 7.17.2016.
 * http://mikemilla.com
 */
public class CustomColorActivity extends AppCompatActivity {

    private BottomBar mBottomBar;
    private TextView mMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        mMessageView = (TextView) findViewById(R.id.messageView);

        // Customize the colors here
        mBottomBar = BottomBar.attach(this, savedInstanceState,
                Color.parseColor("#FFFFFF"), // Background Color
                ContextCompat.getColor(this, R.color.colorAccent), // Tab Item Color
                0.25f); // Tab Item Alpha

        mBottomBar.setItems(R.menu.bottombar_menu);

        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public boolean onAttemptSelectMenuTab(@IdRes int menuItemId) {
                return true;
            }

            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                mMessageView.setText(TabMessage.get(menuItemId, false));
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                Toast.makeText(getApplicationContext(), TabMessage.get(menuItemId, true), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
