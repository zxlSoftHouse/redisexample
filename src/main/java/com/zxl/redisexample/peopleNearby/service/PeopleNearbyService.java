package com.zxl.redisexample.peopleNearby.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 通过redis的geo数据结构实现附近的人功能，基于zset实现的数据类型
 */
@Service
public class PeopleNearbyService {

    private static final String GEO_KEY = "user_geo";

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 上传位置
     * redis中操作
     * geoadd 添加地理位置坐标
     */
    public void save(Point point, String userId) {
        redisTemplate.opsForGeo().add(GEO_KEY, new RedisGeoCommands.GeoLocation(userId, point));
    }

    /**
     * 附近的人
     * @param point 用户自己的位置
     * @return 所有的附件人的集合
     */
    public GeoResults<RedisGeoCommands.GeoLocation> nearby(Point point) {
        // 半径3000米范围
        Distance distance = new Distance(3000, RedisGeoCommands.DistanceUnit.METERS);
        Circle circle = new Circle(point, distance);
        // 附件5个人，根据需求变化
        RedisGeoCommands.GeoRadiusCommandArgs geoRadiusCommandArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(5);
        GeoResults<RedisGeoCommands.GeoLocation> geoResults = redisTemplate.opsForGeo().radius(GEO_KEY, circle,geoRadiusCommandArgs);
        return geoResults;
    }

    /**
     * 计算两个成员之间的距离
     */
    public double caluatePosition(Point point, Point point1) {
        // 两个点之间的距离
        Distance distance = redisTemplate.opsForGeo().distance(GEO_KEY, point, point1, RedisGeoCommands.DistanceUnit.METERS);
        return distance.getValue();
    }
}
