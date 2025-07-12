package com.java.zhangzhiyuan.ui.home;
//高级搜索对话框
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.java.zhangzhiyuan.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AdvancedSearchDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "search_request";
    public static final String KEY_WORDS = "words";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_END_DATE = "end_date";

    private TextInputEditText etKeyword;
    private AutoCompleteTextView actvCategory;
    private Button btnDateRangePicker;
    private TextView tvSelectedDateRange;
    private Button btnSearch, btnCancel;

    private String selectedCategory = "";
    private String selectedStartDate = "";
    private String selectedEndDate = "";

    @Nullable
    @Override
    //加载XML布局文件
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_advanced_search, container, false);
    }

    @Override
    //进行UI组件初始化和设置监听器的最佳位置
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etKeyword = view.findViewById(R.id.et_keyword);
        actvCategory = view.findViewById(R.id.actv_category);
        btnDateRangePicker = view.findViewById(R.id.btn_date_range_picker);
        tvSelectedDateRange = view.findViewById(R.id.tv_selected_date_range);
        btnSearch = view.findViewById(R.id.btn_search);
        btnCancel = view.findViewById(R.id.btn_cancel);

        setupCategoryDropdown();
        setupListeners();
    }

    private void setupCategoryDropdown() {
        // 使用您在分类功能中定义好的分类列表
        String[] categories = {"娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        // 时间范围选择
        btnDateRangePicker.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("选择日期范围")
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                selectedStartDate = sdf.format(new Date(selection.first));
                selectedEndDate = sdf.format(new Date(selection.second));
                tvSelectedDateRange.setText(String.format("已选: %s 至 %s", selectedStartDate, selectedEndDate));
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        // 取消按钮
        btnCancel.setOnClickListener(v -> dismiss());

        // 搜索按钮
        btnSearch.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(KEY_WORDS, etKeyword.getText().toString().trim());
            result.putString(KEY_CATEGORY, actvCategory.getText().toString());
            result.putString(KEY_START_DATE, selectedStartDate);
            result.putString(KEY_END_DATE, selectedEndDate);

            // 使用Fragment Result API将数据返回给HomeFragment
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });
    }
}