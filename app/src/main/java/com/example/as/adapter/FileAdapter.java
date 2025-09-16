package com.example.as.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.as.R;
import com.example.as.model.FileItem;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileItem> fileList;
    private OnDownloadClickListener listener;

    public interface OnDownloadClickListener {
        void onDownloadClick(int position);
    }

    public FileAdapter(List<FileItem> fileList, OnDownloadClickListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem item = fileList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName, tvFileSize, tvProgress;
        ProgressBar progressBar;
        ImageView ivStatus;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
            ivStatus = itemView.findViewById(R.id.ivStatus);
        }

        public void bind(final FileItem item, final OnDownloadClickListener listener) {
            tvFileName.setText(item.getName());
            tvFileSize.setText(item.getSize());

            switch (item.getStatus()) {
                case NOT_STARTED:
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    ivStatus.setImageResource(R.drawable.ic_download);
                    ivStatus.setClickable(true);
                    break;
                case DOWNLOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    tvProgress.setVisibility(View.VISIBLE);
                    progressBar.setProgress(item.getProgress());
                    tvProgress.setText(item.getProgress() + "%");
                    ivStatus.setClickable(false); // No se puede clickear mientras descarga
                    break;
                case COMPLETED:
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    ivStatus.setImageResource(R.drawable.ic_check);
                    ivStatus.setClickable(false);
                    break;
            }

            ivStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadClick(getAdapterPosition());
                }
            });
        }
    }
}