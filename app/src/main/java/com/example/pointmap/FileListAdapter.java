package com.example.pointmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> implements View.OnClickListener {
    private List<File> fileList;

    public FileListAdapter(List<File> fileList){
        this.fileList=fileList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item,parent,false);
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
            fileIcon=(ImageView)view.findViewById(R.id.file_icon);
            fileName=(TextView) view.findViewById(R.id.file_name);
            view.setOnClickListener(FileListAdapter.this);
            fileName.setOnClickListener(FileListAdapter.this);
            fileIcon.setOnClickListener(FileListAdapter.this);
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
//    @Override
//    public void onClick(View view){
//        int position=(int)view.getTag();
//        if(onItemClickListener!=null){
//            switch (view.getId()){
//                case R.id.recycler_view_fileList:
//                    onItemClickListener.onItemClick(view,position);
//                    break;
//                default:
//                    onItemClickListener.onItemClick(view,position);
//                    break;
//            }
//        }
//    }

}
