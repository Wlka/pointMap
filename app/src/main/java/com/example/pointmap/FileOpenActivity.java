package com.example.pointmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
            fileList.add(new File(fileName,R.drawable.ic_addphoto));
        }
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view_fileList);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        FileListAdapter adapter=new FileListAdapter(fileList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
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
