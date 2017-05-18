package com.roughike.bottombar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by iiro on 8.8.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BadgeTest {
    private BottomBar bottomBar;
    private BottomBarTab nearby;
    private BottomBarTab friends;

    @Before
    public void setUp() {
        bottomBar = new BottomBar(InstrumentationRegistry.getContext());
        bottomBar.setItems(com.roughike.bottombar.test.R.xml.dummy_tabs_three);
        nearby = bottomBar.getTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        friends = bottomBar.getTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
        nearby.setBadgeCount(5);
        friends.setBadgeSymbol("N");
    }

    @Test
    public void hasNoBadges_OnlyFavorites() {
        assertNull(bottomBar.getTabWithId(com.roughike.bottombar.test.R.id.tab_favorites).badge);

        assertNotNull(friends.badge);
        assertNotNull(nearby.badge);
    }

    @Test
    @UiThreadTest
    public void whenTabWithBadgeClicked_BadgeIsHidden() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
                assertFalse(nearby.badge.isVisible());

                bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
                assertFalse(friends.badge.isVisible());
            }
        });
    }

    @Test
    @UiThreadTest
    public void whenBadgeCountIsZero_BadgeIsRemoved() {
        nearby.setBadgeCount(0);
        assertNull(nearby.badge);
    }

    @Test
    @UiThreadTest
    public void whenBadgeCountIsNegative_BadgeIsRemoved() {
        nearby.setBadgeCount(-1);
        assertNull(nearby.badge);
    }

    @Test
    @UiThreadTest
    public void whenBadgeSymbolIsEmpty_BadgeIsRemoved() {
        friends.setBadgeSymbol("");
        assertNull(friends.badge);
    }

    @Test
    @UiThreadTest
    public void whenBadgeStateRestored_CountPersists() {
        nearby.setBadgeCount(1);
        assertEquals(1, nearby.badge.getCount());

        int tabNearbyIndex = nearby.getIndexInTabContainer();
        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putInt(BottomBarTab.STATE_BADGE_COUNT + tabNearbyIndex, 2);
        nearby.restoreState(savedInstanceState);

        assertEquals(2, nearby.badge.getCount());


        friends.setBadgeSymbol("N");
        assertEquals("N", friends.badge.getSymbol());

        int tabFriendsIndex = friends.getIndexInTabContainer();
        savedInstanceState = new Bundle();
        savedInstanceState.putString(BottomBarTab.STATE_BADGE_SYMBOL + tabFriendsIndex, "U");
        friends.restoreState(savedInstanceState);

        assertEquals("U", friends.badge.getSymbol());
    }

    @Test
    @UiThreadTest
    public void badgeRemovedProperly() {
        assertNotEquals(bottomBar.findViewById(R.id.bb_bottom_bar_item_container), nearby.getOuterView());
        assertEquals(2, nearby.getOuterView().getChildCount());
        assertTrue(nearby.getOuterView().getChildAt(1) instanceof BottomBarBadge);

        nearby.removeBadge();
        assertNull(nearby.badge);
        assertEquals(bottomBar.findViewById(R.id.bb_bottom_bar_item_container), nearby.getOuterView());

        assertNotEquals(bottomBar.findViewById(R.id.bb_bottom_bar_item_container), friends.getOuterView());
        assertEquals(2, friends.getOuterView().getChildCount());
        assertTrue(friends.getOuterView().getChildAt(1) instanceof BottomBarBadge);

        friends.removeBadge();
        assertNull(friends.badge);
        assertEquals(bottomBar.findViewById(R.id.bb_bottom_bar_item_container), friends.getOuterView());
    }
}
