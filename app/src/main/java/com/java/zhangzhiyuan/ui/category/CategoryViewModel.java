package com.java.zhangzhiyuan.ui.category;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * 这个ViewModel扮演一个通信中心的角色（告示板）。
 * CategoryFragment（父）向这里发布刷新请求。
 * CategoryNewsListFragment（子）在这里订阅刷新通知。
 */
public class CategoryViewModel extends ViewModel {

    // 创建一个可以被观察的“告示”，内容是要刷新的分类名 (String)
    private final MutableLiveData<String> refreshEvent = new MutableLiveData<>();

    // 提供一个公开的方法，让“父亲”可以贴“告示”
    public void requestRefreshForCategory(String categoryName) {
        refreshEvent.setValue(categoryName);
    }

    // 提供一个公开的信箱，让“孩子们”可以订阅“告示”
    public LiveData<String> getRefreshEvent() {
        return refreshEvent;
    }
}