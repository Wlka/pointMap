package com.example.pointmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileOpenActivity extends AppCompatActivity {

    private List<File> fileList=new ArrayList<>();
    private List<String> pmFilesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_open);

        final Intent intent=getIntent();
        final String[] intentData=intent.getStringExtra("pmFiles").split(":");

        pmFilesList=Arrays.asList(intentData[1].replace("[","").replace("]","")
                .replace(" ","").split(","));
        for(String fileName:pmFilesList){
            fileList.add(new File(fileName,R.drawable.file));
        }
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view_fileList);
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        FileAdapter adapter=new FileAdapter(fileList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent=new Intent();
                intent.putExtra("data_return",intentData[0]+"/"+pmFilesList.get(position));
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }
}
