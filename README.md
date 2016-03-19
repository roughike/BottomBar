# BottomBar
<img src="https://raw.githubusercontent.com/roughike/BottomBar/master/demo1.gif" width="278" height="492" /> <img src="https://raw.githubusercontent.com/roughike/BottomBar/master/demo2.gif" width="278" height="492" />

## Have issues? [Common problems and solutions](https://github.com/roughike/BottomBar/blob/master/README.md#common-problems-and-solutions)

## What?

A custom view component that mimics the new [Material Design Bottom Navigation pattern](https://www.google.com/design/spec/components/bottom-navigation.html).

**(currently under active development, expect to see new releases almost daily)**

## Does it work on my Grandpa's HTC Dream?

Nope. The current minSDK version is API level 14.

Your uncle's Galaxy S Mini will probably be supported in the future though. 

## Gimme that Gradle sweetness, pls?

```groovy
compile 'com.roughike:bottom-bar:1.0.8'
```

**Maven:**
```xml
<dependency>
  <groupId>com.roughike</groupId>
  <artifactId>bottom-bar</artifactId>
  <version>1.0.8</version>
  <type>pom</type>
</dependency>
```

## How?

BottomBar likes Fragments very much, but you can also handle your tab changes by yourself. You can add items by specifying an array of items or **by xml menu resources**.

#### Adding items from menu resource

**res/menu/bottombar_menu.xml:**

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/bottomBarItemOne"
        android:icon="@drawable/ic_recents"
        android:title="Recents" />
        ...
</menu>
```

**MainActivity.java**

```java
public class MainActivity extends AppCompatActivity {
    private BottomBar mBottomBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int resId) {
                if (resId == R.id.bottomBarItemOne) {
                    // the user selected item number one
                }
            }
        });
        
        // Setting colors for different tabs when there's more than three of them.
        // You can set colors for tabs in three different ways as shown below.
        mBottomBar.mapColorForTab(0, ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.mapColorForTab(1, 0xFF5D4037);
        mBottomBar.mapColorForTab(2, "#7B1FA2");
        mBottomBar.mapColorForTab(3, "#FF5252");
        mBottomBar.mapColorForTab(4, "#FF9800");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
```

#### Can it handle my Fragments and replace them automagically when a different tab is selected?

Yep yep yep! Just call ```setFragmentItems()``` instead of ```setItemsFromMenu()```:

```java
mBottomBar.setFragmentItems(getSupportFragmentManager(), R.id.fragmentContainer,
    new BottomBarFragment(SampleFragment.newInstance("Content for recents."), R.drawable.ic_recents, "Recents"),
    new BottomBarFragment(SampleFragment.newInstance("Content for favorites."), R.drawable.ic_favorites, "Favorites"),
    new BottomBarFragment(SampleFragment.newInstance("Content for nearby stuff."), R.drawable.ic_nearby, "Nearby")
);
```

#### I hate Fragments and wanna do everything by myself!

That's alright, you can also do it the hard way if you're living on the edge.

```java
mBottomBar.setItems(
        new BottomBarTab(R.drawable.ic_recents, "Recents"),
        new BottomBarTab(R.drawable.ic_favorites, "Favorites"),
        new BottomBarTab(R.drawable.ic_nearby, "Nearby")
);

// Listen for tab changes
mBottomBar.setOnItemSelectedListener(new OnTabSelectedListener() {
    @Override
    public void onItemSelected(int position) {
        // user selected a different tab
    }
});
```

For a working example, refer to [the sample app](https://github.com/roughike/BottomBar/tree/master/app/src/main).

## Common problems and solutions

#### Can I use it by XML?

No, but you can still put it anywhere in the View hierarchy. Just attach it to any View you want like this:

```java
mBottomBar.attach(findViewById(R.id.myContent), savedInstanceState);
```

#### Why does the top of my content have sooooo much empty space?!

Probably because you're doing some next-level advanced Android stuff (such as using CoordinatorLayout and ```fitsSystemWindows="true"```) and the normal paddings for the content are too much. Add this right after calling ```attach()```:

```java
mBottomBar.noTopOffset();
```

#### I don't like the awesome transparent Navigation Bar / it behaves poorly / breaks my layout!

You can disable it. I'm squashing bugs as fast as I can, but they are hard to find.

```java
mBottomBar.noNavBarGoodness();
```

#### Why is it overlapping my Navigation Drawer?

All you need to do is instead of attaching the BottomBar to your Activity, attach it to the view that has your content. For example, if your fragments are in a ViewGroup that has the id ```fragmentContainer```, you would do something like this:

```java
mBottomBar.attach(findViewById(R.id.fragmentContainer), savedInstanceState);
```

#### What about Tablets?

It works nicely with tablets straight out of the box. When the library detects that the user has a tablet, the BottomBar will become a "LeftBar", just like [in the Material Design Guidelines](https://material-design.storage.googleapis.com/publish/material_v_4/material_ext_publish/0B3321sZLoP_HSTd3UFY2aEp2ZDg/components_bottomnavigation_usage2.png).


#### What about the (insert thing that looks different than the specs here)?

Just give me some time and **all your dreams will come true**.

## Apps using BottomBar

Send me a pull request with modified README.md to get a shoutout!

## Contributions

Feel free to create issues / pull requests.

## License

```
BottomBar library for Android
Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
