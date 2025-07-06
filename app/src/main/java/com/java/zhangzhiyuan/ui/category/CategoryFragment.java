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

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.model.Category;
import com.java.zhangzhiyuan.util.CategoryRepository;

import java.util.List;

public class CategoryFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView ivCategoryManage;
    private CategoryPagerAdapter pagerAdapter;
    private List<Category> myCategories;
    private CategoryRepository categoryRepository;

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
        categoryRepository = new CategoryRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        initViews(view);
        loadCategories();
        setupViewPager();
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout_category);
        viewPager = view.findViewById(R.id.view_pager_category);
        ivCategoryManage = view.findViewById(R.id.iv_category_manage);
    }

    private void loadCategories() {
        myCategories = categoryRepository.getMyCategories();
    }

    private void setupViewPager() {
        pagerAdapter = new CategoryPagerAdapter(getChildFragmentManager(), getLifecycle(), myCategories);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(myCategories.get(position).getName());
        }).attach();
    }

    private void setupListeners() {
        ivCategoryManage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoryManagementActivity.class);
            categoryManagementLauncher.launch(intent);
        });
    }

    private void reloadCategoriesAndRefreshUI() {
        // 记录当前选中的tab位置
        int currentPosition = tabLayout.getSelectedTabPosition();

        // 重新从仓库加载分类
        myCategories.clear();
        myCategories.addAll(categoryRepository.getMyCategories());

        // 通知Adapter数据已更新
        pagerAdapter.notifyDataSetChanged();

        // 尝试恢复之前的选中位置
        if (currentPosition >= 0 && currentPosition < myCategories.size()) {
            viewPager.setCurrentItem(currentPosition, false); // false表示不要平滑滚动
        }
    }
}