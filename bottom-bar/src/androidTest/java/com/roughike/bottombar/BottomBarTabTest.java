package com.roughike.bottombar;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by iiro on 22.8.2016.
 */
@RunWith(AndroidJUnit4.class)
public class BottomBarTabTest {
    private FrameLayout tabContainer;
    private BottomBarTab tab;

    @Before
    public void setUp() {
        tabContainer = new FrameLayout(InstrumentationRegistry.getContext());
        tab = new BottomBarTab(InstrumentationRegistry.getContext());

        tabContainer.addView(tab);
    }

    @Test
    public void correctLayoutReturned_ForFixedTab() {
        tab.setType(BottomBarTab.Type.FIXED);
        assertEquals(R.layout.bb_bottom_bar_item_fixed, tab.getLayoutResource());
    }

    @Test
    public void correctLayoutReturned_ForShiftingTab() {
        tab.setType(BottomBarTab.Type.SHIFTING);
        assertEquals(R.layout.bb_bottom_bar_item_shifting, tab.getLayoutResource());
    }

    @Test
    public void correctLayoutReturned_ForTabletTab() {
        tab.setType(BottomBarTab.Type.TABLET);
        assertEquals(R.layout.bb_bottom_bar_item_fixed_tablet, tab.getLayoutResource());
    }

    @Test
    public void testSavedStateWithBadge_StaysIntact() {
        tab.setBadgeCount(5);
        tab.setIndexInContainer(69);
        assertEquals(69, tab.getIndexInTabContainer());

        tab.setBadgeCount(9);
        assertEquals(9, tab.badge.getCount());
    }

    @Test
    public void testBadgeVisibility_BadgeNull() {
        tab.setBadgeCount(0);
        assertNull(tab.badge);
    }

    @Test
    public void testBadgeVisibility_BadgeNotNull() {
        tab.setBadgeCount(0);
        tab.setBadgeCount(5);
        assertNotNull(tab.badge);
    }
}
