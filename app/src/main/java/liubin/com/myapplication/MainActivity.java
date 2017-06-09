/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package liubin.com.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.mylibrary.base.ActivityUtils;
import com.example.mylibrary.base.BaseActivity;
import com.example.mylibrary.base.BaseFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.bind.TypeAdapters;
import java.util.ArrayList;
import java.util.List;
import liubin.com.myapplication.bean.User;
import liubin.com.myapplication.fragments.BasicFragment;
import liubin.com.myapplication.fragments.CoordinatorLayoutFragment;
import liubin.com.myapplication.fragments.CustomFragment;
import liubin.com.myapplication.fragments.DrawerLayoutFragment;

/**
 * DrawerLayout NavigationView CoordinatorLayout嵌套使用
 * 状态栏层级从下到上 依次是 CoordinatorLayout,NavigationView,系统状态栏
 */
public class MainActivity extends BaseActivity {

  @BindView(R.id.toolbar) Toolbar mToolbar;
  @BindView(R.id.tabs) TabLayout mTabLayout;
  @BindView(R.id.appbar) AppBarLayout mAppbar;
  @BindView(R.id.viewpager) ViewPager mViewpager;
  @BindView(R.id.fab) FloatingActionButton mFab;
  @BindView(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
  @BindView(R.id.nav_view) NavigationView mNavigationView;
  @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

  @Override public int getContentResourceId() {
    return R.layout.activity_main;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    setTransparentForWindow();
    // 注意区分版本进行处理
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mCoordinatorLayout.setPadding(0, this.getSystemBarConfig().getStatusBarHeight(), 0, 0);
      // 设置(覆盖在 CoordinatorLayout 上)状态栏颜色
      mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      mCoordinatorLayout.setPadding(0, 0, 0, 0);
      // 状态栏颜色设置
      mCoordinatorLayout.setBackgroundColor(getResources().getColor(R.color.primary_dark));
    }
    // 这一句是关键,布局文件里面设置这个属性为true,代码里面需要设置这个属性为false
    mDrawerLayout.setFitsSystemWindows(false);

    setupDrawerContent(mNavigationView);
    setSupportActionBar(mToolbar);
    setupViewPager(mViewpager);

    final ActionBar ab = getSupportActionBar();
    ab.setHomeAsUpIndicator(R.drawable.ic_menu);
    ab.setDisplayHomeAsUpEnabled(true);

    mFab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {

        User user = new User();
        user.setAge(12);
        user.setName("asdf");
        String s = new Gson().toJson(user);
        user = getGson().fromJson(s, user.getClass());
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        /*Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show();*/
      }
    });

    mTabLayout.setupWithViewPager(mViewpager);
    mViewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override public void onTabSelected(TabLayout.Tab tab) {
        Log.d("asdf", "sadf");
      }

      @Override public void onTabUnselected(TabLayout.Tab tab) {
        Log.d("asdf", "sadf");
      }

      @Override public void onTabReselected(TabLayout.Tab tab) {
        Log.d("asdf", "sadf");
      }
    });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.sample_actions, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * 初始化ViewPager
   *
   * @param viewPager {@link ViewPager}
   */
  private void setupViewPager(ViewPager viewPager) {
    Adapter adapter = new Adapter(getSupportFragmentManager());
    adapter.addFragment(new CheeseListFragment(), "Category 1");
    adapter.addFragment(new CheeseListFragment(), "Category 2");
    adapter.addFragment(new CheeseListFragment(), "Category 3");
    viewPager.setAdapter(adapter);
  }

  /**
   * 初始化左侧导航栏
   *
   * @param navigationView {@link NavigationView}
   */
  private void setupDrawerContent(NavigationView navigationView) {
    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
              case R.id.nav_home: {//基本使用
                ActivityUtils.startActivity(MainActivity.this, BasicFragment.class, null, -1);
                break;
              }
              case R.id.nav_custom: {//自定义
                ActivityUtils.startActivity(MainActivity.this, CustomFragment.class, null, -1);
                break;
              }
              case R.id.nav_coordinator_layout: {//CoordinatorLayout使用
                ActivityUtils.startActivity(MainActivity.this, CoordinatorLayoutFragment.class,
                    null, -1);
                break;
              }
              case R.id.nav_messages: {
                ActivityUtils.startActivity(MainActivity.this, DrawerLayoutFragment.class, null,
                    -1);
                break;
              }
              case R.id.nav_friends: {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), FullscreenActivity.class);
                startActivity(intent);
                break;
              }
            }
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
          }
        });
  }

  /**
   * ViewPage适配器
   */
  public static class Adapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    public Adapter(FragmentManager fm) {
      super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
      mFragments.add(fragment);
      mFragmentTitles.add(title);
    }

    @Override public Fragment getItem(int position) {
      return mFragments.get(position);
    }

    @Override public int getCount() {
      return mFragments.size();
    }

    @Override public CharSequence getPageTitle(int position) {
      return mFragmentTitles.get(position);
    }
  }

  public static Gson getGson() {
    return new GsonBuilder().create();
    /*return (new GsonBuilder()).registerTypeAdapterFactory(
        TypeAdapters.newFactory(Integer.TYPE, Integer.class, new IntegerAdapter()))
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(Double.TYPE, Double.class, new TeaDoubleAdapter()))
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(Long.TYPE, Long.class, new LongAdapter()))
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(Boolean.TYPE, Boolean.class, new BooleanAdapter()))
        .registerTypeAdapterFactory(
            TypeAdapters.newFactory(String.class, String.class, new TeaStringAdapter()))
        .create();*/
  }
}
