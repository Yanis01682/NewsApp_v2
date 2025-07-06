package com.java.zhangzhiyuan.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.java.zhangzhiyuan.ui.detail.NewsDetailActivity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        holder.titleTextView.setText(news.getTitle());
        holder.publisherTextView.setText(String.format("%s %s", news.getPublisher(), news.getPublishTime()));

        if (viewedNewsIds.contains(news.getNewsID())) {
            holder.titleTextView.setTextColor(Color.GRAY);
        } else {
            holder.titleTextView.setTextColor(Color.BLACK);
        }

        String imageUrl = news.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image_background)
                    .error(R.drawable.placeholder_image_background)
                    .into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

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
