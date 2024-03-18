package com.gccloud.devices.webSocket;

import org.springframework.web.socket.WebSocketSession;

// 其他方法...
public class WebSocketSessionInfo {
    private WebSocketSession session;
    private long connectionTime;
    private String deviceId;

    public WebSocketSessionInfo(WebSocketSession session, long connectionTime) {
        this.session = session;
        this.connectionTime = connectionTime;
        this.deviceId = "";
    }

    public WebSocketSession getSession() {
        return session;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public String getDeviceId() {
        return deviceId;
    }
    public void SetDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    public String getClientConnectionDuration() {
        // 获取客户端的连接时长
        long durationMillis = System.currentTimeMillis() - connectionTime;
        long years = durationMillis / (1000L * 60 * 60 * 24 * 365);
        long days = durationMillis / (1000L * 60 * 60 * 24) % 365;
        long hours = durationMillis / (1000L * 60 * 60) % 24;
        long minutes = durationMillis / (1000L * 60) % 60;

        StringBuilder builder = new StringBuilder();
        if (years > 0) {
            builder.append(years).append("年 ");
        }
        if (days > 0) {
            builder.append(days).append("天 ");
        }
        if (hours > 0) {
            builder.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("分钟 ");
        }

        return builder.toString().trim();
    }
}