package com.gccloud.devices.api.group;

import com.gccloud.common.controller.SuperController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiSort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import com.gccloud.devices.Response;

import java.util.List;
import java.util.Map;

@RestController("devicesGroupController")
@RequestMapping("/api/client/group")
@Api(tags = "设备分组")
@ApiSort(value = 10)
@CrossOrigin
@Slf4j
public class devices extends SuperController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @GetMapping("/list")
    @ResponseBody
    public Response getList() {
        try {
            String sqlQuery = "SELECT * FROM `client_group`";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sqlQuery);
            // 遍历结果列表，将"id"键替换为"code"
            for (Map<String, Object> entry : result) {
                Object id = entry.remove("id");
                entry.put("code", id);
            }
            return Response.ok(result); // 使用ok方法快速构建成功响应
        } catch (Exception e) {
            return Response.error("Data acquisition failed"); // 构建错误响应
        }
    }
    @PostMapping("/add")
    @ResponseBody
    public Response add(@RequestBody Map<String, Object> params) {
        try {
            String sqlInsert = "INSERT INTO `client_group` (`name`) VALUES (?)";
            jdbcTemplate.update(sqlInsert, params.get("name"));
            return Response.ok("Add success");
        } catch (Exception e) {
            return Response.error("Add failed");
        }
    }
    @PostMapping("/update")
    @ResponseBody
    public Response update(@RequestBody Map<String, Object> params) {
        try {
            String sqlUpdate = "UPDATE `client_group` SET `name` = ? WHERE `id` = ?";
            jdbcTemplate.update(sqlUpdate, params.get("name"), params.get("code"));
            return Response.ok("Update success");
        } catch (Exception e) {
            return Response.error("Update failed");
        }
    }
    @PostMapping("/delete/{code}")
    @ResponseBody
    public Response delete(@PathVariable("code") String code) {
        try {
            String sqlDelete = "DELETE FROM `client_group` WHERE `id` = ?";
            jdbcTemplate.update(sqlDelete, code);
            return Response.ok("Delete success");
        } catch (Exception e) {
            return Response.error("Delete failed");
        }
    }
}
