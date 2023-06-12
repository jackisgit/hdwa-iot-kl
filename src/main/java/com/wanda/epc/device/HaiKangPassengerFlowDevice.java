package com.wanda.epc.device;

import com.alibaba.fastjson.JSON;
import com.wanda.epc.param.DeviceMessage;
import com.wanda.epc.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * @author LianYanFei
 * @description 海康威视客流采集
 * @date 2023/5/20
 */
@Slf4j
@Service
public class HaiKangPassengerFlowDevice extends BaseDevice {

    @Autowired
    private JdbcTemplate postgreJdbcTemple;

    @Autowired
    private CommonDevice commonDevice;

    @Value("${epc.gcId}")
    private String gcId;

    @Value("${epc.gatewayId}")
    private String gatewayId;

    private final String klSql = "select * from  t_kl_baseinfo";

    @Override
    public void sendMessage(DeviceMessage dm) {
        //如果数据变化则，发送emqx
        if (dm != null) {
            commonDevice.sendMessage(dm);
        }
    }

    @Override
    public boolean processData() throws Exception {
        log.info("=====================开始进行客流采集=====================");
        Set<String> keys = redisUtil.scan("Pj" + this.gcId + "." + this.gatewayId + ".*");
        if (!CollectionUtils.isEmpty(keys)) {
            List<Map<String, Object>> maps = postgreJdbcTemple.queryForList(klSql);
            log.info("客流统计查询结果：{}", JSON.toJSONString(maps));
            if (!CollectionUtils.isEmpty(maps)) {
                HaiKangPassengerFlow haiKangPassengerFlowBean = new HaiKangPassengerFlow();
                for (Map<String, Object> resultMap : maps) {
                    log.info("海康客流统计：{}", JSON.toJSONString(resultMap));
                    haiKangPassengerFlowBean.setCurrentNum((Integer) resultMap.get("currentNum") < 0 ? 0 : (Integer) resultMap.get("currentNum"));
                    haiKangPassengerFlowBean.setDeviceFailureNum((Integer) resultMap.get("deviceFailureNum") < 0 ? 0 : (Integer) resultMap.get("deviceFailureNum"));
                    haiKangPassengerFlowBean.setTodayNum((Integer) resultMap.get("todayNum") < 0 ? 0 : (Integer) resultMap.get("todayNum"));
                    haiKangPassengerFlowBean.setStreetCurrentNum((Integer) resultMap.get("streetCurrentNum") < 0 ? 0 : (Integer) resultMap.get("streetCurrentNum"));
                    haiKangPassengerFlowBean.setStreetTodayNum((Integer) resultMap.get("streetTodayNum") < 0 ? 0 : (Integer) resultMap.get("streetTodayNum"));
                }
                //消息发送
                for (String key : keys) {
                    log.info("客流key================={}", key);
                    DeviceMessage deviceMessage = JSON.parseObject(JSON.toJSONString(redisUtil.get(key)), DeviceMessage.class);
                    if (deviceMessage == null) {
                        continue;
                    }
                    log.info("开始消息发送");
                    //场内实时人数
                    if (Objects.equals("bdExsitPeopleNum", deviceMessage.getOutParamId())) {
                        sendMsg(haiKangPassengerFlowBean.getCurrentNum(), deviceMessage);
                        log.info("场内实时人数:" + haiKangPassengerFlowBean.getCurrentNum());
                    }
                    //当日累计人数
                    if (Objects.equals("accInNum", deviceMessage.getOutParamId())) {
                        sendMsg(haiKangPassengerFlowBean.getTodayNum(), deviceMessage);
                        log.info("当日累计人数:" + haiKangPassengerFlowBean.getTodayNum());
                    }
                    //步行街实时人数
                    if (Objects.equals("flExsitPeopleNum", deviceMessage.getOutParamId())) {
                        sendMsg(haiKangPassengerFlowBean.getStreetCurrentNum(), deviceMessage);
                        log.info("步行街实时人数:" + haiKangPassengerFlowBean.getStreetCurrentNum());
                    }
                    //步行街当日累计人数
                    if (Objects.equals("accFlInNum", deviceMessage.getOutParamId())) {
                        sendMsg(haiKangPassengerFlowBean.getStreetTodayNum(), deviceMessage);
                        log.info("步行街当日累计人数:" + haiKangPassengerFlowBean.getStreetTodayNum());
                    }
                }
            }
        }
        log.info("=====================客流采集完成=====================");
        return true;
    }

    public void sendMsg(Object value, DeviceMessage deviceMessage) {
        if (Objects.nonNull(deviceMessage)) {
            deviceMessage.setValue(value == null ? "0" : String.valueOf(value));
            deviceMessage.setUpdateTime(ConvertUtil.getNowDateTime("yyyyMMddHHmmss"));
            log.info("发送客流{}数据==={}", deviceMessage.getOutParamId(), JSON.toJSONString(deviceMessage));
            sendMessage(deviceMessage);
        }
    }

    @Override
    public void dispatchCommand(String meter, Integer funcid, String value, String message) {
    }

    @Override
    public boolean processData(String... obj) throws Exception {
        return false;
    }

}