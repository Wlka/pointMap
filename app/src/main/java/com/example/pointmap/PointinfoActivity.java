package com.example.pointmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PointinfoActivity extends AppCompatActivity {

    private List<Point> pointList=new ArrayList<>();
    private PointAdapter adapter;
    private JsonArray jsonArray;
    private static final int INFOCHANGE=1;
    private int positionClicked=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointinfo);

        final Intent intent=getIntent();
        jsonArray=new JsonParser().parse(intent.getStringExtra("point_Data")).getAsJsonArray();

        initPoints();
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view_pointInfoList);
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new PointAdapter(pointList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new PointAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(View v, int position) {
                Intent intent=new Intent(getApplicationContext(), InfochangeActivity.class);
                intent.putExtra("coord_Data",jsonArray.get(position).toString());
                positionClicked=position;
                startActivityForResult(intent,INFOCHANGE);
//                finish();
            }
        });
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private void initPoints(){
        pointList.clear();
        for(int i=0;i<jsonArray.size();++i){
            JsonObject jsonObject=jsonArray.get(i).getAsJsonObject();
            if(!jsonObject.get("pointName").getAsString().equals("")){
                pointList.add(new Point(jsonObject));
            }
        }
    }

    @Override
    protected void onActivityResult(int requsetCode,int resultCode,Intent data) {
        super.onActivityResult(requsetCode, resultCode, data);
        switch (requsetCode) {
            case INFOCHANGE:
                if (resultCode == RESULT_OK) {
                    Intent intent=new Intent();
                    intent.putExtra("data_return",data.getStringExtra("data_return"));
                    setResult(RESULT_OK,intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
