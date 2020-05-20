package com.example.pointmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.simple.spiderman.SpiderMan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.write.WriteException;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBar actionBar;
    private NavigationView navigationView;
    private TextView textView_phoneNumber;
    private TextView textView_userName;
    private MapView mapView;
    private BaiduMap baiduMap;
    private ImageView imageView;
    private FloatingActionButton floatingActionButton;
    public LocationClient mLocationClient;
    private boolean isFirstLocate = true; //首次获取定位信息标识
    private MyOrientationListener myOrientationListener;
    private float mLastX;   //当前手机方向
    private LatLng currentLocation; //用户当前位置
    private Button toCurrPos;   //将地图中心移动到当前位置
    private ProgressDialog progressDialog;  //提示框
    private BaiduMap.OnMarkerClickListener pointMenuMarkerClickListener;    //Marker信息窗口点击监听事件
    private BaiduMap.OnMarkerClickListener pointConnectClickListener;    //Marker连接点击监听事件
    private List<String> pointInfoList=new ArrayList<>();
    private LatLng lastPoint=new LatLng(0.0,0.0);
    private int overLayCount=0; //overlay数量
    private List<List<LatLng>> connectPointsList=new ArrayList<>();
    private boolean isConnect=false;
    String fileName="untitled";
    String excelFilePath="";
    public static final int INFOCHANGE=1;
    public static final int CHOOSE_OPENFILE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpiderMan.init(this);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        actionBar = getSupportActionBar();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        progressDialog = new ProgressDialog(MainActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toCurrPos = (Button) findViewById(R.id.toCurrPos);
        imageView=(ImageView)findViewById(R.id.image);
        floatingActionButton=(FloatingActionButton)findViewById(R.id.floationActionButton);
        initStorageDir();   //初始化应用文件夹
        initMap();  //初始化地图
        initMyOrien();  //初始化方向传感器
        getLocationPermission();    //获取定位权限
        //点击按钮回到当前位置
        toCurrPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
                baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
            }
        });

        pointMenuMarkerClickListener =new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                View view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.infowindow,null);
                TextView textView_navigation=(TextView)view.findViewById(R.id.textView_navigation);
                TextView textView_infoChange=(TextView)view.findViewById(R.id.textView_infoChange);
                TextView textView_deleteInfo=(TextView)view.findViewById(R.id.textView_deleteInfo);
                TextView textView_closeWindow=(TextView)view.findViewById(R.id.textView_closeWindow);
                InfoWindow infoWindow=new InfoWindow(view,marker.getPosition(),-170);
                baiduMap.showInfoWindow(infoWindow);
                textView_navigation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final WalkNaviLaunchParam param=new WalkNaviLaunchParam().stPt(currentLocation).endPt(marker.getPosition());
                        WalkNavigateHelper.getInstance().initNaviEngine(MainActivity.this, new IWEngineInitListener() {
                            @Override
                            public void engineInitSuccess() {
                                WalkNavigateHelper.getInstance().routePlanWithParams(param, new IWRoutePlanListener() {
                                    @Override
                                    public void onRoutePlanStart() {
                                    }

                                    @Override
                                    public void onRoutePlanSuccess() {
                                        Toast.makeText(getApplicationContext(),"算路成功，正在跳转",Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(getApplicationContext(),WNaviGuideActivity.class);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onRoutePlanFail(WalkRoutePlanError walkRoutePlanError) {
                                        Toast.makeText(getApplicationContext(),"算路失败",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Toast.makeText(getApplicationContext(),"导航引擎初始化成功",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void engineInitFail() {
                                Toast.makeText(getApplicationContext(),"导航引擎初始化失败",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                textView_infoChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(MainActivity.this, InfochangeActivity.class);
                        if(pointInfoList.size()>0){
                            int i=0;
                            for(i=0;i<pointInfoList.size();++i){
                                if(pointInfoList.get(i).contains(marker.getPosition().toString())){
                                    intent.putExtra("coord_Data",pointInfoList.get(i));
                                    break;
                                }
                            }
                            if(i==pointInfoList.size())
                                intent.putExtra("coord_Data",marker.getPosition().toString());
                        }else{
                            intent.putExtra("coord_Data",marker.getPosition().toString());
                        }
                        startActivityForResult(intent,INFOCHANGE);
                    }
                });
                textView_deleteInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        marker.remove();
                        overLayCount--;
                        baiduMap.hideInfoWindow();
                        for(int i=0;i<pointInfoList.size();++i) {
                            if (pointInfoList.get(i).contains(marker.getPosition().toString())) {
                                pointInfoList.remove(i);
                                break;
                            }
                        }
                    }
                });
                textView_closeWindow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        baiduMap.hideInfoWindow();
                    }
                });
                return true;
            }
        };

        pointConnectClickListener=new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(lastPoint.toString().equals(new LatLng(0.0,0.0).toString())){
                    lastPoint=marker.getPosition();
                }else{
                    List<LatLng> points=new ArrayList<>();
                    points.add(lastPoint);
                    points.add(marker.getPosition());
                    drawPloyLine(points);
                    lastPoint=marker.getPosition();
                }
                return true;
            }
        };

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_new:
                        if(overLayCount>0) {
                            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                            builder.setMessage("是否保存当前文件");
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    saveFile();
                                    newFile();
                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    newFile();
                                }
                            });
                            builder.show();
                        }else{
                            newFile();
                        }
                        break;
                    case R.id.nav_open:
                        openFile();
                        break;
                    case R.id.nav_save:
                        if(overLayCount>0){
                            saveFile();
                        }else{
                            Toast.makeText(getApplicationContext(),"当前数据为空",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_export:
                        if(overLayCount>0){
                            exportFile();
                        }else{
                            Toast.makeText(getApplicationContext(),"当前数据为空",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_about:
                        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                        dialog.setTitle("关于 & 反馈");
                        dialog.setMessage("软件如在使用过程发现任何问题或者需要完善那些内容，可以将错误截图或修改意见发送至邮箱：1464101258@qq.com");
                        dialog.setPositiveButton("确定",null);
                        dialog.show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void newFile(){
        final EditText editText=new EditText(MainActivity.this);
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setTitle("设置新建文件名").setView(editText);
        editText.setTextColor(Color.BLACK);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        final AlertDialog dialog=builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(editText.getText())) {
                    baiduMap.clear();
                    overLayCount = 0;
                    pointInfoList.clear();
                    File documentStorageDir = new File(Environment.getExternalStoragePublicDirectory("").getAbsoluteFile() + "/PointMap",
                            "Documents");
                    if (!documentStorageDir.exists()) {
                        try {
                            documentStorageDir.mkdirs();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"Documents文件夹创建失败", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            return;
                        }
                    }
                    File file = new File(documentStorageDir, editText.getText().toString() + ".pm");
                    dialog.cancel();
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        fileName=file.getAbsolutePath();
                        excelFilePath=fileName.replace(".pm",".xlsx");
                        Toast.makeText(getApplicationContext(),"文件新建成功",Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "文件新建失败", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        return;
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "文件名不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void saveFile(){
        if(TextUtils.equals(fileName,"untitled")){
            final EditText editText=new EditText(MainActivity.this);
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_DayNight_Dialog_Alert);
            builder.setTitle("设置保存文件名").setView(editText);
            editText.setTextColor(Color.BLACK);
            builder.setPositiveButton("确定",null);
            builder.setNegativeButton("取消",null);
            final AlertDialog dialog=builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(editText.getText())) {
                        File documentStorageDir = new File(Environment.getExternalStoragePublicDirectory("").getAbsoluteFile() + "/PointMap",
                                "Documents");
                        if (!documentStorageDir.exists()) {
                            try {
                                documentStorageDir.mkdirs();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(),"Documents文件夹创建失败", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                return;
                            }
                        }
                        File file = new File(documentStorageDir, editText.getText().toString() + ".pm");
                        dialog.cancel();
                        try {
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            fileName=file.getAbsolutePath();
                            excelFilePath=fileName.replace(".pm",".xlsx");
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "文件保存失败", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            return;
                        }
                        savePointInfo();
                    }else{
                        Toast.makeText(getApplicationContext(), "文件名不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            savePointInfo();
        }
    }

    public void openFile(){
        File documentStorageDir = new File(Environment.getExternalStoragePublicDirectory("").getAbsoluteFile() + "/PointMap",
                "Documents");
        if (!documentStorageDir.exists()) {
            try {
                documentStorageDir.mkdirs();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"Documents文件夹创建失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        List<String>pmFileList=new ArrayList<>();
        for(File pmFile:documentStorageDir.listFiles()){
            if(pmFile.getAbsolutePath().endsWith(".pm")) {
                pmFileList.add(pmFile.getName());
            }
        }
        if(pmFileList.size()!=0){
            Intent intent=new Intent(getApplicationContext(),FileOpenActivity.class);
            intent.putExtra("pmFiles",documentStorageDir.getAbsolutePath()+":"+pmFileList.toString());
            startActivityForResult(intent,CHOOSE_OPENFILE);
        }else{
            Toast.makeText(getApplicationContext(),"当前设备无数据文件",Toast.LENGTH_SHORT).show();
        }
    }

    public void exportFile(){
        if(TextUtils.equals(fileName,"untitled")) {
            final EditText editText=new EditText(MainActivity.this);
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_DayNight_Dialog_Alert);
            builder.setTitle("设置保存文件名").setView(editText);
            editText.setTextColor(Color.BLACK);
            builder.setPositiveButton("确定",null);
            builder.setNegativeButton("取消",null);
            final AlertDialog dialog=builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(editText.getText())) {
                        File documentStorageDir = new File(Environment.getExternalStoragePublicDirectory("").getAbsoluteFile() + "/PointMap",
                                "Documents");
                        if (!documentStorageDir.exists()) {
                            try {
                                documentStorageDir.mkdirs();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(),"Documents文件夹创建失败", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                return;
                            }
                        }
                        File file = new File(documentStorageDir, editText.getText().toString() + ".pm");
                        dialog.cancel();
                        try {
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            fileName=file.getAbsolutePath();
                            excelFilePath=fileName.replace(".pm",".xlsx");
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "文件保存失败", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            return;
                        }
                        export2Excel();
                        savePointInfo();
                    }else{
                        Toast.makeText(getApplicationContext(), "文件名不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            export2Excel();
            savePointInfo();
        }
    }

    private void export2Excel(){
        if(pointInfoList.size()==0){
            Toast.makeText(getApplicationContext(),"没有填写任何信息，无法导出excel文件",Toast.LENGTH_SHORT).show();
            return;
        }
        //TODO 导出excel样式修改
        ExcelUtils excelUtils=new ExcelUtils(getContentResolver(),excelFilePath);
        for(int i=0;i<pointInfoList.size();++i){
            JsonObject jsonObject=new JsonParser().parse(pointInfoList.get(i)).getAsJsonObject();
            String sheetName="";
            if(TextUtils.isEmpty(jsonObject.get("pointName").getAsString())){
                sheetName="null_"+i;
            }else{
                sheetName=jsonObject.get("pointName").getAsString();
            }
            excelUtils.addSheet(sheetName,i);

            excelUtils.addText(i,0,0,0,0,"点号");
            excelUtils.addText(i,1,0,2,0,jsonObject.get("pointName").getAsString());
            excelUtils.addText(i,5,0,0,0,"级别");
            excelUtils.addText(i,6,0,2,0,jsonObject.get("pointLevel").getAsString());

            excelUtils.addText(i,0,1,0,0,"地类土质");
            excelUtils.addText(i,1,1,2,0,jsonObject.get("pointSoil").getAsString());
            excelUtils.addText(i,5,1,0,0,"标石类型");
            excelUtils.addText(i,6,1,2,0,jsonObject.get("markerType").getAsString());

            excelUtils.addText(i,0,2,0,0,"选点人");
            excelUtils.addText(i,1,2,2,0,jsonObject.get("selectionPerson").getAsString());
            excelUtils.addText(i,5,2,0,0,"选点日期");
            excelUtils.addText(i,6,2,2,0,jsonObject.get("selectionDate").getAsString());

            excelUtils.addText(i,0,3,0,0,"埋石单位");
            excelUtils.addText(i,1,3,2,0,jsonObject.get("buriedUnit").getAsString());
            excelUtils.addText(i,5,3,0,0,"埋石日期");
            excelUtils.addText(i,6,3,2,0,jsonObject.get("buriedDate").getAsString());

            excelUtils.addText(i,0,4,0,0,"所在位置");
            excelUtils.addText(i,1,4,2,0,jsonObject.get("pointPosition").getAsString());

            excelUtils.addText(i,0,5,0,5,"实地图片");
            excelUtils.addImage(i,1,5,5,5, jsonObject.get("imageUri").getAsString());
        }
        excelUtils.close();
        Toast.makeText(getApplicationContext(),"成功导出excel文件",Toast.LENGTH_SHORT).show();
    }

    private void savePointInfo(){
        FileOutputStream fileOutputStream= null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            JsonObject jsonObject=new JsonObject();
            jsonObject.addProperty("pointList",pointInfoList.toString());
            String connectInfo="";
            for(int i=0;i<connectPointsList.size();++i){
                connectInfo+=connectPointsList.get(i).toString().replace("latitude","")
                        .replace("longitude","")
                        .replace(":","")
                        .replace("[","").replace("]","")
                        .replace(" ","")+";";
            }
            jsonObject.addProperty("connectPointsList",connectInfo);
            fileOutputStream.write(jsonObject.toString().getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(getApplicationContext(),"文件保存成功",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"自动备份失败",Toast.LENGTH_SHORT).show();
            SpiderMan.show(e);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
//            case R.id.markerConvert:
//                // TODO 点位图标与文字相互转换
//                if(overLayCount!=0){
//                    if(item.getTitle()=="文字"){
//                        item.setTitle("图标");
//                    }else{
//                        item.setTitle("文字");
//                    }
//                    Toast.makeText(getApplicationContext(),"test",Toast.LENGTH_SHORT);
//                }else{
//                    Toast.makeText(getApplicationContext(),"当前无点位",Toast.LENGTH_SHORT);
//                }
//                break;
            case R.id.pointConnect:
                if(isConnect==false){
                    isConnect=true;
                    baiduMap.removeMarkerClickListener(pointMenuMarkerClickListener);
                    baiduMap.setOnMarkerClickListener(pointConnectClickListener);
                    item.setTitle("结束");
                }else{
                    isConnect=false;
                    baiduMap.removeMarkerClickListener(pointConnectClickListener);
                    baiduMap.setOnMarkerClickListener(pointMenuMarkerClickListener);
                    item.setTitle("连接");
                }
                break;
            case R.id.pointInfo:
                if(overLayCount>0) {
                    Intent intent = new Intent(this, PointinfoActivity.class);
                    intent.putExtra("point_Data", pointInfoList.toString());
                    startActivityForResult(intent, INFOCHANGE);
                }else{
                    Toast.makeText(getApplicationContext(),"目前没有标记任何地点",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.choosePoint:
                baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
                baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
                baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
                    @Override
                    public void onMapStatusChangeStart(MapStatus mapStatus) {
                    }
                    @Override
                    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
                    }
                    @Override
                    public void onMapStatusChange(MapStatus mapStatus) {
                        imageView.setVisibility(View.VISIBLE);
                        floatingActionButton.setVisibility(View.GONE);
                    }
                    @Override
                    public void onMapStatusChangeFinish(final MapStatus mapStatus) {
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                drawPointMarker(mapStatus.target);
                                overLayCount++;
                                baiduMap.setOnMapStatusChangeListener(null);
                                floatingActionButton.setVisibility(View.GONE);
                                imageView.setVisibility(View.GONE);
                            }
                        });
                    }
                });
                baiduMap.setOnMarkerClickListener(pointMenuMarkerClickListener);
                break;
            default:
                break;
        }
        return true;
    }

    //初始化应用文件夹
    private void initStorageDir(){
        File fileStorageDir = new File(Environment.getExternalStoragePublicDirectory("").getAbsoluteFile(), "PointMap");
        if (!fileStorageDir.exists()) {
            try{
                fileStorageDir.mkdirs();
                File imageStorageDir = new File(fileStorageDir, "Images");
                File documentStorageDir = new File(fileStorageDir, "Documents");
                imageStorageDir.mkdirs();
                documentStorageDir.mkdirs();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "初始化应用文件夹失败:\n"+e.toString(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    //地图相关设置
    private void initMap() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                connectPointsList.remove(polyline.getPoints());
                polyline.remove();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requsetCode,int resultCode,Intent data) {
        super.onActivityResult(requsetCode, resultCode, data);
        switch (requsetCode) {
            case INFOCHANGE:
                if (resultCode == RESULT_OK) {
                    String data_return=data.getStringExtra("data_return");
                    for(int i=0;i<pointInfoList.size();++i){
                        if(new JsonParser().parse(pointInfoList.get(i)).getAsJsonObject().get("coord_Data").getAsString().equals(
                                new JsonParser().parse(data_return).getAsJsonObject().get("coord_Data").getAsString())){
                            pointInfoList.remove(i);
                        }
                    }
                    pointInfoList.add(data_return);
                    Toast.makeText(getApplicationContext(),
                            new JsonParser().parse(data_return).getAsJsonObject().get("pointName").getAsString()+"修改成功",Toast.LENGTH_SHORT).show();
                }
                break;
            case CHOOSE_OPENFILE:
                if(resultCode==RESULT_OK){
                    String data_return=data.getStringExtra("data_return");
                    File pmFile=new File(data_return);
                    try {
                        InputStream inputStream = new FileInputStream(pmFile);
                        fileName=pmFile.getAbsolutePath();
                        excelFilePath=fileName.replace(".pm",".xlsx");
                        if(inputStream!=null){
                            InputStreamReader inputStreamReader=new InputStreamReader(inputStream,"UTF-8");
                            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                            String line="";
                            String content="";
                            while((line=bufferedReader.readLine())!=null){
                                content+=line;
                            }
                            inputStream.close();
                            JsonObject jsonObject=new JsonParser().parse(content).getAsJsonObject();
                            JsonArray jsonArray=new JsonParser().parse(jsonObject.get("pointList").getAsString()).getAsJsonArray();
                            baiduMap.clear();
                            pointInfoList.clear();
                            connectPointsList.clear();
                            fileName=data_return;
                            excelFilePath=fileName.replace(".pm",".xlsx");
                            overLayCount=0;
                            for(int i=0;i<jsonArray.size();++i){
                                String[] tmp=jsonArray.get(i).getAsJsonObject().get("coord_Data").getAsString()
                                        .replace("latitude:","")
                                        .replace("longitude:","")
                                        .replace(" ","")
                                        .split(",");
                                drawPointMarker(new LatLng(Double.parseDouble(tmp[0]),Double.parseDouble(tmp[1])));
                                overLayCount++;
                                pointInfoList.add(jsonArray.get(i).toString());
                            }
                            if(!TextUtils.isEmpty(jsonObject.get("connectPointsList").getAsString())) {
                                String[] tmp=jsonObject.get("connectPointsList").getAsString().split(";");
                                for (int i = 0; i < tmp.length; ++i) {
                                    String[] t = tmp[i].split(",");
                                    List<LatLng> tmpList = new ArrayList<>();
                                    tmpList.add(new LatLng(Double.parseDouble(t[0]), Double.parseDouble(t[1])));
                                    tmpList.add(new LatLng(Double.parseDouble(t[2]), Double.parseDouble(t[3])));
                                    drawPloyLine(tmpList);
                                    connectPointsList.add(tmpList);
                                }
                            }
                            isConnect=false;
                            baiduMap.setOnMarkerClickListener(pointMenuMarkerClickListener);
                        }
                    } catch (Exception e) {
                        SpiderMan.show(e);
                    }
                }
                break;
            default:
                break;
        }
    }

    //单点标记
    public Overlay drawPointMarker(LatLng point) {
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.pointmarker);
        OverlayOptions mPointOption = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .animateType(MarkerOptions.MarkerAnimateType.grow)
                .flat(true);
        return baiduMap.addOverlay(mPointOption);
    }

    //绘制线
    public Overlay drawPloyLine(List<LatLng> points){
        OverlayOptions mPloyLineOptions = new PolylineOptions()
                .width(5)
                .color(Color.BLUE)
                .points(points);
        connectPointsList.add(points);
        return baiduMap.addOverlay(mPloyLineOptions);
    }

    //绘制文字
    public Overlay drawText(LatLng point,String text){
        OverlayOptions mTextOptions = new TextOptions()
                .text(text)
                .fontSize(24)
                .fontColor(Color.BLUE)
                .position(point)
                .align(10,10);
        return baiduMap.addOverlay(mTextOptions);
    }

    //方向传感器类初始化
    private void initMyOrien() {
        myOrientationListener = new MyOrientationListener(this);
        myOrientationListener.start();
        myOrientationListener.setmSensorManager(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mLastX = x;
            }
        });
    }

    //开始定位
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    //设置地图相关选项
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setEnableSimulateGps(true);
        option.setNeedDeviceDirect(true);
        option.setLocationNotify(true);
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);
    }

    //监听位置变化
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }
    }

    //根据实际位置显示地图
    private void navigateTo(BDLocation location) {
        MyLocationConfiguration configuration = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null);
        baiduMap.setMyLocationConfiguration(configuration);
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (isFirstLocate) {
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(currentLocation);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder
                .accuracy(location.getRadius()).direction(mLastX).build();
        baiduMap.setMyLocationData(locationData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        myOrientationListener.stop();
        mapView.onDestroy();
        baiduMap.clear();
        baiduMap.setMyLocationEnabled(false);
    }

    //权限获取
    private void getLocationPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
//                != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.READ_PHONE_STATE);
//        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
}
