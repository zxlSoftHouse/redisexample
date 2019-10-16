package com.zxl.redisexample.websocket.live;

import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        // 保存消息到redis ？形式是streams 生产者和消费者的模式（现在只能redisClient实现）
        RedisClient redisClient = RedisClient.create("redis://106.12.38.52:6379"); // 每一次都创建
        StatefulRedisConnection<String, String> connect = redisClient.connect(); // 肯定会用连接池
        RedisCommands<String, String> redisCommands = connect.sync();
        redisCommands.xadd("room:msg:" + roomId, "userId", userId, "content", message.getPayload());
        // 发送消息到某个客户端
        // session.sendMessage(new TextMessage("服务器返回信息11"));
    }

    // 启动系统后，执行
    @PostConstruct // spring默认单实例，调用一次
    public void init() {
        new Thread(() -> {
            // 保存消息到redis ？形式是streams 生产者和消费者的模式（现在只能redisClient实现）
            RedisClient redisClient = RedisClient.create("redis://106.12.38.52:6379");
            StatefulRedisConnection<String, String> connect = redisClient.connect();
            RedisCommands<String, String> redisCommands = connect.sync();
            // 指定读取消息，读取不同房间的消息
            while (true) {
                try {

                    // 订阅当前服务器上用户对应的房间 --- 所有的房间
                    Set<String> strings = roomUserMap.keySet();
                    ArrayList<XReadArgs.StreamOffset<String>> streamOffsets = new ArrayList<>();
                    for (String roomId : strings) {
                        streamOffsets.add(XReadArgs.StreamOffset.latest("room:msg:" + roomId));
                    }
                    if (streamOffsets.size() > 0) {
                        List<StreamMessage<String, String>> stream_sms_send = redisCommands.xread(XReadArgs.Builder.block(1000), XReadArgs.StreamOffset.latest("room:msg:1001"));
                        for (StreamMessage<String, String> stringStringStreamMessage : stream_sms_send) {
                            recvRedisMessage(stringStringStreamMessage);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 推送消息（其他人发的消息，推送到直播间）
    // 触发，如果redis里面有数据,则触发执行
    public void recvRedisMessage(StreamMessage<String, String> data) throws IOException {
        // 推送这条消息
        // 1、获取roomId
        // 2、根据房间找session
        String roomId = data.getBody().get("roomId");
        String content = data.getBody().get("content");
        String userId = data.getBody().get("userId");

        String message = userId + "说:" + content;
        List<WebSocketSession> webSocketSessionList = roomUserMap.get(roomId);
        for (WebSocketSession webSocketSession : webSocketSessionList) {
            webSocketSession.sendMessage(new TextMessage(message));
        }
    }
}
