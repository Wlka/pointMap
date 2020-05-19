package com.example.pointmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonParser;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.ViewHolder>{
    private Context mContext;
    private List<Point> mPointList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView pointImage;
        TextView pointName;

        public ViewHolder(View view){
            super(view);
            cardView=(CardView)view;
            pointImage=(ImageView)view.findViewById(R.id.point_image);
            pointName=(TextView)view.findViewById(R.id.point_name);
        }
    }

    public PointAdapter(List<Point> pointList){
        mPointList=pointList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        if(mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.point_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                Point point=mPointList.get(position);
                Intent intent=new Intent(view.getContext(),InfochangeActivity.class);
                intent.putExtra("coord_Data",point.getJsonObject().toString());
                ActivityCompat.startActivityForResult((Activity) view.getContext(),intent,2,null);
                // TODO 点击卡片跳转至对应的点之记页面
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        Point point=mPointList.get(position);
        holder.pointName.setText(point.getPointName());
        Glide.with(mContext).load(point.getImageUri()).into(holder.pointImage);
    }

    @Override
    public int getItemCount(){
        return mPointList.size();
    }
}
