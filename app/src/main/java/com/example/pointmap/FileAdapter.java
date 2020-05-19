package com.example.pointmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> implements View.OnClickListener {
    private List<File> fileList;

    public FileAdapter(List<File> fileList){
        this.fileList=fileList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file=fileList.get(position);
        holder.fileIcon.setImageResource(file.getImageId());
        holder.fileName.setText(file.getFileName());
        holder.fileView.setTag(position);
        holder.fileIcon.setTag(position);
        holder.fileName.setTag(position);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View fileView;
        ImageView fileIcon;
        TextView fileName;

        public ViewHolder(View view){
            super(view);
            fileView=view;
            fileIcon=(ImageView)view.findViewById(R.id.card_icon);
            fileName=(TextView) view.findViewById(R.id.card_name);
            view.setOnClickListener(FileAdapter.this);
            fileName.setOnClickListener(FileAdapter.this);
            fileIcon.setOnClickListener(FileAdapter.this);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View v,int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener=onItemClickListener;
    }

    @Override
    public void onClick(View view) {
        onItemClickListener.onItemClick(view,(int)view.getTag());
    }
}
