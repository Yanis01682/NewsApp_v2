package com.java.zhangzhiyuan.adapter;
//构建了item_note.xml
//因为“我的笔记”列表的样式和普通新闻列表不同，所以它需要一个专门的适配器和专门的列表项布局。
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.model.NewsItem;
import com.java.zhangzhiyuan.model.NoteRecord;
import com.java.zhangzhiyuan.ui.detail.NewsDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final Context context;
    private final List<NoteRecord> noteList;
    private final Gson gson = new Gson();

    public NoteAdapter(Context context, List<NoteRecord> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteRecord note = noteList.get(position);
        //  将JSON字符串反序列化回NewsItem对象
        NewsItem newsItem = gson.fromJson(note.newsItemJson, NewsItem.class);
        //  填充UI组件
        holder.titleTextView.setText(newsItem.getTitle());
        // 确保这里引用的 sourceTimeTextView 能在 ViewHolder 中被正确初始化
        holder.sourceTimeTextView.setText(String.format("%s %s", newsItem.getPublisher(), newsItem.getPublishTime()));
        holder.previewTextView.setText(note.noteContent);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.updateTimeTextView.setText("更新于: " + sdf.format(new Date(note.updateTime)));
        //设置点击事件
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            intent.putExtra("news_item", newsItem);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteList == null ? 0 : noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView sourceTimeTextView;
        TextView previewTextView;
        TextView updateTimeTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_news_title);
            // 确保这里能找到正确的ID
            sourceTimeTextView = itemView.findViewById(R.id.note_news_source_time);
            previewTextView = itemView.findViewById(R.id.note_preview);
            updateTimeTextView = itemView.findViewById(R.id.note_update_time);
        }
    }
}
