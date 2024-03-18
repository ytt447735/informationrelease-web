package com.gccloud.devices.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gccloud.common.vo.R;
import com.gccloud.devices.Response;
import com.gccloud.devices.webSocket.MyWebSocketHandler;
import com.gccloud.devices.webSocket.WebSocketSessionInfo;
import com.google.gson.JsonParser;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

import static com.gccloud.devices.webSocket.MyWebSocketHandler.sessions;

@RestController
@CrossOrigin
public class Client {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @RequestMapping(value="/api/client/getUrl", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> getDbType(
            @RequestParam(value = "key", required = true) String keyValue,
            @RequestBody String postBody
    ) {
        try {
            // 尝试执行查询
            String sqlQuery = "SELECT * FROM `client_inf` WHERE `device_id` = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sqlQuery, keyValue);
            // 如果查询结果不为空，则更新equipment字段的值
            String sqlUpdate = "UPDATE `client_inf` SET equipment = ? WHERE device_id = ?";
            jdbcTemplate.update(sqlUpdate, postBody, keyValue);
//            return ResponseEntity.ok(result);
            return ResponseEntity.status(HttpStatus.OK).body("{\"code\":200,\"data\":" + toJson(result) + "}");
        } catch (EmptyResultDataAccessException e) {
            // 如果查询结果为空，则表示未找到数据，执行插入操作
            String sqlInsert = "INSERT INTO `client_inf` (`device_id`, equipment) VALUES (?, ?)";
            // 假设需要插入的其他字段值为 ...，你需要根据你的实际情况修改
            jdbcTemplate.update(sqlInsert, keyValue, postBody); // 这里需要传入其他字段的值
            return ResponseEntity.status(HttpStatus.OK).body("{\"code\":201,\"message\":\"Data inserted successfully\"}");
        }
    }
    //发送url
    @RequestMapping(value="/api/client/SendUrl", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> SendUrl(
            @RequestBody String postBody
    ) throws IOException, InterruptedException {
        System.out.println("postBody: " + postBody);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(postBody).getAsJsonObject();

        String device = jsonObject.get("device").getAsString().replace("[", "").replace("]", "");
        String url = jsonObject.get("url").getAsString();
        String[] deviceArray = device.split(",");
        String context = "{\"type\":1001,\"data\":{\"url\":\"" + url + "\"}}";
        StringBuilder Reply = new StringBuilder();
        for (String s : deviceArray) {
            System.out.println("device===: " + s);
            String sqlQuery = "SELECT * FROM `client_inf` WHERE `id` = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sqlQuery, s);
            String deviceId = result.get("device_id").toString();
            String name = result.get("name").toString();
            System.out.println("result: " + deviceId);
            SenUrl(jdbcTemplate,deviceId, url,false);//修改url
            // 用于清空缓存
            MyWebSocketHandler.sendMessageToClient(deviceId, "{\"type\":1001,\"data\":{\"url\":\"hello\"}}");
            Thread.sleep(500);
            // 正式推送内容
            if(!MyWebSocketHandler.sendMessageToClient(deviceId, context)){
                Reply.append(name).append(" 即时发送失败，缓存发送成功\\n");
            }else {
                Reply.append(name).append(" 发送成功\\n");
            }
            Thread.sleep(500);
        }
        System.out.println("device: " + device);
        System.out.println("url: " + url);
        return ResponseEntity.status(HttpStatus.OK).body("{\"code\":200,\"message\":\"" + Reply + "\"}");
    }

    private String toJson(Map<String, Object> result) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}"; // 如果转换失败，则返回一个空对象
        }
    }

    //单发设置url
    @RequestMapping(value="/api/client/SetUrl", method = RequestMethod.GET)
    public ResponseEntity<Object> SetUrl(
            @RequestParam(value = "key", required = false) String keyValue,//设备id
            @RequestParam(value = "url", required = true) String urlValue,//url
            @RequestParam(value = "instantly", required = true) String instantly,//1立即发送 0不立即发送
            @RequestParam(value = "mode", required = true) int modeValue //1单发 2群发
    ) throws IOException {
        // group > key
        String ins="1000";
        if(instantly.equals("1")){
            ins="1001";
        }
        String Reply = "即时发送失败，缓存发送成功！";
        switch (modeValue){
            case 1:
                //单发
                SenUrl(jdbcTemplate,keyValue, urlValue,false);
                String context0 = "{\"type\":"+ins+",\"data\":{\"url\":\""+urlValue+"\"}}";
                if(MyWebSocketHandler.sendMessageToClient(keyValue, context0)){
                    Reply  = "全部发送成功";
                }
                break;
            case 2:
                //群发
                SenUrl(jdbcTemplate,keyValue, urlValue,true);
                String context1 = "{\"type\":"+ins+",\"data\":{\"url\":\""+urlValue+"\"}}";
                MyWebSocketHandler.broadcastMessage(context1);
                Reply  = "全部发送成功";
                break;
        }
        return ResponseEntity.status(HttpStatus.OK).body("{\"code\":100,\"data\":\""+Reply+"\"}");
    }

    //群发消息
    @RequestMapping(value="/api/client/SetGroupUrl", method = RequestMethod.GET)
    public ResponseEntity<Object> SetGroupUrl(
            @RequestParam(value = "group_id", required = true) String group_id,//群组id
            @RequestParam(value = "mode", required = true) int modeValue,//1立即发送url 2缓存发送 3更改服务器 4黑屏 5亮屏 6重启
            @RequestParam(value = "data", required = true) String dataValue//url
    ) throws IOException {
        //群发消息
        String context;
        switch (modeValue) {
            //立即发送url
            case 1:
                context = "{\"type\":1001,\"data\":{\"url\":\"" + dataValue + "\"}}";
                break;
            //缓存发送url
            case 2:
                context = "{\"type\":1000,\"data\":{\"url\":\"" + dataValue + "\"}}";
                break;
            //更改服务器
            case 3:
                context = "{\"type\":1002,\"data\":{\"server\":\"" + dataValue + "\"}}";
                break;
            //黑屏
            case 4:
                context = "{\"type\":1004,\"data\":{\"black\":\"\"}}";
                break;
            //亮屏
            case 5:
                context = "{\"type\":1005,\"data\":{\"light\":\"\"}}";
                break;
            //重启
            case 6:
                context = "{\"type\":1006,\"data\":{\"reboot\":\"\"}}";
                break;
            default:
                context = "";
                break;
        }

        String error = "";
        StringBuilder Reply = new StringBuilder();
        String code = "100";

        String sqlQuery = "SELECT * FROM `client_group` WHERE `id` = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sqlQuery,group_id);
        for (Map<String, Object> row : result) {
            // 获取 device_id 对应的值
            Object member = row.get("member");

            // 使用Gson解析JSON字符串
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(member.toString(), JsonObject.class);

            // 获取device_id数组
            JsonArray deviceIdArray = jsonObject.getAsJsonArray("device_id");
            System.out.println("device_id:");
            for (int i = 0; i < deviceIdArray.size(); i++) {
                String deviceId = deviceIdArray.get(i).getAsString();
                System.out.println(deviceId);
                try {
                    if (modeValue == 1 || modeValue == 2) {
                        SenUrl(jdbcTemplate,deviceId, dataValue,false);
                    }
                    if(!MyWebSocketHandler.sendMessageToClient(deviceId, context)){
                        Reply.append(deviceId).append("即时发送失败，缓存发送成功！\n");
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    error = e.getMessage();
                    code = "200";
                    throw new RuntimeException(e);
                }
            }
        }
        if(error.equals("")){
            return ResponseEntity.status(HttpStatus.OK).body("{\"code\":"+code+",\"data\":\""+Reply+"\"}");
        }
        return ResponseEntity.status(HttpStatus.OK).body("{\"code\":"+code+",\"data\":\""+error+"\"}");
    }

    //获取客户端列表
    @RequestMapping(value="/api/client/GetList", method = RequestMethod.GET)
    public ResponseEntity<Object> GetList(
            @RequestParam(value = "module", required = false) String module // 分组
    ) throws IOException {
        List<Map<String, Object>> result = null;
        if(!Objects.equals(module, "")){
            String sqlQuery = "SELECT * FROM `client_inf` WHERE `grouping` = ?";
            result = jdbcTemplate.queryForList(sqlQuery, module);
        }else {
            String sqlQuery = "SELECT * FROM `client_inf`";
            result = jdbcTemplate.queryForList(sqlQuery);
        }
        for (Map<String, Object> row : result) {
            // 获取 device_id 对应的值
            Object deviceId = row.get("device_id");
            boolean online = false;
            String Time ="";
            for (WebSocketSessionInfo sessionInfo : sessions.values()) {
                if (sessionInfo != null) {
                    if(sessionInfo.getDeviceId().equals(deviceId.toString())){
                        online = true;
                        Time = sessionInfo.getClientConnectionDuration();
                        break;
                    }
                }
            }
            if (online)
                row.put("online", 1);
            else
                row.put("online", 0);
            row.put("OnlineTime", Time);
            if(row.get("grouping") != null) {
                String sqlQuery = "SELECT * FROM `client_group` WHERE `id` = ?";
                List<Map<String, Object>> result1 = jdbcTemplate.queryForList(sqlQuery, row.get("grouping"));
                result1.forEach(row1 -> {
                    row.put("groupingName", row1.get("name"));
                });
            }else {
                row.put("groupingName", "");
            }
        }
        // 创建 ObjectMapper 对象
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 将 List<Map<String, Object>> 转换为 JSON 字符串
            String json = objectMapper.writeValueAsString(result);
            // 打印 JSON 字符串
//            System.out.println(json);
            return ResponseEntity.status(HttpStatus.OK).body("{\"code\":200,\"msg\": \"success\",\"data\":" + json + "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body("{\"code\":100,\"msg\": \"error\",\"data\":\"\"}");
    }

    //获取客户端截屏
    @RequestMapping(value="/api/client/GetScreen", method = RequestMethod.GET)
    public ResponseEntity<Object> GetScreen(
            @RequestParam(value = "key", required = true) String keyValue//设备id
    ) throws IOException, InterruptedException {
        // 创建一个倒计时锁
        MyWebSocketHandler.sendMessageToClient(keyValue,"{\"type\":1003,\"msg\":\"\"}");
        String reply = MyWebSocketHandler.waitForReply(keyValue, 5*1000);
        if(reply == null){
            return ResponseEntity.status(HttpStatus.OK).body("{\"code\":200,\"data\":\"\",\"msg\":\"error\"}");
        }
        return ResponseEntity.status(HttpStatus.OK).body("{\"code\":200,\"data\":\""+reply+"\",\"msg\":\"success\"}");
    }
    // 删除设备
    @PostMapping("/api/client/delete/{id}")
    @ApiOperation(value = "删除", notes = "删除", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public R<Boolean> delete(@PathVariable("id") String id) {
        String sqlDelete = "DELETE FROM `client_inf` WHERE `id` = ?";
        jdbcTemplate.update(sqlDelete, id);
        return R.success(true);
    }
    //修改设备
    @PostMapping("/api/client/update")
    @ResponseBody
    public Response update(@RequestBody Map<String, Object> params) {
        try {
            String sqlUpdate = "UPDATE `client_inf` SET `name` = ?, `grouping` = ? WHERE `id` = ?";
            jdbcTemplate.update(sqlUpdate, params.get("name"), params.get("group"), params.get("id"));
            return Response.ok("修改成功");
        } catch (Exception e) {
            return Response.error("Update failed"+e.getMessage());
        }
    }
    //屏幕设置
    @GetMapping("/api/client/ScreenStatus/{id}/{status}")
    public Response delete(@PathVariable("id") String id,@PathVariable("status") String status) throws IOException {
        try {
            String sql = "SELECT * FROM `client_inf` WHERE `id` = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, id);
            String deviceId = result.get("device_id").toString();
            String context = "{\"type\":1004,\"data\":{\"black\":\"\"}}";//黑屏
            if(status.equals("1")){
                context = "{\"type\":1005,\"data\":{\"light\":\"\"}}";//亮屏
            }
            if(MyWebSocketHandler.sendMessageToClient(deviceId, context)){
                return Response.ok("设置成功");
            }
            return Response.error("设置失败");
        }catch (Exception e){
            return Response.error("Update failed"+e.getMessage());
        }
    }

    //修改url
    public static void SenUrl(JdbcTemplate jdbcTemplate, String keyValue, String url, boolean all) {
        try {
            String sqlUpdate ="";
            if (all){
                sqlUpdate = "UPDATE `client_inf` SET url = ?";
                jdbcTemplate.update(sqlUpdate, url);
            }else {
                sqlUpdate = "UPDATE `client_inf` SET url = ? WHERE device_id = ?";
                jdbcTemplate.update(sqlUpdate, url, keyValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
