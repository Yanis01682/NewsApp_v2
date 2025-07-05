package com.java.zhangzhiyuan.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.model.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<NewsItem> newsList;
    private static final String TAG = "NewsAdapter";

    // --- 新增：定义两种视图类型 ---
    private static final int VIEW_TYPE_ITEM = 0;     // 普通新闻项
    private static final int VIEW_TYPE_LOADING = 1;  // 底部加载项

    public NewsAdapter(Context context, List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 根据视图类型加载不同的布局
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);
        } else { // viewType == VIEW_TYPE_LOADING
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 根据 ViewHolder 的类型来绑定数据
        if (holder instanceof NewsViewHolder) {
            populateItemRows((NewsViewHolder) holder, position);
        }
        // LoadingViewHolder 不需要绑定任何数据，因为它只有一个ProgressBar
    }

    /**
     * 决定当前位置应该使用哪种视图类型
     */
    @Override
    public int getItemViewType(int position) {
        // 如果列表项是null，我们就认为它是加载项，否则是普通新闻项
        return newsList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return newsList == null ? 0 : newsList.size();
    }

    /**
     * 普通新闻项的 ViewHolder
     */
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView publisherTextView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.news_image);
            titleTextView = itemView.findViewById(R.id.news_title);
            publisherTextView = itemView.findViewById(R.id.news_publisher);
        }
    }

    /**
     * 底部加载项的 ViewHolder
     */
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            // 这里可以获取ProgressBar的引用，如果需要控制它的话
        }
    }

    /**
     * 填充普通新闻项数据的辅助方法
     */
    private void populateItemRows(NewsViewHolder holder, int position) {
        NewsItem news = newsList.get(position);
        holder.titleTextView.setText(news.getTitle());
        holder.publisherTextView.setText(news.getPublisher() + " - " + news.getPublishTime());

        String imageUrl = news.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }
    }
}