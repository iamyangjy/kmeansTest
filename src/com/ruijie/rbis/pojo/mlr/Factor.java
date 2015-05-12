package com.ruijie.rbis.pojo.mlr;

/**
 * Created by OA on 2014/12/23.
 */
public class Factor {
    private int buildingId;
    private int allNum;
    private int enterNum;
    private int stayNum;
    private double avgEnterTime;
    private double avgStayTime;
    private int oldNum;
    private int newNum;
    private double sale;

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setAllNum(int allNum) {
        this.allNum = allNum;
    }

    public int getAllNum() {
        return allNum;
    }

    public void setEnterNum(int enterNum) {
        this.enterNum = enterNum;
    }

    public int getEnterNum() {
        return enterNum;
    }

    public void setStayNum(int stayNum) {
        this.stayNum = stayNum;
    }

    public int getStayNum() {
        return stayNum;
    }

    public void setAvgEnterTime(double avgEnterTime) {
        this.avgEnterTime = avgEnterTime;
    }

    public double getAvgEnterTime() {
        return avgEnterTime;
    }

    public void setAvgStayTime(double avgStayTime) {
        this.avgStayTime = avgStayTime;
    }

    public double getAvgStayTime() {
        return avgStayTime;
    }

    public void setNewNum(int newNum) {
        this.newNum = newNum;
    }

    public int getNewNum() {
        return newNum;
    }

    public void setOldNum(int oldNum) {
        this.oldNum = oldNum;
    }

    public int getOldNum() {
        return oldNum;
    }

    public void setSale(double sale) {
        this.sale = sale;
    }

    public double getSale() {
        return sale;
    }
}

