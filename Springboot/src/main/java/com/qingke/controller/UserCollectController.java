package com.qingke.controller;

import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.UserCollect;
import com.qingke.service.UserCollectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/user-collect")
public class UserCollectController {

    private static final Logger logger = LoggerFactory.getLogger(UserCollectController.class);

    @Autowired
    private UserCollectService userCollectService;
    
    /**
     * 添加收藏
     */
    @OperationLogAnnotation(module = "我的收藏", type = "add", description = "添加收藏")
    @PostMapping("/add")
    public Result add(@RequestBody UserCollect userCollect) {
        try {
            // 检查是否已收藏
            boolean isStored = userCollectService.isStored(userCollect.getUserId(), userCollect.getTargetId(), userCollect.getTargetTable());
            if (isStored) {
                return Result.error("已经收藏过了");
            }
            userCollectService.add(userCollect);
            return Result.success("收藏成功");
        } catch (Exception e) {
            logger.error("收藏失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     */
    @OperationLogAnnotation(module = "我的收藏", type = "delete", description = "取消收藏")
    @DeleteMapping("/cancel")
    public Result cancel(
            @RequestParam Long userid,
            @RequestParam Long spid,
            @RequestParam String tablename
    ) {
        try {
            userCollectService.cancel(userid, spid, tablename);
            return Result.success("取消收藏成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result check(
            @RequestParam Long userid,
            @RequestParam Long spid,
            @RequestParam String tablename
    ) {
        try {
            boolean isStored = userCollectService.isStored(userid, spid, tablename);
            return Result.success(isStored);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的所有收藏
     */
    @GetMapping("/list")
    public Result list(@RequestParam Long userid) {
        try {
            List<UserCollect> list = userCollectService.findByUserid(userid);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据表名获取用户收藏
     */
    @GetMapping("/list-by-table")
    public Result<List<UserCollect>> listByTable(
            @RequestParam Long userid,
            @RequestParam String tablename
    ) {
        try {
            List<UserCollect> list = userCollectService.findByUseridAndTablename(userid, tablename);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}