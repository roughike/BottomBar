package com.example.bottombar.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;

/**
 * The ViewPager example activity.
 * This illustrates how simply you can hook up bottom bar with your viewpager by calling just one method
 * Please note that you need a working viewpager adapter to achieve error free results.
 */
public class ViewPagerActivity extends AppCompatActivity {

    /**
     * The View pager.
     */
    ViewPager viewPager;
    /**
     * The Bottom bar.
     */
    BottomBar bottomBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);


        // initialize views by xml references
        initViews();

        // initiate viewpager
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        // set adapter to viewpager
        viewPager.setAdapter(viewPagerAdapter);

        // just add viewpager to bottombar
        bottomBar.setUpWithViewPager(viewPager);

        // Now viewpager and bottom bar will work together


    }


    private void initViews() {

        viewPager = (ViewPager) findViewById(R.id.viewpager_button);
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);

    }

    /**
     * The View pager adapter to manage fragments.
     */

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {


        /**
         * Instantiates a new View pager adapter.
         *
         * @param fm the SupportFragmentManager object
         */
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {


            return SampleFragment.newInstance("This is Fragment #" + (position + 1));
        }

        @Override
        public int getCount() {
            return 3;                           // working with 3 fragments for now
        }
    }

}
