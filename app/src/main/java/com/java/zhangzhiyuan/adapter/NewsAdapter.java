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

        // --- 文本和点击事件的设置 (这部分不变) ---
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

        // --- 【最终版图片处理铁律】 ---
        String imageUrl = news.getImage();

        // 1. 判断是否有图片
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            // 如果有，就必须先让图片框可见，准备接收图片
            holder.imageView.setVisibility(View.VISIBLE);

            // 然后命令Glide加载图片
            Glide.with(context)
                    .load(imageUrl)
                    // 加载开始前，显示一个占位的“框框”
                    .placeholder(R.drawable.placeholder_image_background)
                    // 如果加载失败（链接失效等），也显示同一个“框框”或一个错误图标
                    // 为了实现“加载失败就不显示”，请确保你的error drawable是透明的，或者直接移除.error()这一行
                    .error(R.drawable.placeholder_image_background) // 或者注释掉/删除此行
                    .into(holder.imageView);

        } else {
            // 2. 如果没有任何图片URL
            // a. 清除这个ImageView可能因为复用而残留的任何旧图片
            Glide.with(context).clear(holder.imageView);
            // b. 强制将它从布局中彻底隐藏
            holder.imageView.setVisibility(View.GONE);
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