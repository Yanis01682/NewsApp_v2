package com.java.zhangzhiyuan.ui.my;
//没有被实际使用
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("我的页面");
    }

    public LiveData<String> getText() {
        return mText;
    }
}