package com.gccloud.devices.webSocket;

import com.gccloud.devices.webSocket.conmm.Synchronization;
import org.springframework.web.socket.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyWebSocketHandler implements WebSocketHandler {
    private static final Map<String, String> replyCache = new ConcurrentHashMap<>();// 缓存消息
    public static final Map<String, WebSocketSessionInfo> sessions = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立时的处理逻辑
        System.out.println("客户加入: " + session.getId());
        long currentTime = System.currentTimeMillis();
        sessions.put(session.getId(), new WebSocketSessionInfo(session, currentTime));
    }
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 收到消息时的处理逻辑
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String payload = textMessage.getPayload();
            System.out.println("收到消息: " + payload);

            // 判断收到的消息是否是 JSON 格式
            if (isJSONValid(payload)) {
                // 将 JSON 字符串转换为对象
                WebSocketMessageData Data = objectMapper.readValue(payload, WebSocketMessageData.class);
                // 处理消息数据
                int messageType = Data.getType();
                SetDeviceId(session.getId(),Data.getId());

                switch (messageType) {
                    case 1:
                        // 收到心跳包
//                        System.out.println("Received message type: 1");
                        break;
                    case 1000:
                        //同步并更新设备资料
                        Synchronization C = new Synchronization();
                        String D = C.getDbType(Data.getId(),Data.getData());
                        System.out.println("回复消息："+D);
                        session.sendMessage(new TextMessage(D));
                        break;
                    case 1003:
                        //截屏
                        storeReply(Data.getId(), Data.getData());
                        System.out.println("Received message type: 1001");
                        break;
                    default:
                        System.out.println("Received message type: " + messageType);
                        break;
                }
            } else {
                System.out.println("Received message is not valid JSON.");
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 处理传输错误时的逻辑
        System.out.println("客户传输出错: " + session.getId());
        System.out.println(exception.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // 连接关闭时的处理逻辑
        System.out.println("客户离开: " + session.getId());
        WebSocketSessionInfo sessionInfo = sessions.get(session.getId());
        if (sessionInfo != null) {
            Synchronization C = new Synchronization();
            C.SenOfflineTime(sessionInfo.getDeviceId());
        }
        sessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public static Boolean sendMessageToClient(String clientId, String message) throws IOException {
        // 发送消息给指定客户端
        System.out.println("发送消息给"+clientId+": " + message);
        WebSocketSessionInfo sessionInfo = GetSessionId(clientId);
        if (sessionInfo != null) {
            sendMessage(sessionInfo.getSession(), message);
            return true;
        }
        return false;
    }

    public static void broadcastMessage(String message) throws IOException {
        if(message==null || message.isEmpty()){return;}
        // 群发消息
        System.out.println("群发消息: " + message);
        for (WebSocketSessionInfo sessionInfo : sessions.values()) {
            sendMessage(sessionInfo.getSession(), message);
        }
    }

    private static void sendMessage(WebSocketSession session, String message) {
        // 发送消息
        try {
            System.out.println("发送消息: " + session.getId() + " -> " + message);
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetDeviceId(String clientId, String deviceId) throws IOException {
        // 修改DeviceId
        WebSocketSessionInfo sessionInfo = sessions.get(clientId);
        if (sessionInfo != null) {
            sessionInfo.SetDeviceId(deviceId);
        }
    }

    // 获取SessionId
    private static WebSocketSessionInfo GetSessionId(String deviceId) {
        for (WebSocketSessionInfo sessionInfo : sessions.values()) {
            if (sessionInfo != null) {
                if(sessionInfo.getDeviceId().equals(deviceId)){
                    return sessionInfo;
                }
            }
        }
        return null;
    }
    // 方法用于等待回复消息
    public static String waitForReply(String messageId, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (replyCache.containsKey(messageId)) {
                return replyCache.remove(messageId);
            }
            // 可以添加一些等待的逻辑，例如等待一段时间再进行检查
            try {
                Thread.sleep(100); // 每次等待100毫秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null; // 如果线程被中断，则退出等待
            }
        }
        return null; // 超时，未收到回复
    }

    // 方法用于存储回复消息
    public static void storeReply(String messageId, String reply) {
        replyCache.put(messageId, reply);
    }

    // 判断字符串是否是有效的 JSON 格式
    private boolean isJSONValid(String jsonInString) {
        try {
            objectMapper.readTree(jsonInString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
