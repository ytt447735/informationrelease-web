package com.gccloud.devices.TimedTasks;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "client_tasks")
public class ClientTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Getter
    private Integer id;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String cron;
    @Setter
    @Getter
    private String type;
    @Setter
    @Getter
    private Integer device;
    @Setter
    @Getter
    private String data;

    // getters and setters
}


