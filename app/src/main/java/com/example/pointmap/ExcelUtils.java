package com.example.pointmap;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.simple.spiderman.SpiderMan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    //    private WritableSheet writableSheet;
    private WritableWorkbook writableWorkbook;
    private ContentResolver contentResolver;

    public ExcelUtils(ContentResolver contentResolver, String filePath) {
        try {
            OutputStream outputStream=new FileOutputStream(filePath);
            writableWorkbook= Workbook.createWorkbook(outputStream);
            this.contentResolver=contentResolver;
        } catch (Exception e) {
            SpiderMan.show(e);
        }
    }

    public void addSheet(String sheetName, int index){
        try{
            writableWorkbook.createSheet(sheetName,index);
        }catch(Exception e){
            SpiderMan.show(e);
        }
    }

    public int sheetCnt(){
        return writableWorkbook.getNumberOfSheets();
    }

    public void close(){
        try {
            writableWorkbook.write();
            writableWorkbook.close();
        } catch (Exception e) {
            SpiderMan.show(e);
        }
    }

    public void addText(int sheetIndex,int col,int row,int colRange,int rowRange,String text){
        WritableSheet writableSheet=writableWorkbook.getSheet(sheetIndex);
        if(writableSheet==null || col<0 || row<0 || colRange<0 || rowRange<0){
            return;
        }
        try {
            writableSheet.mergeCells(col,row,col+colRange,row+rowRange);
            writableSheet.addCell(new Label(col,row,text));
        } catch (WriteException e) {
            SpiderMan.show(e);
        }
    }

    public void addImage(int sheetIndex,int col,int row,int colRange,int rowRange,String imagePath){
        WritableSheet writableSheet=writableWorkbook.getSheet(sheetIndex);
        if(writableSheet==null || imagePath==null || col<0 || row<0 || colRange<0 || rowRange<0){
            return;
        }
        try {
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("").getAbsoluteFile()+"/PointMap",
                    "Images");
            String imageName = "IMG_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.getDefault()).format(new Date()) +".png";
            File outputImage = new File(imageStorageDir, imageName);
            OutputStream outputStream=new FileOutputStream(outputImage);
            Uri imageUri=Uri.parse(imagePath);
            try {
                if (BitmapFactory.decodeFile(imageUri.getPath()).compress(Bitmap.CompressFormat.PNG, 80, outputStream)) {
                    outputStream.flush();
                }
            }catch(Exception ex){
                if(BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri)).compress(Bitmap.CompressFormat.PNG, 80, outputStream)) {
                    outputStream.flush();
                }
            }
            outputStream.close();
            writableSheet.mergeCells(col,row,col+colRange,row+rowRange);
            writableSheet.addImage(new WritableImage(col,row,1,1,outputImage));
        } catch (Exception e) {
            SpiderMan.show(e);
        }
    }
}
