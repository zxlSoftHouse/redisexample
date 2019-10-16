package com.zxl.redisexample.websocket.live;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 接收消息
@Component
public class WebsocketLiveHandler extends TextWebSocketHandler {

    // 存储所有websocket信息
    public static Map<String, List<WebSocketSession>> roomUserMap = new ConcurrentHashMap<>();

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * websocket建立成功操作，进入房间--建立连接
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("新用户来了" + session);
        // session.getAttributes().get("userId");
        // String roomId = (String)session.getAttributes().get("roomId");
        // 将sum::roomId作为key并且实现内容+1统计计入房间人数 incr + 1操作
        // redisTemplate.opsForValue().increment("sum::" + roomId);
        // 使用zset实现存储房间信息，排除重复的情况，可通过score属性排序
        UriComponents uriComponents = UriComponentsBuilder.fromUri(session.getUri()).build();
        String roomId = uriComponents.getQueryParams().getFirst("roomId");
        String userId = uriComponents.getQueryParams().getFirst("userId");
        String score = uriComponents.getQueryParams().getFirst("score");
        List<WebSocketSession> sessionList = roomUserMap.get(roomId);
        sessionList.add(session);
        // 本地
        roomUserMap.put(roomId, sessionList);
        // 新增 ---保存
        redisTemplate.opsForZSet().add("roominfo::" + roomId, userId, Double.valueOf(score));
    }

    /**
     * websocket建立通道关闭，断开连接
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("用户走了" + session);
        // 获取链接参数
        UriComponents uriComponents = UriComponentsBuilder.fromUri(session.getUri()).build();
        String roomId = uriComponents.getQueryParams().getFirst("roomId");
        redisTemplate.opsForZSet().remove("roominfo::" + roomId);
    }

    /**
     * 处理发送的信息
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 收到消息
        System.out.println("收到:" + session + "发送的信息：" + message);
        UriComponents uriComponents = UriComponentsBuilder.fromUri(session.getUri()).build();
        String roomId = uriComponents.getQueryParams().getFirst("roomId");
        String userId = uriComponents.getQueryParams().getFirst("userId");
        // 保存消息到redis

        // 发送消息到某个客户端
        session.sendMessage(new TextMessage("服务器返回信息11"));
    }

    // 推送消息（其他人发的消息，推送到直播间）
    // 触发，如果redis里面有数据,则触发执行
    public void recvRedisMessage(String data) throws IOException {
        // 推送这条消息
        // 1、获取roomId
        // 2、根据房间找session
        String roomId = "";
        String message = "";
        List<WebSocketSession> webSocketSessionList = roomUserMap.get(roomId);
        for (WebSocketSession webSocketSession : webSocketSessionList) {
            webSocketSession.sendMessage(new TextMessage(message));
        }
    }
}
