package com.ruijie.rbis.pojo.mlr;

import java.util.Date;

/**
 * Created by OA on 2014/12/24.
 */
public class Weight {
    private int buildingId;
    private Date apTime;
    private double allNum = 0.0;
    private double enterNum = 0.0;
    private double stayNum = 0.0;
    private double avgEnterTime = 0.0;
    private double avgStayTime = 0.0;
    private double newNum = 0.0;
    private double oldNum = 0.0;
    private double cof = 0.0;
    private double rSquare = 0.0;

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

    public void setAllNum(double allNum) {
        this.allNum = allNum;
    }

    public double getAllNum() {
        return allNum;
    }

    public void setEnterNum(double enterNum) {
        this.enterNum = enterNum;
    }

    public double getEnterNum() {
        return enterNum;
    }

    public void setStayNum(double stayNum) {
        this.stayNum = stayNum;
    }

    public double getStayNum() {
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

    public void setNewNum(double newNum) {
        this.newNum = newNum;
    }

    public double getNewNum() {
        return newNum;
    }

    public void setOldNum(double oldNum) {
        this.oldNum = oldNum;
    }

    public double getOldNum() {
        return oldNum;
    }

    public void setCof(double cof) {
        this.cof = cof;
    }

    public double getCof() {
        return cof;
    }

    public void setrSquare(double rSquare) {
        this.rSquare = rSquare;
    }

    public double getrSquare() {
        return rSquare;
    }
}
