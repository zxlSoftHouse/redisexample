package com.zxl.redisexample.peopleNearby.controller;

import com.zxl.redisexample.peopleNearby.service.PeopleNearbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 附近的人功能，LBS引用
 */
@RestController("/geo")
public class PeopleNearByController {

    @Autowired
    PeopleNearbyService peopleNearbyService;

    /**
     * 上传位置
     */
    @RequestMapping("/save")
    public void save(String userId, String longitude, String latitude) {
        Point point = new Point(Double.valueOf(longitude), Double.valueOf(latitude));
        peopleNearbyService.save(point, userId);
    }

    /**
     * 获取经纬度下附近的人
     */
    @RequestMapping("/near")
    public Object near(String longitude, String latitude) {
        Point point = new Point(Double.valueOf(longitude), Double.valueOf(latitude));
        return peopleNearbyService.nearby(point);
    }

    /**
     * 计算两个点之间的距离
     */
    @RequestMapping("/distance")
    public double distance(String longitude1, String latitude1, String longitude2, String latitude2) {
        Point point1 = new Point(Double.valueOf(longitude1), Double.valueOf(latitude1));
        Point point2 = new Point(Double.valueOf(longitude2), Double.valueOf(latitude2));
        return peopleNearbyService.caluatePosition(point1, point2);
    }
}
