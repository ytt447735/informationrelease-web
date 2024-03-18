package com.gccloud.devices.TimedTasks;


import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientTaskRepository extends JpaRepository<ClientTask, Integer> {
}
