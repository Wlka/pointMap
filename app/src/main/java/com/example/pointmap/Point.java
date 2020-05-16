package com.example.pointmap;

import android.net.Uri;

import com.google.gson.JsonObject;

public class Point {
    private String projectName;
    private String pointName;
    private String pointLevel;
    private String pointSoil;
    private String markerType;
    private String selectionPerson;
    private String selectionDate;
    private String buriedUnit;
    private String buriedDate;
    private String pointPosition;
    private Uri imageUri;
    private JsonObject jsonObject;

    public Point(JsonObject jsonObject){
        this.jsonObject=jsonObject;
        projectName=jsonObject.get("projectName").getAsString();
        pointName=jsonObject.get("pointName").getAsString();
        pointLevel=jsonObject.get("pointLevel").getAsString();
        pointSoil=jsonObject.get("pointSoil").getAsString();
        markerType=jsonObject.get("markerType").getAsString();
        selectionPerson=jsonObject.get("selectionPerson").getAsString();
        selectionDate=jsonObject.get("selectionDate").getAsString();
        buriedUnit=jsonObject.get("buriedUnit").getAsString();
        buriedDate=jsonObject.get("buriedDate").getAsString();
        pointPosition=jsonObject.get("pointPosition").getAsString();
        imageUri=Uri.parse(jsonObject.get("imageUri").getAsString());
    }

    public Point(String projectName, String pointName, String pointLevel, String pointSoil, String markerType, String selectionPerson, String selectionDate, String buriedUnit, String buriedDate, String pointPosition, String imageUri) {
        this.projectName = projectName;
        this.pointName = pointName;
        this.pointLevel = pointLevel;
        this.pointSoil = pointSoil;
        this.markerType = markerType;
        this.selectionPerson = selectionPerson;
        this.selectionDate = selectionDate;
        this.buriedUnit = buriedUnit;
        this.buriedDate = buriedDate;
        this.pointPosition = pointPosition;
        this.imageUri = Uri.parse(imageUri);
    }

    public String getProjectName() {
        return projectName;
    }

    public String getPointName() {
        return pointName;
    }

    public String getPointLevel() {
        return pointLevel;
    }

    public String getPointSoil() {
        return pointSoil;
    }

    public String getMarkerType() {
        return markerType;
    }

    public String getSelectionPerson() {
        return selectionPerson;
    }

    public String getSelectionDate() {
        return selectionDate;
    }

    public String getBuriedUnit() {
        return buriedUnit;
    }

    public String getBuriedDate() {
        return buriedDate;
    }

    public String getPointPosition() {
        return pointPosition;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }
}
