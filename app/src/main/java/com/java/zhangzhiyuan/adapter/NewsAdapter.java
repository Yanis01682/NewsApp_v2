package com.java.zhangzhiyuan.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.annotation.OptIn;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.model.NewsItem;
import com.java.zhangzhiyuan.ui.detail.NewsDetailActivity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@OptIn(markerClass = UnstableApi.class)
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<NewsItem> newsList;
    private Set<String> viewedNewsIds = new HashSet<>();

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    public NewsAdapter(Context context, List<NewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    public void setViewedNewsIds(Set<String> viewedNewsIds) {
        this.viewedNewsIds = viewedNewsIds;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsViewHolder) {
            populateItemRows((NewsViewHolder) holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < newsList.size() && newsList.get(position) != null) {
            return VIEW_TYPE_ITEM;
        } else {
            return VIEW_TYPE_LOADING;
        }
    }

    @Override
    public int getItemCount() {
        return newsList == null ? 0 : newsList.size();
    }

    private void populateItemRows(NewsViewHolder holder, int position) {
        NewsItem news = newsList.get(position);
        if (news == null) return;

        // --- 1. 文本和点击事件的逻辑保持不变 ---
        holder.titleTextView.setText(news.getTitle());
        holder.publisherTextView.setText(String.format("%s %s", news.getPublisher(), news.getPublishTime()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            intent.putExtra("news_item", news);
            context.startActivity(intent);
        });
        if (news.getUrl() != null && viewedNewsIds.contains(news.getUrl())) {
            holder.titleTextView.setTextColor(Color.GRAY);
        } else {
            holder.titleTextView.setTextColor(Color.BLACK);
        }

        // --- 2. 【终极版】图片处理逻辑 ---
        final String imageUrl = news.getImage();

        // 分支 A: 如果这个新闻【没有】图片URL
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // 【关键步骤 1】: 强制取消之前可能在这个ImageView上进行的任何Glide加载任务。
            Glide.with(context).clear(holder.imageView);
            // 【关键步骤 2】: 将ImageView彻底隐藏，并且不占用任何布局空间。
            holder.imageView.setVisibility(View.GONE);
        }
        // 分支 B: 如果这个新闻【有】图片URL
        else {
            // 【关键步骤 3】: 先让ImageView在布局中可见，准备好承载新图片。
            holder.imageView.setVisibility(View.VISIBLE);
            // 【关键步骤 4】: 使用Glide加载新图片。
            Glide.with(context)
                    .load(imageUrl)
                    // 让Glide来管理占位图，它会在开始加载时显示
                    .placeholder(R.drawable.placeholder_image_background)
                    // 如果加载失败，显示一张“图片损坏”的图标
                    .error(R.drawable.ic_baseline_broken_image_24)
                    .into(holder.imageView);
        }
    }


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

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}