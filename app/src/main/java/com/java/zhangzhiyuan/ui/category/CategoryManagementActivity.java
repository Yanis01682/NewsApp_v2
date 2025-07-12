package com.java.zhangzhiyuan.ui.category;
//用户通过点击和拖拽来管理他们的分类列表
//分类管理界面
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.model.Category;
import com.java.zhangzhiyuan.util.CategoryRepository;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {

    private RecyclerView rvMyCategories, rvMoreCategories;
    private CategoryManagementAdapter myCategoriesAdapter, moreCategoriesAdapter;
    private List<Category> myCategories, moreCategories;
    private CategoryRepository categoryRepository;
    private boolean hasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        Toolbar toolbar = findViewById(R.id.toolbar_category_management);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        categoryRepository = new CategoryRepository(this);
        myCategories = categoryRepository.getMyCategories();
        moreCategories = categoryRepository.getMoreCategories();

        setupRecyclerViews();
        setupItemTouchHelper(); // 调用新的方法
    }

    private void setupRecyclerViews() {
        rvMyCategories = findViewById(R.id.rv_my_categories);
        rvMoreCategories = findViewById(R.id.rv_more_categories);

        rvMyCategories.setLayoutManager(new GridLayoutManager(this, 4));
        rvMoreCategories.setLayoutManager(new GridLayoutManager(this, 4));

        myCategoriesAdapter = new CategoryManagementAdapter(myCategories);
        moreCategoriesAdapter = new CategoryManagementAdapter(moreCategories);

        rvMyCategories.setAdapter(myCategoriesAdapter);
        rvMoreCategories.setAdapter(moreCategoriesAdapter);
        //为“我的分类”列表项设置点击监听
        myCategoriesAdapter.setOnItemClickListener(position -> {
            if (myCategories.size() <= 1) return; // 至少保留一个分类
            // 从“我的分类”移动到“更多分类”
            Category category = myCategories.remove(position);
            moreCategories.add(0, category);
            myCategoriesAdapter.notifyItemRemoved(position);
            moreCategoriesAdapter.notifyItemInserted(0);
            hasChanged = true;
        });
        //从更多分类移动到我的分类
        moreCategoriesAdapter.setOnItemClickListener(position -> {
            Category category = moreCategories.remove(position);
            myCategories.add(category);
            moreCategoriesAdapter.notifyItemRemoved(position);
            myCategoriesAdapter.notifyItemInserted(myCategories.size() - 1);
            hasChanged = true;
        });
    }

    // 用于实现拖拽
    private void setupItemTouchHelper() {
        //创建ItemTouchHelper的回调
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //当一个item被拖动到新位置时
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                //通知Adapter更新数据位置并播放动画
                myCategoriesAdapter.onItemMove(fromPosition, toPosition);
                hasChanged = true;
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 我们不需要左右滑动删除，所以这里为空
            }
        };
        //创建ItemTouchHelper实例并附加到RecyclerView上
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvMyCategories);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hasChanged) {
            categoryRepository.saveMyCategories(myCategories);
        }
    }
}