package com.java.zhangzhiyuan.ui.category;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.model.Category;
import com.java.zhangzhiyuan.util.CategoryRepository;

import java.util.List;

public class CategoryFragment extends Fragment {

    //顶部的tab
    private TabLayout tabLayout;
    //下方的内容去
    private ViewPager2 viewPager;
    private ImageView ivCategoryManage;
    private CategoryPagerAdapter pagerAdapter;
    private List<Category> myCategories;
    private CategoryRepository categoryRepository;
    private TabLayoutMediator tabLayoutMediator;
    private CategoryViewModel categoryViewModel;

    // 一个标志位，用于判断是否是第一次加载
    private boolean isInitialDataLoaded = false;


    private final ActivityResultLauncher<Intent> categoryManagementLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // 当从分类管理页面返回时，这个回调会被触发
                if (result.getResultCode() == Activity.RESULT_OK || result.getResultCode() == Activity.RESULT_CANCELED) {
                    // 不论是按返回键还是正常退出，都重新加载分类并刷新UI
                    reloadCategoriesAndRefreshUI();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化数据仓库
        categoryRepository = new CategoryRepository(requireContext());

        // 初始化用于通信的ViewModel
        // 通过这种方式获取的ViewModel，可以被所有子Fragment共享
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //加载布局文件
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        //初始化ui组件
        initViews(view);
        // onCreateView只负责创建视图，不加载任何数据
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 在页面可见时，才决定是否加载数据
        if (!isInitialDataLoaded) {
            // 如果是第一次进入该页面，则开始加载分类数据
            loadCategoriesAndSetupUI();
            // 标记为已加载过
            isInitialDataLoaded = true;
        }
    }

    private void loadCategoriesAndSetupUI(){
        loadCategories();
        setupViewPagerAndTabs();
        setupListeners();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout_category);
        viewPager = view.findViewById(R.id.view_pager_category);
        ivCategoryManage = view.findViewById(R.id.iv_category_manage);
    }

    private void loadCategories() {
        myCategories = categoryRepository.getMyCategories();
    }

    private void setupViewPagerAndTabs() {
        // 在Fragment中使用ViewPager2，应使用getChildFragmentManager()和getLifecycle()
        pagerAdapter = new CategoryPagerAdapter(getChildFragmentManager(), getLifecycle(), myCategories);
        viewPager.setAdapter(pagerAdapter);

        // --- 核心修正：将TabLayoutMediator的创建和附加逻辑也封装起来 ---
        // 设置页面缓存数量。我们把它设置成一个稍大的值（比如10），
        // 这样在您滑动分类时，绝大部分页面都会被保留在内存中，不会被销毁和重建。
        viewPager.setOffscreenPageLimit(10);

        attachTabs();
    }

    private void attachTabs() {
        // 如果已存在一个Mediator，先将其分离，防止内存泄漏和重复附加
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }

        // 创建新的Mediator实例，并将TabLayout与ViewPager2关联起来
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position >= 0 && position < myCategories.size()) {
                tab.setText(myCategories.get(position).getName());
            }
        });
        tabLayoutMediator.attach();
    }


    private void setupListeners() {
        ivCategoryManage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoryManagementActivity.class);
            categoryManagementLauncher.launch(intent);
        });
    }

    private void reloadCategoriesAndRefreshUI() {
        // 记录当前选中的tab位置，以便刷新后恢复
        int currentPosition = tabLayout.getSelectedTabPosition();

        // 1. 从仓库重新加载最新的分类列表
        loadCategories();

        // 2. 创建一个全新的适配器实例
        pagerAdapter = new CategoryPagerAdapter(getChildFragmentManager(), getLifecycle(), myCategories);

        // 3. 将新适配器设置给ViewPager
        viewPager.setAdapter(pagerAdapter);

        // 4. 【关键步骤】重新附加TabLayout，以根据新数据刷新顶部的标签
        attachTabs();

        // 5. 尝试恢复之前的选中位置
        if (currentPosition >= 0 && currentPosition < myCategories.size()) {
            viewPager.setCurrentItem(currentPosition, false);
        }
    }

    public void refreshCurrentList() {
        // 确保UI组件都已准备好
        if (tabLayout != null && myCategories != null && !myCategories.isEmpty()) {
            // 获取当前选中的tab的位置
            int currentPosition = tabLayout.getSelectedTabPosition();
            if (currentPosition >= 0 && currentPosition < myCategories.size()) {
                // 获取当前分类的名字
                String currentCategoryName = myCategories.get(currentPosition).getName();

                // 使用ViewModel发布一个刷新请求，把分类名作为“告示”内容
                categoryViewModel.requestRefreshForCategory(currentCategoryName);
            }
        }
    }
}