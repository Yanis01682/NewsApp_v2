package com.java.zhangzhiyuan;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
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

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_category, R.id.navigation_notifications)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.navView, navController);

        // vvv--- 将整个 setOnItemReselectedListener 监听器替换为下面的版本 ---vvv
        binding.navView.setOnItemReselectedListener(item -> {
            if (navHostFragment == null) {
                return;
            }

            // 这是从 NavHostFragment 中获取当前主导航Fragment的最可靠方法
            Fragment primaryFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

            if (primaryFragment == null) {
                return;
            }

            // 判断当前Fragment的类型，并调用其公共的刷新方法
            if (item.getItemId() == R.id.navigation_home && primaryFragment instanceof HomeFragment) {
                ((HomeFragment) primaryFragment).scrollToTopAndRefresh();
            }
            // 暂时先注释掉分类的刷新，确保首页的功能先恢复正常

            else if (item.getItemId() == R.id.navigation_category && primaryFragment instanceof CategoryFragment) {
                ((CategoryFragment) primaryFragment).refreshCurrentList();
            }

        });
        // ^^^--- 替换结束 ---^^^
    }

}