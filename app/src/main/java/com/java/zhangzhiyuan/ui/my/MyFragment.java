package com.java.zhangzhiyuan.ui.my;
//实现了一个简单的入口导航
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.java.zhangzhiyuan.databinding.FragmentMyBinding;

public class MyFragment extends Fragment {

    private FragmentMyBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // ViewModel的初始化是标准流程
        MyViewModel myViewModel =
                new ViewModelProvider(this).get(MyViewModel.class);

        binding = FragmentMyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置所有监听器
        setupListeners();

        return root;
    }
    //为布局中的三个LinearLayout（历史记录、我的收藏、我的笔记）设置了点击监听器。
    private void setupListeners() {
        // 核心修正：将点击事件绑定到整个LinearLayout上，而不是不存在的TextView
        binding.layoutHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewsListActivity.class);
            intent.putExtra(NewsListActivity.EXTRA_TYPE, NewsListActivity.TYPE_HISTORY);
            startActivity(intent);
        });

        binding.layoutFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewsListActivity.class);
            intent.putExtra(NewsListActivity.EXTRA_TYPE, NewsListActivity.TYPE_FAVORITES);
            startActivity(intent);
        });
        binding.layoutNotes.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewsListActivity.class);
            intent.putExtra(NewsListActivity.EXTRA_TYPE, NewsListActivity.TYPE_NOTES);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}