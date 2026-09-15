package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.PlantMaintainRecord;
import com.qingke.service.PlantMaintainRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plant-maintain-record")
public class PlantMaintainRecordController {

    private static final Logger logger = LoggerFactory.getLogger(PlantMaintainRecordController.class);

    @Autowired
    private PlantMaintainRecordService plantMaintainRecordService;
    
    /**
     * 查询所有养护记录（管理员用）
     */
    @GetMapping("/all")
    public Result getAll() {
        try {
            List<PlantMaintainRecord> list = plantMaintainRecordService.findAll();
            return Result.success(list);
        } catch (Exception e) {
            logger.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加养护记录
     */
    @OperationLogAnnotation(module = "养护记录", type = "add", description = "新增养护记录")
    @PostMapping("/add")
    public Result add(@RequestBody PlantMaintainRecord record) {
        try {
            plantMaintainRecordService.add(record);
            return Result.success("添加成功");
        } catch (Exception e) {
            logger.error("添加失败", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询养护记录
     */
    @GetMapping("/page")
    public Result getPage(
            @RequestParam String userAccount,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageInfo<PlantMaintainRecord> pageInfo = plantMaintainRecordService.findByPage(pageNum, pageSize, userAccount);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询所有养护记录
     */
    @GetMapping("/list")
    public Result getList(@RequestParam String userAccount) {
        try {
            List<PlantMaintainRecord> list = plantMaintainRecordService.findByZhanghao(userAccount);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            PlantMaintainRecord record = plantMaintainRecordService.findById(id);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新养护记录
     */
    @OperationLogAnnotation(module = "养护记录", type = "update", description = "修改养护记录")
    @PutMapping("/update")
    public Result update(@RequestBody PlantMaintainRecord record) {
        try {
            plantMaintainRecordService.update(record);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除养护记录
     */
    @OperationLogAnnotation(module = "养护记录", type = "delete", description = "删除养护记录")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        try {
            plantMaintainRecordService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除
     */
    @OperationLogAnnotation(module = "养护记录", type = "delete", description = "批量删除养护记录")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        try {
            plantMaintainRecordService.deleteBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据绿植名称查询养护记录
     */
    @GetMapping("/plant")
    public Result<List<PlantMaintainRecord>> getByPlant(
            @RequestParam String plantName,
            @RequestParam String userAccount
    ) {
        try {
            List<PlantMaintainRecord> list = plantMaintainRecordService.findByPlantAndUser(plantName, userAccount);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}