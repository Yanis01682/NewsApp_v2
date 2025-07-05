package com.java.zhangzhiyuan.ui.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.model.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    // private final Context context; // 不再需要这个全局context
    private final List<NewsItem> newsList;
    private static final String TAG = "NewsAdapter"; // 定义日志TAG

    public NewsAdapter(Context context, List<NewsItem> newsList) {
        // this.context = context;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 直接使用parent的context，这是最佳实践
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem news = newsList.get(position);
        holder.titleTextView.setText(news.getTitle());
        holder.publisherTextView.setText(news.getPublisher() + " - " + news.getPublishTime());

        String imageUrl = news.getImage();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // ==================== 这里是核心修改点 ====================
            // 使用带有详细错误监听器的Glide调用

            Glide.with(holder.itemView.getContext()) // 使用holder的context，更安全
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // 图片加载失败时，这段代码会被执行
                            Log.e(TAG, "Glide加载图片失败，URL: " + model);
                            if (e != null) {
                                // 打印完整的、详细的错误堆栈信息，这是我们破案的关键！
                                Log.e(TAG, "GlideException详情: ", e);
                            }
                            // 必须返回false，Glide才会继续显示你设置的error占位图
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // 图片加载成功时，这段代码会被执行（可选，用于调试）
                            Log.d(TAG, "Glide成功加载图片: " + model);
                            // 必须返回false，让Glide自己去把图片设置到ImageView上
                            return false;
                        }
                    })
                    .placeholder(R.drawable.ic_launcher_background) // 加载中的占位图
                    .error(R.drawable.ic_launcher_foreground)       // 加载失败时显示的图（建议和加载中用不同的图，方便区分）
                    .into(holder.imageView); // 你的ViewHolder里的ImageView
            // ========================================================
        } else {
            // 如果没有图片URL，清理旧图或设置一个默认图
            Glide.with(holder.itemView.getContext()).clear(holder.imageView); // 清理，防止复用出错
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView publisherTextView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            // 确保这里的ID和你的item_news.xml文件中的ID一致
            imageView = itemView.findViewById(R.id.news_image);
            titleTextView = itemView.findViewById(R.id.news_title);
            publisherTextView = itemView.findViewById(R.id.news_publisher);
        }
    }
}