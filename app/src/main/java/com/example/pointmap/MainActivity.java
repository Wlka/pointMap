package com.example.pointmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.simple.spiderman.SpiderMan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private int overLayCount=0; //overlay数量
    private LatLng lastPoint=new LatLng(0.0,0.0);
    private boolean isConnect=false;
    String fileName="untitled";
    String excelFilePath="";

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
                        //TODO navigate to the point
                        Toast.makeText(getApplicationContext(),"test",Toast.LENGTH_SHORT).show();
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
                        startActivityForResult(intent,1);
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
        //TODO 打开文件列表让用户选择打开指定文件
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
        //TODO 导出到excel
        if(pointInfoList.size()==0){
            Toast.makeText(getApplicationContext(),"没有填写任何信息，无法导出excel文件",Toast.LENGTH_SHORT).show();
            return;
        }
        ExcelUtils excelUtils=new ExcelUtils(excelFilePath);
        for(int i=0;i<pointInfoList.size();++i){
            JsonObject jsonObject=new JsonParser().parse(pointInfoList.get(i)).getAsJsonObject();
            String sheetName="";
            if(TextUtils.isEmpty(jsonObject.get("pointName").getAsString())){
                sheetName="null_"+i;
            }else{
                sheetName=jsonObject.get("pointName").getAsString();
            }
            excelUtils.addSheet(sheetName,i);
            excelUtils.addText(i,0,0,"jfofhefehf");
        }
        excelUtils.close();
        Toast.makeText(getApplicationContext(),"成功导出excel文件",Toast.LENGTH_SHORT).show();
    }

    private void savePointInfo(){
        FileOutputStream fileOutputStream= null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(pointInfoList.toString().getBytes());
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
                    startActivityForResult(intent, 1);
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
                polyline.remove();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requsetCode,int resultCode,Intent data) {
        super.onActivityResult(requsetCode, resultCode, data);
        switch (requsetCode) {
            case 1:
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
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