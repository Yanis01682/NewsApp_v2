package com.java.zhangzhiyuan;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
// Import the NavHostFragment
import androidx.navigation.fragment.NavHostFragment;
import com.java.zhangzhiyuan.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
//进入软件的加载
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 填充布局 (Inflate the Layout)
        // 这行代码会读取你的XML布局文件(activity_main.xml)，并创建其中定义的所有视图对象，
        // 然后将这些对象的引用放入 'binding' 这个变量中，方便后续直接调用。
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // 2. 设置内容视图 (Set the Content View)
        // 这行代码告诉Activity：“好了，现在请把我们刚刚填充好的那个布局显示出来。”
        setContentView(binding.getRoot());

        // 3. 找到底部导航栏视图
        // 我们从布局中获取对 BottomNavigationView 这个UI组件的直接引用。
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 4. 配置顶级页面 (Top-Level Screens)
        // 这行代码告诉导航系统，哪些页面是“主页面”。
        // 当你位于这些主页面时，应用顶部的标题栏不会显示“返回”箭头。
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        // --- 修复闪退的关键代码 ---

        // 5. 获取导航宿主Fragment (Navigation Host Fragment)
        // 这是最重要的一步。我们不再让整个Activity去寻找导航控制器，
        // 而是先找到那个被指定为所有其他Fragment“宿主”的特定Fragment。
        // 这个宿主就是你在 activity_main.xml 中定义的 <FragmentContainerView>。
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        // 6. 从宿主获取导航控制器 (Navigation Controller)
        // 拿到了宿主之后，我们再向它索要它的控制器。NavController 就像是你应用的
        // “交通警察”或者“电视频道切换器”——它真正负责执行页面（Fragment）的切换。
        // 这是获取 NavController 最可靠的方式。
        NavController navController = navHostFragment.getNavController();

        // 7. 连接顶部标题栏 (Action Bar)
        // 这行代码将你的顶部标题栏与 NavController 连接起来。它会自动完成两件事：
        //  - 根据当前页面的标签(label)，自动更改标题栏的文字。
        //  - 当你从主页面进入更深的页面时，自动显示和处理“向上”返回箭头。
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 8. 连接底部导航栏 (Bottom Navigation View)
        // 这是最后的连接。它将你的 BottomNavigationView (navView) 与 NavController 连接起来。
        // 当你点击底部导航栏的按钮时，这个设置会告诉 NavController 导航到对应的页面。
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}