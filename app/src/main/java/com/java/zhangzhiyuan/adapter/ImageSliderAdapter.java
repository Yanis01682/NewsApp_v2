package com.java.zhangzhiyuan.adapter;
//负责将一个包含图片URL字符串的列表转换成用户可以左右滑动的图片。
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.java.zhangzhiyuan.R;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private final List<String> imageUrls;
    private OnAllImagesFailedListener allImagesFailedListener;
    private int failedImageCount = 0;

    // 1. 定义回调接口
    public interface OnAllImagesFailedListener {
        void onAllImagesFailed();
    }

    public void setOnAllImagesFailedListener(OnAllImagesFailedListener listener) {
        this.allImagesFailedListener = listener;
    }

    public ImageSliderAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载 item_image_slider.xml 布局，它内部只有一个ImageView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        // 使用Glide加载图片
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image_background)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        // 1. 追踪失败数量
                        failedImageCount++;
                        // 2. 检查是否所有图片都已失败
                        if (failedImageCount == getItemCount() && allImagesFailedListener != null) {
                            allImagesFailedListener.onAllImagesFailed();
                        }
                        // 返回 false 让 Glide 自己处理占位符等
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls == null ? 0 : imageUrls.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image_view);
        }
    }
}
