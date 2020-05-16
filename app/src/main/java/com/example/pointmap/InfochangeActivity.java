package com.example.pointmap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InfochangeActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO=1;
    public static final int CHOOSE_PHOTO=2;
    private File imageStorageDir;
    private TextView textView_projectName;
    private TextView textView_pointName;
    private TextView textView_pointLevel;
    private TextView textView_pointSoil;
    private TextView textView_markerType;
    private TextView textView_selectionPerson;
    private TextView textView_selectionDate;
    private TextView textView_buriedUnit;
    private TextView textView_buriedDate;
    private TextView textView_pointPosition;
    private ImageView photoViewer;
    private Button btn_ok;
    private Uri imageUri=null;
    private String coord_Data;
    private JsonObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infochange);
        textView_projectName=(TextView)findViewById(R.id.textView_projectName);
        textView_pointName=(TextView)findViewById(R.id.textView_pointName);
        textView_pointLevel=(TextView)findViewById(R.id.textView_pointLevel);
        textView_pointSoil=(TextView)findViewById(R.id.textView_pointSoil);
        textView_markerType=(TextView)findViewById(R.id.textView_markerType);
        textView_selectionPerson=(TextView)findViewById(R.id.textView_selectionPerson);
        textView_selectionDate=(TextView)findViewById(R.id.textView_selectionDate);
        textView_buriedUnit=(TextView)findViewById(R.id.textView_buriedUnit);
        textView_buriedDate=(TextView)findViewById(R.id.textView_buriedDate);
        textView_pointPosition=(TextView)findViewById(R.id.textView_pointPosition);
        photoViewer = (ImageView) findViewById(R.id.photoViewer);
        btn_ok=(Button)findViewById(R.id.btn_ok);

        final Intent intent=getIntent();
        coord_Data=intent.getStringExtra("coord_Data");
        try {
            jsonObject=new JsonParser().parse(coord_Data).getAsJsonObject();
            textView_projectName.setText(jsonObject.get("projectName").getAsString());
            textView_pointName.setText(jsonObject.get("pointName").getAsString());
            textView_pointLevel.setText(jsonObject.get("pointLevel").getAsString());
            textView_pointSoil.setText(jsonObject.get("pointSoil").getAsString());
            textView_markerType.setText(jsonObject.get("markerType").getAsString());
            textView_selectionPerson.setText(jsonObject.get("selectionPerson").getAsString());
            textView_selectionDate.setText(jsonObject.get("selectionDate").getAsString());
            textView_buriedUnit.setText(jsonObject.get("buriedUnit").getAsString());
            textView_buriedDate.setText(jsonObject.get("buriedDate").getAsString());
            textView_pointPosition.setText(jsonObject.get("pointPosition").getAsString());
            coord_Data=jsonObject.get("coord_Data").getAsString();
            imageUri=Uri.parse(jsonObject.get("imageUri").getAsString());
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                photoViewer.setImageBitmap(null);
                photoViewer.setBackground(new BitmapDrawable(bitmap));
            } catch (Exception e) {
                Bitmap bitmap=BitmapFactory.decodeFile(imageUri.getPath());
                if(bitmap!=null){
                    photoViewer.setImageBitmap(bitmap);
                }else{
                    Toast.makeText(getApplicationContext(), "未找到实地照片", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (JsonParseException e){
            jsonObject=new JsonObject();
        }
        //检查图片保存路径
        imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("").getAbsoluteFile()+"/PointMap",
                "Images");
        if (!imageStorageDir.exists()) {
            try{
                imageStorageDir.mkdirs();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Images文件夹创建失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        photoViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(photoViewer.getResources().equals(getResources().getDrawable(R.drawable.ic_addphoto))){
                    AlertDialog.Builder dialog=new AlertDialog.Builder(InfochangeActivity.this);
                    final String[] items={"拍照","从相册选取"};
                    dialog.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(items[i].equals("拍照")){
                                takePhoto();
                            }else if(items[i].equals("从相册选取")){
                                openAlbum();
                            }
                        }
                    });
                    dialog.show();
                }else{
                    AlertDialog.Builder dialog=new AlertDialog.Builder(InfochangeActivity.this);
                    final String[] items={"拍照","从相册选取"};
                    dialog.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(items[i].equals("拍照")){
                                takePhoto();
                            }else if(items[i].equals("从相册选取")){
                                openAlbum();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jsonObject.addProperty("projectName",textView_projectName.getText().toString());
                jsonObject.addProperty("pointName",textView_pointName.getText().toString());
                jsonObject.addProperty("pointLevel",textView_pointLevel.getText().toString());
                jsonObject.addProperty("pointSoil",textView_pointSoil.getText().toString());
                jsonObject.addProperty("markerType",textView_markerType.getText().toString());
                jsonObject.addProperty("selectionPerson",textView_selectionPerson.getText().toString());
                jsonObject.addProperty("selectionDate",textView_selectionDate.getText().toString());
                jsonObject.addProperty("buriedUnit",textView_buriedUnit.getText().toString());
                jsonObject.addProperty("buriedDate",textView_buriedDate.getText().toString());
                jsonObject.addProperty("pointPosition",textView_pointPosition.getText().toString());
                if(imageUri!=null){
                    jsonObject.addProperty("imageUri",imageUri.toString());
                }else{
                    jsonObject.addProperty("imageUri","");
                }
                jsonObject.addProperty("coord_Data",coord_Data);
                Intent intent=new Intent();
                intent.putExtra("data_return",jsonObject.toString());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    private void takePhoto(){
        String imageName = "IMG_" + new SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault()).format(new Date()) +".jpg";
        File outputImage = new File(imageStorageDir, imageName);
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(Build.VERSION.SDK_INT>=24){
            imageUri= FileProvider.getUriForFile(InfochangeActivity.this,"com.example.pointmap.fileprovider",outputImage);
        }else{
            imageUri=Uri.fromFile(outputImage);
        }
        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        photoViewer.setBackground(new BitmapDrawable(bitmap));
                        photoViewer.setImageBitmap(null);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "图片获取失败:\n"+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    if(Build.VERSION.SDK_INT>=19){
                        handleImageOnKitKat(data);  //4.4以上系统处理图片方法
                    }else{
                        handleImageBeforeKitKat(data);  //4.4以下系统处理图片方法
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];  //解析得到数字格式id
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.prviders.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://download/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //如果是content类型uri，则使用普通方式处理
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file，则直接获取图片路径
            imagePath=uri.getPath();
        }
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String path=null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        imageUri=Uri.parse(path);
//        Toast.makeText(getApplicationContext(),imageUri.toString(),Toast.LENGTH_SHORT).show();
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath!=null){
            Bitmap bitmap=BitmapFactory.decodeFile(imageUri.getPath());
            photoViewer.setBackground(null);
            photoViewer.setImageBitmap(bitmap);
        }else{
            Toast.makeText(getApplicationContext(),"加载图片失败",Toast.LENGTH_SHORT).show();
        }
    }
}
