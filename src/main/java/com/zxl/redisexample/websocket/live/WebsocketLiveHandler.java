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

// 接收消息
@Component
public class WebsocketLiveHandler extends TextWebSocketHandler {

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
    }

    /**
     * 处理发送的信息
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("收到:" + session + "发送的信息：" + message);
        session.sendMessage(new TextMessage("服务器返回信息11"));
    }
}
