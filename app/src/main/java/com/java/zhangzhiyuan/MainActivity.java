package com.java.zhangzhiyuan;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.java.zhangzhiyuan.adapter.MainViewPagerAdapter;
import com.java.zhangzhiyuan.databinding.ActivityMainBinding;
import com.java.zhangzhiyuan.ui.category.CategoryFragment;
import com.java.zhangzhiyuan.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewPager2 mainViewPager = binding.mainViewPager;
        BottomNavigationView navView = binding.navView;

        // 创建并设置适配器
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(this);
        mainViewPager.setAdapter(adapter);

        // 核心：设置页面缓存，确保所有页面都保留在内存中，从而保存状态
        mainViewPager.setOffscreenPageLimit(3);

        // 设置页面切换时的监听，让底部导航栏的图标跟随页面变化
        mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        navView.setSelectedItemId(R.id.navigation_home);
                        break;
                    case 1:
                        navView.setSelectedItemId(R.id.navigation_category);
                        break;
                    case 2:
                        navView.setSelectedItemId(R.id.navigation_notifications);
                        break;
                }
            }
        });

        // 设置底部导航栏的点击监听，让页面跟随图标切换
        // 注意：这里使用 setOnNavigationItemSelectedListener
        navView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                mainViewPager.setCurrentItem(0, false); // false表示切换时不要平滑滚动
            } else if (itemId == R.id.navigation_category) {
                mainViewPager.setCurrentItem(1, false);
            } else if (itemId == R.id.navigation_notifications) {
                mainViewPager.setCurrentItem(2, false);
            }
            return true;
        });

        // 这里是我们之前为“重复点击刷新”添加的逻辑，依然保留
        navView.setOnItemReselectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + mainViewPager.getCurrentItem());

            if (currentFragment != null) {
                if (item.getItemId() == R.id.navigation_home && currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).scrollToTopAndRefresh();
                } else if (item.getItemId() == R.id.navigation_category && currentFragment instanceof CategoryFragment) {
                    ((CategoryFragment) currentFragment).refreshCurrentList();
                }
            }
        });
    }
}