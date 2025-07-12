package com.java.zhangzhiyuan.ui.home;
//ide自动生成的，实际上没有被用到，开发时选择的时在HomeFragment内部直接管理状态
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("这里是首页");
    }

    public LiveData<String> getText() {
        return mText;
    }
}