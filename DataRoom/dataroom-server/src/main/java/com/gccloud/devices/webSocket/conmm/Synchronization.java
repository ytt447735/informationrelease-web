package com.gccloud.devices.webSocket.conmm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Component
public class Synchronization implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }
    public String getDbType(String keyValue, String postBody) {
        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
        try {
            // 尝试执行查询
            String sqlQuery = "SELECT * FROM `client_inf` WHERE `device_id` = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sqlQuery, keyValue);
            // 如果查询结果不为空，则更新equipment字段的值
            String sqlUpdate = "UPDATE `client_inf` SET equipment = ?, offline_time = '' WHERE device_id = ?";
            jdbcTemplate.update(sqlUpdate, postBody, keyValue);
//            return ResponseEntity.ok(result);
            return "{\"type\":1001,\"data\":" + toJson(result) + "}";
        } catch (EmptyResultDataAccessException e) {
            // 如果查询结果为空，则表示未找到数据，执行插入操作
            String sqlInsert = "INSERT INTO `client_inf` (`device_id`, equipment,offline_time) VALUES (?, ?,'')";
            // 假设需要插入的其他字段值为 ...，你需要根据你的实际情况修改
            jdbcTemplate.update(sqlInsert, keyValue, postBody); // 这里需要传入其他字段的值
            return "{\"type\":1100,\"message\":\"Data inserted successfully\"}";
        }
    }

    private static String toJson(Map<String, Object> result) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}"; // 如果转换失败，则返回一个空对象
        }
    }

    public Boolean SenOfflineTime(String keyValue) {
        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedDateTime = now.format(formatter);
        try {
            // 尝试执行查询
            String sqlQuery = "SELECT * FROM `client_inf` WHERE `device_id` = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sqlQuery, keyValue);
            // 如果查询结果不为空，则更新equipment字段的值
            String sqlUpdate = "UPDATE `client_inf` SET `offline_time` = ? WHERE `device_id` = ?";
            jdbcTemplate.update(sqlUpdate, formattedDateTime, keyValue);
//            return ResponseEntity.ok(result);
            return true;
        } catch (EmptyResultDataAccessException e) {
            // 如果查询结果为空，则表示未找到数据，执行插入操作
            String sqlInsert = "INSERT INTO `client_inf` (`device_id`, `offline_time`) VALUES (?, ?)";
            // 假设需要插入的其他字段值为 ...，你需要根据你的实际情况修改
            jdbcTemplate.update(sqlInsert, keyValue, formattedDateTime); // 这里需要传入其他字段的值
            return true;
        }
    }

}
