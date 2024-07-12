package com.wanda.epc.device;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wanda.epc.param.DeviceMessage;
import com.wanda.epc.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class PassengerFlowDevice extends BaseDevice {

    @Autowired
    private CommonDevice commonDevice;

    @Value("${epc.gcId}")
    private String gcId;

    @Value("${epc.gatewayId}")
    private String gatewayId;

    @Value("${apiUrl}")
    private String apiUrl;

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
            PassengerFlow passengerFlowBean = query();
            log.info("客流统计查询结果：{}", JSON.toJSONString(passengerFlowBean));
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
                    sendMsg(passengerFlowBean.getCurrentNum(), deviceMessage);
                    log.info("场内实时人数:" + passengerFlowBean.getCurrentNum());
                }
                //当日累计人数
                if (Objects.equals("accInNum", deviceMessage.getOutParamId())) {
                    sendMsg(passengerFlowBean.getTodayNum(), deviceMessage);
                    log.info("当日累计人数:" + passengerFlowBean.getTodayNum());
                }
                //步行街实时人数
                if (Objects.equals("flExsitPeopleNum", deviceMessage.getOutParamId())) {
                    sendMsg(passengerFlowBean.getStreetCurrentNum(), deviceMessage);
                    log.info("步行街实时人数:" + passengerFlowBean.getStreetCurrentNum());
                }
                //步行街当日累计人数
                if (Objects.equals("accFlInNum", deviceMessage.getOutParamId())) {
                    sendMsg(passengerFlowBean.getStreetTodayNum(), deviceMessage);
                    log.info("步行街当日累计人数:" + passengerFlowBean.getStreetTodayNum());
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

    public PassengerFlow query() {
        PassengerFlow passengerFlowBean = new PassengerFlow();
        try {
            URL obj = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject data = new JSONObject(response.toString()).getJSONObject("data");

            String retention = data.getStr("retention");
            String inpv = data.getStr("inpv");
            String storeRetention = data.getStr("street_retention");
            String streetInpv = data.getStr("street_inpv");

            passengerFlowBean.setCurrentNum(NumberUtil.isInteger(retention) ? Integer.parseInt(retention) : 0);
            passengerFlowBean.setTodayNum(NumberUtil.isInteger(inpv) ? Integer.parseInt(inpv) : 0);
            passengerFlowBean.setStreetCurrentNum(NumberUtil.isInteger(storeRetention) ? Integer.parseInt(storeRetention) : 0);
            passengerFlowBean.setStreetTodayNum(NumberUtil.isInteger(streetInpv) ? Integer.parseInt(streetInpv) : 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return passengerFlowBean;
    }

}