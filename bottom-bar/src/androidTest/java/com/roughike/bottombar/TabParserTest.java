package com.roughike.bottombar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TabParserTest {
    private Context context;
    private List<BottomBarTab> tabs;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getContext();
        tabs = new TabParser(
                context,
                new BottomBarTab.Config.Builder().build(),
                com.roughike.bottombar.test.R.xml.dummy_tabs_five
        ).parseTabs();
    }

    @Test
    public void correctAmountOfTabs() {
        assertEquals(5, tabs.size());
    }

    @Test
    public void idsNotEmpty() {
        assertNotSame(0, tabs.get(0).getId());
        assertNotSame(0, tabs.get(1).getId());
        assertNotSame(0, tabs.get(2).getId());
        assertNotSame(0, tabs.get(3).getId());
        assertNotSame(0, tabs.get(4).getId());
    }

    @Test
    public void correctTabTitles() {
        assertEquals("Recents", tabs.get(0).getTitle());
        assertEquals("Favorites", tabs.get(1).getTitle());
        assertEquals("Nearby", tabs.get(2).getTitle());
        assertEquals("Friends", tabs.get(3).getTitle());
        assertEquals("Food", tabs.get(4).getTitle());
    }

    @Test
    public void correctInActiveColors() {
        assertEquals(Color.parseColor("#00FF00"), tabs.get(0).getInActiveColor());
        assertEquals(Color.parseColor("#0000FF"), tabs.get(1).getInActiveColor());
        assertEquals(Color.parseColor("#FF0000"), tabs.get(2).getInActiveColor());
        assertEquals(Color.parseColor("#F0F000"), tabs.get(3).getInActiveColor());
        assertEquals(Color.parseColor("#F00F00"), tabs.get(4).getInActiveColor());
    }

    @Test
    public void correctActiveColors() {
        assertEquals(Color.parseColor("#FF0000"), tabs.get(0).getActiveColor());

        assertEquals(
                ContextCompat.getColor(context, com.roughike.bottombar.test.R.color.test_random_color),
                tabs.get(1).getActiveColor()
        );

        assertEquals(Color.parseColor("#0000FF"), tabs.get(2).getActiveColor());
        assertEquals(Color.parseColor("#DAD666"), tabs.get(3).getActiveColor());
        assertEquals(Color.parseColor("#F00F00"), tabs.get(4).getActiveColor());
    }

    @Test
    public void iconResourcesExist() {
        assertNotNull(getDrawableByResource(tabs.get(0).getIconResId()));
        assertNotNull(getDrawableByResource(tabs.get(1).getIconResId()));
        assertNotNull(getDrawableByResource(tabs.get(2).getIconResId()));
        assertNotNull(getDrawableByResource(tabs.get(3).getIconResId()));
        assertNotNull(getDrawableByResource(tabs.get(4).getIconResId()));
    }

    @Test
    public void iconResourceIdsAsExpected() {
        int expectedId = com.roughike.bottombar.test.R.drawable.empty_icon;

        assertEquals(expectedId, tabs.get(0).getIconResId());
        assertEquals(expectedId, tabs.get(1).getIconResId());
        assertEquals(expectedId, tabs.get(2).getIconResId());
        assertEquals(expectedId, tabs.get(3).getIconResId());
        assertEquals(expectedId, tabs.get(4).getIconResId());
    }

    @Test
    public void barColorWhenSelectedAsExpected() {
        assertEquals(Color.parseColor("#FF0000"), tabs.get(0).getBarColorWhenSelected());
        assertEquals(Color.parseColor("#00FF00"), tabs.get(1).getBarColorWhenSelected());
        assertEquals(Color.parseColor("#F00000"), tabs.get(2).getBarColorWhenSelected());
        assertEquals(Color.parseColor("#00F000"), tabs.get(3).getBarColorWhenSelected());
        assertEquals(Color.parseColor("#00F0F0"), tabs.get(4).getBarColorWhenSelected());
    }

    @Test
    public void badgeBackgroundColorAsExpected() {
        assertEquals(Color.parseColor("#FF0000"), tabs.get(0).getBadgeBackgroundColor());
        assertEquals(Color.parseColor("#00FF00"), tabs.get(1).getBadgeBackgroundColor());
        assertEquals(Color.parseColor("#F00000"), tabs.get(2).getBadgeBackgroundColor());
        assertEquals(Color.parseColor("#00F000"), tabs.get(3).getBadgeBackgroundColor());
        assertEquals(Color.parseColor("#00F0F0"), tabs.get(4).getBadgeBackgroundColor());
    }

    @Test
    public void correctBadgeHidingPolicies() {
        assertFalse(tabs.get(0).getBadgeHidesWhenActive());
        assertTrue(tabs.get(1).getBadgeHidesWhenActive());
        assertFalse(tabs.get(2).getBadgeHidesWhenActive());
        assertTrue(tabs.get(3).getBadgeHidesWhenActive());
        assertTrue(tabs.get(4).getBadgeHidesWhenActive());
    }

    @Test
    public void titlelessTabsAsExpected() {
        assertFalse(tabs.get(0).isTitleless());
        assertFalse(tabs.get(1).isTitleless());
        assertTrue(tabs.get(2).isTitleless());
        assertFalse(tabs.get(3).isTitleless());
        assertTrue(tabs.get(4).isTitleless());
    }

    private Drawable getDrawableByResource(int iconResId) {
        return ContextCompat.getDrawable(context, iconResId);
    }
}