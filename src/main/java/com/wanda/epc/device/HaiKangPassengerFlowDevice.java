package com.wanda.epc.device;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.wanda.epc.param.DeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 *@description 海康威视客流采集
 *@author LianYanFei
 *@date 2023/5/20
 */
@Slf4j
@Service
public class HaiKangPassengerFlowDevice extends BaseDevice {

    @Autowired
    private JdbcTemplate postgreJdbcTemple;

    @Autowired
    private CommonDevice commonDevice;

    @Override
    public void sendMessage(DeviceMessage dm) {
        //如果数据变化则，发送emqx
        if (dm != null) {
            commonDevice.sendMessage(dm);
        }
    }

    @Override
    public boolean processData() throws Exception {
        String sql = "select * from  t_kl_baseinfo";
        List<Map<String, Object>> maps = postgreJdbcTemple.queryForList(sql);
        log.info("广州增城客流统计查询结果：{}", JSON.toJSONString(maps));
        HaiKangPassengerFlow HaiKangPassengerFlowBean = new HaiKangPassengerFlow();
        if (!CollectionUtils.isEmpty(maps)) {
            for (Map<String, Object> resultMap : maps) {
                log.info("海康客流统计：{}", JSON.toJSONString(resultMap));
                HaiKangPassengerFlowBean.setCurrentNum((Integer) resultMap.get("currentNum") < 0 ? 0 : (Integer) resultMap.get("currentNum"));
                HaiKangPassengerFlowBean.setDeviceFailureNum((Integer) resultMap.get("deviceFailureNum") < 0 ? 0 : (Integer) resultMap.get("deviceFailureNum"));
                HaiKangPassengerFlowBean.setTodayNum((Integer) resultMap.get("todayNum") < 0 ? 0 : (Integer) resultMap.get("todayNum"));
                HaiKangPassengerFlowBean.setStreetCurrentNum((Integer) resultMap.get("streetCurrentNum") < 0 ? 0 : (Integer) resultMap.get("streetCurrentNum"));
                HaiKangPassengerFlowBean.setStreetTodayNum((Integer) resultMap.get("streetTodayNum") < 0 ? 0 : (Integer) resultMap.get("streetTodayNum"));
            }
            //消息发送
            for (String key : deviceParamMap.keySet()) {
                log.info("客流key================={}", key);
                DeviceMessage deviceMessage = deviceParamMap.get(key);
                log.info("开始消息发送");
                if ("flExsitPeopleNum".equals(key)) {
                    deviceMessage.setValue(String.valueOf(HaiKangPassengerFlowBean.getStreetCurrentNum()));
                    deviceMessage.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    log.info("发送海康客流步行街实时人数：{}", JSON.toJSONString(deviceMessage));
                    sendMessage(deviceMessage);
                }
                if ("accFlInNum".equals(key)) {
                    deviceMessage.setValue(String.valueOf(HaiKangPassengerFlowBean.getStreetTodayNum()));
                    deviceMessage.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    log.info("发送海康客流步行街当日累计人数：{}", JSON.toJSONString(deviceMessage));
                    sendMessage(deviceMessage);
                }
                if ("bdExsitPeopleNum".equals(key)) {
                    deviceMessage.setValue(String.valueOf(HaiKangPassengerFlowBean.getCurrentNum()));
                    deviceMessage.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    log.info("发送海康客流场内实时人数：{}", JSON.toJSONString(deviceMessage));
                    sendMessage(deviceMessage);
                }
                if ("accInNum".equals(key)) {
                    deviceMessage.setValue(String.valueOf(HaiKangPassengerFlowBean.getTodayNum()));
                    deviceMessage.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    log.info("发送海康客流当日累计人数：{}", JSON.toJSONString(deviceMessage));
                    sendMessage(deviceMessage);
                }
            }
        }
        return true;
    }


    @Override
    public void dispatchCommand(String meter, Integer funcid, String value, String message) {
    }

    @Override
    public boolean processData(String... obj) throws Exception {
        return false;
    }
}