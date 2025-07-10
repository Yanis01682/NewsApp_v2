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

@OptIn(markerClass = UnstableApi.class) // <--- 2. 在类声明的上一行添加这个注解

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

//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof NewsViewHolder) {
//            populateItemRows((NewsViewHolder) holder, position);
//        }
//    }

    //新的！！！
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsViewHolder) {
            populateItemRows((NewsViewHolder) holder, position);
            NewsItem news = newsList.get(position);
            if (news != null) {
                // 在此处调用logDetails方法
                news.logDetails("NewsAdapter");
            }
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

        holder.titleTextView.setText(news.getTitle());
        holder.publisherTextView.setText(String.format("%s %s", news.getPublisher(), news.getPublishTime()));

        if (news.getUrl() != null && viewedNewsIds.contains(news.getUrl())) {
            holder.titleTextView.setTextColor(Color.GRAY);
        } else {
            holder.titleTextView.setTextColor(Color.BLACK);
        }

        String imageUrl = news.getImage();
        // --- 核心修正：在这里清理旧图片并根据URL设置可见性 ---
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE); // 先确保可见
            Glide.with(context)
                    .load(imageUrl)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.imageView.setVisibility(View.GONE); // 加载失败则隐藏
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.imageView.setVisibility(View.VISIBLE); // 加载成功确保可见
                            return false;
                        }
                    })
                    .into(holder.imageView);
        } else {
            // 如果没有图片URL，不仅要隐藏，还要清除可能存在的旧图
            holder.imageView.setVisibility(View.GONE);
            Glide.with(context).clear(holder.imageView);
        }
        // --- 修正结束 ---


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            intent.putExtra("news_item", news);
            context.startActivity(intent);
        });
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView publisherTextView;
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            // 确保ID与item_news.xml中的ID完全一致
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