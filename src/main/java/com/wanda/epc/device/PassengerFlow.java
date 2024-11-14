package com.wanda.epc.device;

import lombok.Data;

/**
 * @program: DAPC
 * @description: 海康客流数据响应对象
 * @author: LianYanFei
 * @create: 2022-08-22 16:23
 **/
@Data
public class PassengerFlow {

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


}