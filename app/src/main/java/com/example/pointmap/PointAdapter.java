package com.example.pointmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.ViewHolder> implements View.OnClickListener {
    private Context mContext;
    private List<Point> mPointList;

    public PointAdapter(List<Point> pointList){
        mPointList=pointList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        if(mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.card_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                Point point=mPointList.get(position);
                Intent intent=new Intent(view.getContext(),InfochangeActivity.class);
                intent.putExtra("coord_Data",point.getJsonObject().toString());
                ActivityCompat.startActivityForResult((Activity) view.getContext(),intent,2,null);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        Point point=mPointList.get(position);
        holder.pointName.setText(point.getPointName());
        Glide.with(mContext).load(point.getImageUri()).into(holder.pointImage);
        holder.cardView.setTag(position);
        holder.pointImage.setTag(position);
        holder.pointName.setTag(position);
    }

    @Override
    public int getItemCount(){
        return mPointList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView pointImage;
        TextView pointName;

        public ViewHolder(View view){
            super(view);
            cardView=(CardView)view;
            pointImage=(ImageView)view.findViewById(R.id.card_icon);
            pointName=(TextView)view.findViewById(R.id.card_name);
            cardView.setOnClickListener(PointAdapter.this);
            pointImage.setOnClickListener(PointAdapter.this);
            pointName.setOnClickListener(PointAdapter.this);
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
