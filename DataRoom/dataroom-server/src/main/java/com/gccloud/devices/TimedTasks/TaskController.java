package com.gccloud.devices.TimedTasks;

import com.gccloud.devices.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private ClientTaskRepository taskRepository;

    @Autowired
    private DynamicTaskScheduler dynamicTaskScheduler;

    // 获取全部任务
    @GetMapping("/list")
    public Response getAllTasks() {
        return Response.ok(taskRepository.findAll());
    }


    // 添加新任务
    @PostMapping("/add")
    public Response addTask(@RequestBody ClientTask task) {
        ClientTask savedTask = taskRepository.save(task);
        dynamicTaskScheduler.addOrUpdateTask(savedTask);
        return Response.ok(savedTask);
    }

    // 更新任务
    @PostMapping("/update/{taskId}")
    public Response updateTask(@PathVariable Integer taskId, @RequestBody ClientTask task) {
        return taskRepository.findById(taskId).map(existingTask -> {
            existingTask.setName(task.getName());
            existingTask.setCron(task.getCron());
            existingTask.setType(task.getType());
            existingTask.setData(task.getData());
            taskRepository.save(existingTask);
            dynamicTaskScheduler.addOrUpdateTask(existingTask);
            return Response.ok(existingTask);
        }).orElseGet(() -> Response.ok(ResponseEntity.notFound().build()));
    }

    // 删除任务
    @GetMapping("/delete/{taskId}")
    public Response deleteTask(@PathVariable Integer taskId) {
        return taskRepository.findById(taskId).map(task -> {
            taskRepository.delete(task);
            dynamicTaskScheduler.cancelTask(task.getId());
            return Response.ok("成功");
        }).orElseGet(() -> Response.ok(ResponseEntity.notFound().build()));
    }
}

