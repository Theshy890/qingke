package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.PlantKnowledge;
import com.qingke.service.PlantKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 养护知识控制器
 */
@RestController
@RequestMapping("/api/plant-knowledge")
public class PlantKnowledgeController {
    
    @Autowired
    private PlantKnowledgeService plantKnowledgeService;
    
    /**
     * 查询所有养护知识
     */
    @GetMapping("/list")
    public Result<List<PlantKnowledge>> list() {
        try {
            List<PlantKnowledge> list = plantKnowledgeService.findAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询养护知识
     */
    @GetMapping("/page")
    public Result<PageInfo<PlantKnowledge>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String plantName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String orderBy
    ) {
        try {
            PageInfo<PlantKnowledge> pageInfo = plantKnowledgeService.findByPage(pageNum, pageSize, plantName, categoryName, orderBy);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询养护知识
     */
    @GetMapping("/{id}")
    public Result<PlantKnowledge> getById(@PathVariable Long id) {
        try {
            PlantKnowledge plantKnowledge = plantKnowledgeService.findById(id);
            if (plantKnowledge != null) {
                plantKnowledgeService.increaseClicknum(id);
            }
            return Result.success(plantKnowledge);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 新增养护知识
     */
    @OperationLogAnnotation(module = "养护知识", type = "add", description = "新增养护知识")
    @PostMapping("/add")
    public Result<String> add(@RequestBody PlantKnowledge plantKnowledge) {
        try {
            plantKnowledgeService.add(plantKnowledge);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新养护知识
     */
    @OperationLogAnnotation(module = "养护知识", type = "update", description = "修改养护知识")
    @PutMapping("/update")
    public Result<String> update(@RequestBody PlantKnowledge plantKnowledge) {
        try {
            plantKnowledgeService.update(plantKnowledge);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID删除养护知识
     */
    @OperationLogAnnotation(module = "养护知识", type = "delete", description = "删除养护知识")
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        try {
            plantKnowledgeService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除养护知识
     */
    @OperationLogAnnotation(module = "养护知识", type = "delete", description = "批量删除养护知识")
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        try {
            plantKnowledgeService.deleteBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 增加收藏数量
     */
    @OperationLogAnnotation(module = "养护知识", type = "update", description = "收藏知识文章")
    @PutMapping("/{id}/storeup")
    public Result<String> increaseStoreupnum(@PathVariable Long id) {
        try {
            plantKnowledgeService.increaseStoreupnum(id);
            return Result.success("收藏成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 减少收藏数量
     */
    @OperationLogAnnotation(module = "养护知识", type = "update", description = "取消收藏文章")
    @PutMapping("/{id}/unstoreup")
    public Result<String> decreaseStoreupnum(@PathVariable Long id) {
        try {
            plantKnowledgeService.decreaseStoreupnum(id);
            return Result.success("取消收藏成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}