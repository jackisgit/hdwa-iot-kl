package com.wanda.epc.device;

/**
 * @program: DAPC
 * @description: 海康客流数据响应对象
 * @author: LianYanFei
 * @create: 2022-08-22 16:23
 **/

public class HaiKangPassengerFlow {


    /**
     * 场内实时人数
     */
    Integer currentNum = 0;
    /**
     * 当日累计人数
     */
    Integer todayNum = 0;
    /**
     * 步行街实时人数
     */
    Integer streetCurrentNum = 0;
    /**
     * 步行街当日累计人数
     */
    Integer streetTodayNum = 0;
    /**
     * 前端设备故障点个数
     */
    Integer deviceFailureNum = 0;



    public Integer getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(Integer currentNum) {
        this.currentNum = currentNum;
    }

    public Integer getTodayNum() {
        return todayNum;
    }

    public void setTodayNum(Integer todayNum) {
        this.todayNum = todayNum;
    }

    public Integer getStreetCurrentNum() {
        return streetCurrentNum;
    }

    public void setStreetCurrentNum(Integer streetCurrentNum) {
        this.streetCurrentNum = streetCurrentNum;
    }

    public Integer getStreetTodayNum() {
        return streetTodayNum;
    }

    public void setStreetTodayNum(Integer streetTodayNum) {
        this.streetTodayNum = streetTodayNum;
    }

    public Integer getDeviceFailureNum() {
        return deviceFailureNum;
    }

    public void setDeviceFailureNum(Integer deviceFailureNum) {
        this.deviceFailureNum = deviceFailureNum;
    }
}
