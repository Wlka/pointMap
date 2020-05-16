package com.example.pointmap;

import android.widget.Toast;

import com.simple.spiderman.SpiderMan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    //    private WritableSheet writableSheet;
    private WritableWorkbook writableWorkbook;

    public ExcelUtils(String filePath) {
        try {
            OutputStream outputStream=new FileOutputStream(filePath);
            writableWorkbook= Workbook.createWorkbook(outputStream);
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

    public void addText(int sheetIndex,int row,int col,String text){
        WritableSheet writableSheet=writableWorkbook.getSheet(sheetIndex);
        if(writableSheet==null){
            return;
        }
        try {
            writableSheet.addCell(new Label(row,col,text));
        } catch (WriteException e) {
            SpiderMan.show(e);
        }
    }

    public void addImage(int sheetIndex,int row,int col,String imagePath){
        WritableSheet writableSheet=writableWorkbook.getSheet(sheetIndex);
        if(writableSheet==null){
            return;
        }
        try {
            writableSheet.addImage(new WritableImage(row,col,1,1,new File(imagePath)));
        } catch (Exception e) {
            SpiderMan.show(e);
        }
    }
}
