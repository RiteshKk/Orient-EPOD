package com.ipssi.orient_epod.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ipssi.orient_epod.callbacks.OnImageDeleteClickListener;
import com.ipssi.orient_epod.databinding.ImageViewBinding;
import com.ipssi.orient_epod.model.UploadDocumentEntity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<UploadDocumentEntity> images = new ArrayList<>();
    private OnImageDeleteClickListener listener;

    public ImageAdapter(OnImageDeleteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(ImageViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadDocumentEntity entity = images.get(position);
        holder.bind(entity.getDriverDocs());
        holder.binding.btnDeleteImage.setOnClickListener(v -> listener.onImageDeleteClick(entity));
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    public void setImages(ArrayList<UploadDocumentEntity> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageViewBinding binding;

        public void bind(String image) {
            binding.setBase64String(image);
        }

        public ViewHolder(@NotNull ImageViewBinding inflate) {
            super(inflate.getRoot());
            binding = inflate;
        }
    }
}
