package com.wanda.epc;

import com.wanda.epc.device.PassengerFlowDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class CommonTask {

    @Autowired
    private PassengerFlowDevice device;

    @Scheduled(cron = "${epc.cron}")
    public boolean processData() throws Exception {
        return device.processData();
    }

}