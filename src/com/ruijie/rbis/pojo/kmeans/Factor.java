package com.ruijie.rbis.pojo.kmeans;

/**
 * Created by OA on 2015/1/8.
 */
public class Factor {
    //mac
    private String mac;
    //商城访问次数
    private double visitFrequency;
    //店铺访问次数
    private double storeVisitFrequency;
    //商城驻留时长
    private double stayTime;
    //店铺驻留时长
    private double storeStayTime;
    //上月访问次数
    private double lastVf;
    //手机类型
    private String deviceModel;
    //wifi使用时长
    private double wifiDuration;
    //簇的定义
    private int clusterId;
    //时间类型
    private int timeType;

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getMac() {
        return mac;
    }

    public void setVisitFrequency(double visitFrequency) {
        this.visitFrequency = visitFrequency;
    }

    public double getVisitFrequency() {
        return visitFrequency;
    }

    public void setStayTime(double stayTime) {
        this.stayTime = stayTime;
    }

    public double getStayTime() {
        return stayTime;
    }

    public void setLastVf(double lastVf) {
        this.lastVf = lastVf;
    }

    public double getLastVf() {
        return lastVf;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }

    public int getTimeType() {
        return timeType;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setStoreVisitFrequency(double storeVisitFrequency) {
        this.storeVisitFrequency = storeVisitFrequency;
    }

    public double getStoreVisitFrequency() {
        return storeVisitFrequency;
    }

    public void setStoreStayTime(double storeStayTime) {
        this.storeStayTime = storeStayTime;
    }

    public double getStoreStayTime() {
        return storeStayTime;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setWifiDuration(double wifiDuration) {
        this.wifiDuration = wifiDuration;
    }

    public double getWifiDuration() {
        return wifiDuration;
    }
}
