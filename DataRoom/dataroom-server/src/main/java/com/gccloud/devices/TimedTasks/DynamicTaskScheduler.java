package com.gccloud.devices.TimedTasks;

import com.alibaba.druid.support.ibatis.SqlMapClientImplWrapper;
import com.gccloud.devices.webSocket.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

import static com.gccloud.devices.api.Client.SenUrl;
import static com.gccloud.devices.webSocket.MyWebSocketHandler.broadcastMessage;

@Service
public class DynamicTaskScheduler {
    @Autowired
    private ClientTaskRepository taskRepository;
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TaskScheduler taskScheduler;

    // 用于存储任务ID和对应的ScheduledFuture
    private Map<Integer, ScheduledFuture<?>> jobsMap = new HashMap<>();

    @PostConstruct
    public void scheduleRunnableWithCronTrigger() {
        int successfulTasks = 0;
        for (ClientTask task : taskRepository.findAll()) {
            try {
                ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> {
                    try {
                        executeTask(task);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, new CronTrigger(task.getCron()));
                if (scheduledTask != null) {
                    jobsMap.put(task.getId(), scheduledTask);
                    successfulTasks++;
                }
            } catch (Exception e) {
                System.out.println("调度任务ID失败: " + task.getId());
            }
        }
        System.out.println(successfulTasks + " 定时任务启动成功");
    }

    private void executeTask(ClientTask task) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        // 输出任务详情
        System.out.println("正在执行的任务- ID: " + task.getId() +
                ", Name: " + task.getName() +
                ", Type: " + task.getType() +
                ", Cron: " + task.getCron() +
                ", Device: " + task.getDevice() +
                ", Data: " + task.getData() +
                ", Time: " + formattedNow);
        String context = null;
        // 根据任务类型执行相应逻辑
        switch (task.getType()) {
            case "1": // 发送
                // 执行发送相关的逻辑
                context = "{\"type\":1001,\"data\":{\"url\":\"" + task.getData() + "\"}}";
                break;
            case "2": // 黑屏
                // 执行黑屏相关的逻辑
                context = "{\"type\":1004,\"data\":{\"black\":\"\"}}";
                break;
            case "3": // 亮屏
                // 执行亮屏相关的逻辑
                context = "{\"type\":1005,\"data\":{\"light\":\"\"}}";
                break;
            default:
                System.out.println("未知任务类型。");
        }
        if(task.getDevice() == -1){
            //群发
            broadcastMessage(context);
            if(Objects.equals(task.getType(), "1")){
                SenUrl(jdbcTemplate,"", task.getData(),true);
            }
        }else {
            String sqlQuery = "SELECT * FROM `client_inf` WHERE `grouping` = ?";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sqlQuery, task.getDevice() );
            for (Map<String, Object> row : result) {
                // 获取 device_id 对应的值
                Object deviceId = row.get("device_id");
                MyWebSocketHandler.sendMessageToClient(deviceId.toString(), context);
                if(Objects.equals(task.getType(), "1")){
                    SenUrl(jdbcTemplate,deviceId.toString(), task.getData(),false);
                }
            }

        }
    }


    // 方法用于动态添加或更新任务
    public void addOrUpdateTask(ClientTask task) {
        // 先取消现有的任务
        ScheduledFuture<?> scheduledTask = jobsMap.get(task.getId());
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        // 调度新的任务
        scheduledTask = taskScheduler.schedule(() -> {
            try {
                executeTask(task);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, new CronTrigger(task.getCron()));
        jobsMap.put(task.getId(), scheduledTask);
    }

    // 方法用于取消任务
    public void cancelTask(Integer taskId) {
        ScheduledFuture<?> scheduledTask = jobsMap.get(taskId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            jobsMap.remove(taskId);
        }
    }
}

