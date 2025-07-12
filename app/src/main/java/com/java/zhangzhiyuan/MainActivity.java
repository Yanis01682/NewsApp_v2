package com.java.zhangzhiyuan;
//搭建应用的主界面，
// 并管理底部导航栏，
// 让用户可以在“首页”、“新闻分类”和“我的”这三个核心功能模块之间切换。
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
    //安卓官方提供的一个“基础屏幕蓝图“

    private ActivityMainBinding binding;
    //ActivityMainBinding 是一个自动生成的类，根据布局文件 activity_main.xml 自动生成的。

    @Override
    //系统会把一些重要的临时数据（比如输入框里已经输入的文字）打包放到一个 Bundle 里。当新的Activity被创建，系统会把这个“包裹”通过 savedInstanceState 参数传回来。
    protected void onCreate(Bundle savedInstanceState) {
        //super 指的就是 AppCompatActivity,是父类的意思
        super.onCreate(savedInstanceState);
        //禁用夜间模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        //inflate就是根据activity_main.xml填充布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //让布局在屏幕上显示出来
        setContentView(binding.getRoot());
        //这两个都在activity_main中定义
        //ViewPager2：一个可以切换的内容展示区
        ViewPager2 mainViewPager = binding.mainViewPager;
        //BottomNavigationView：位于屏幕底部的导航栏，提供可点击的图标，用于切换 ViewPager2 中显示的页面。
        BottomNavigationView navView = binding.navView;

        // 创建并设置适配器
        //适配器是ui与数据之间的桥梁
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(this);
        mainViewPager.setAdapter(adapter);

        // 切换底部Tab不刷新页面
        //”离屏页面限制数”。“离屏”就是指那些没有显示在当前屏幕上，但在左右两边的页面。
        mainViewPager.setOffscreenPageLimit(3);

        // 不允许用户左右滑动来切换导航页
        mainViewPager.setUserInputEnabled(false);

        // 设置页面切换时的监听，让底部导航栏的图标跟随页面变化，并实现高亮
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

        // tab的“重复点击刷新”（重新选中）
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