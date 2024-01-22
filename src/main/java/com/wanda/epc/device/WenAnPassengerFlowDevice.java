package com.wanda.epc.device;

import com.alibaba.fastjson.JSON;
import com.wanda.epc.param.DeviceMessage;
import com.wanda.epc.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author liurs
 * @description 北京文安客流采集
 * @date 2024/01/19
 */
@Slf4j
@Service
public class WenAnPassengerFlowDevice extends BaseDevice {

    private final static Logger logger = LoggerFactory.getLogger(WenAnPassengerFlowDevice.class);

    @Autowired
    private JdbcTemplate postgreJdbcTemple;

    @Autowired
    private CommonDevice commonDevice;

    private final String totalSql = "SELECT * FROM public_view_count_org_day ORDER BY counttime DESC";

    private final String bxjSql = "SELECT * FROM public_view_count_zone_minute_bxj";

    @Override
    public void sendMessage(DeviceMessage dm) {
        //如果数据变化则，发送emqx
        if (dm != null) {
            commonDevice.sendMessage(dm);
        }
    }

    @Override
    public boolean processData() {
        log.info("=====================开始进行客流采集=====================");
        //当日累计人数
        String todayNum = "";
        //步行街当日累计人数
        String streetTodayNum = "";
        //步行街实时人数
        String streetCurrentNum = "";
        //场内实时人数
        String currentNum = "";

        List<Map<String, Object>> totalList = postgreJdbcTemple.queryForList(totalSql);
        List<Map<String, Object>> bxjList = postgreJdbcTemple.queryForList(bxjSql);
        log.info("客流统计查询结果：{}", JSON.toJSONString(totalList));
        log.info("客流统计查询结果：{}", JSON.toJSONString(bxjList));
        if (!CollectionUtils.isEmpty(totalList)){
            Map<String, Object> map = totalList.get(0);
            if (ObjectUtils.isNotEmpty(map)){
                todayNum = String.valueOf(map.get("innum"));
                streetTodayNum = String.valueOf(map.get("outnum"));
                List<DeviceMessage> accInNumMessagesList = deviceParamListMap.get("accInNum");
                if (!CollectionUtils.isEmpty(accInNumMessagesList)) {
                    for (DeviceMessage deviceMessage : accInNumMessagesList) {
                        deviceMessage.setValue(todayNum);
                        deviceMessage.setUpdateTime(ConvertUtil.getNowDateTime("yyyyMMddHHmmss"));
                        sendMessage(deviceMessage);
                        logger.info("当日累计人数:" + deviceMessage.getEqId() + deviceMessage.getParamId() + "值为：" + todayNum);
                    }
                }
                List<DeviceMessage> accFlInNumMessagesList = deviceParamListMap.get("accFlInNum");
                if (!CollectionUtils.isEmpty(accFlInNumMessagesList)) {
                    for (DeviceMessage deviceMessage : accFlInNumMessagesList) {
                        deviceMessage.setValue(streetTodayNum);
                        deviceMessage.setUpdateTime(ConvertUtil.getNowDateTime("yyyyMMddHHmmss"));
                        sendMessage(deviceMessage);
                        logger.info("步行街当日累计人数:" + deviceMessage.getEqId() + deviceMessage.getParamId() + "值为：" + streetTodayNum);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(bxjList)){
            Map<String, Object> map = totalList.get(0);
            if (ObjectUtils.isNotEmpty(map)){
                streetCurrentNum = String.valueOf(map.get("innum"));
                List<DeviceMessage> streetCurrentNumMessagesList = deviceParamListMap.get("flExsitPeopleNum");
                if (!CollectionUtils.isEmpty(streetCurrentNumMessagesList)) {
                    for (DeviceMessage deviceMessage : streetCurrentNumMessagesList) {
                        deviceMessage.setValue(streetCurrentNum);
                        deviceMessage.setUpdateTime(ConvertUtil.getNowDateTime("yyyyMMddHHmmss"));
                        sendMessage(deviceMessage);
                        logger.info("步行街实时人数:" + deviceMessage.getEqId() + deviceMessage.getParamId() + "值为：" + streetCurrentNum);
                    }
                }
                List<DeviceMessage> bdExsitPeopleNumMessagesList = deviceParamListMap.get("bdExsitPeopleNum");
                Integer todayNumValue = Integer.parseInt(todayNum);
                Integer streetTodayNumValue = Integer.parseInt(streetTodayNum);
                currentNum = String.valueOf(todayNumValue - streetTodayNumValue);
                if (!CollectionUtils.isEmpty(bdExsitPeopleNumMessagesList)) {
                    for (DeviceMessage deviceMessage : bdExsitPeopleNumMessagesList) {
                        deviceMessage.setValue(currentNum);
                        deviceMessage.setUpdateTime(ConvertUtil.getNowDateTime("yyyyMMddHHmmss"));
                        sendMessage(deviceMessage);
                        logger.info("场内实时人数:" + deviceMessage.getEqId() + deviceMessage.getParamId() + "值为：" + currentNum);
                    }
                }
            }
        }
        log.info("=====================客流采集完成=====================");
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