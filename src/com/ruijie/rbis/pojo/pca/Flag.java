package com.ruijie.rbis.pojo.pca;

import java.util.Date;
/**
 * Created by OA on 2014/12/23.
 */
public class Flag {
    private int buildingId;
    private Date apTime;
    private double eigenvalue;
    private double allNumWeight;
    private double enterNumWeight;
    private double stayNumWeight;
    private double AvgEnterTimeWeight;
    private double AvgStayTimeWeight;
    private double newNumWeight;
    private double oldNumWeight;

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setApTime(Date apTime) {
        this.apTime = apTime;
    }

    public Date getApTime() {
        return apTime;
    }

    public void setEigenvalue(double eigenvalue) {
        this.eigenvalue = eigenvalue;
    }

    public double getEigenvalue() {
        return eigenvalue;
    }

    public void setAllNumWeight(double allNumWeight) {
        this.allNumWeight = allNumWeight;
    }

    public double getAllNumWeight() {
        return allNumWeight;
    }

    public void setEnterNumWeight(double enterNumWeight) {
        this.enterNumWeight = enterNumWeight;
    }

    public double getEnterNumWeight() {
        return enterNumWeight;
    }

    public void setStayNumWeight(double stayNumWeight) {
        this.stayNumWeight = stayNumWeight;
    }

    public double getStayNumWeight() {
        return stayNumWeight;
    }

    public void setAvgEnterTimeWeight(double avgEnterTimeWeight) {
        AvgEnterTimeWeight = avgEnterTimeWeight;
    }

    public double getAvgEnterTimeWeight() {
        return AvgEnterTimeWeight;
    }

    public void setAvgStayTimeWeight(double avgStayTimeWeight) {
        AvgStayTimeWeight = avgStayTimeWeight;
    }

    public double getAvgStayTimeWeight() {
        return AvgStayTimeWeight;
    }

    public void setNewNumWeight(double newNumWeight) {
        this.newNumWeight = newNumWeight;
    }

    public double getNewNumWeight() {
        return newNumWeight;
    }

    public void setOldNumWeight(double oldNumWeight) {
        this.oldNumWeight = oldNumWeight;
    }

    public double getOldNumWeight() {
        return oldNumWeight;
    }
}
