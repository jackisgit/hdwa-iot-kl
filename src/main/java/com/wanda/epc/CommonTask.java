package com.wanda.epc;

import com.wanda.epc.device.HaiKangPassengerFlowDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *@description 海康威视客流采集
 *@author LianYanFei
 *@date 2023/5/20
 */
@Configuration
@EnableScheduling
public class CommonTask {

    @Autowired
    private HaiKangPassengerFlowDevice device;

    @Scheduled(cron = "0/30 * * * * ?")
    public boolean processData() throws Exception {
        return device.processData();
    }

}
