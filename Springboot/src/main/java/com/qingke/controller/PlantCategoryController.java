package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.PlantCategory;
import com.qingke.service.PlantCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plant-category")
public class PlantCategoryController {
    
    @Autowired
    private PlantCategoryService plantCategoryService;
    
    /**
     * 查询所有绿植种类
     */
    @GetMapping("/list")
    public Result<List<PlantCategory>> list() {
        try {
            List<PlantCategory> list = plantCategoryService.findAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询绿植种类
     */
    @GetMapping("/page")
    public Result<PageInfo<PlantCategory>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String categoryName
    ) {
        try {
            PageInfo<PlantCategory> pageInfo = plantCategoryService.findByPage(pageNum, pageSize, categoryName);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询绿植种类
     */
    @GetMapping("/{id}")
    public Result<PlantCategory> getById(@PathVariable Long id) {
        try {
            PlantCategory plantCategory = plantCategoryService.findById(id);
            return Result.success(plantCategory);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 新增绿植种类
     */
    @OperationLogAnnotation(module = "绿植种类", type = "add", description = "新增绿植种类")
    @PostMapping("/add")
    public Result<String> add(@RequestBody PlantCategory plantCategory) {
        try {
            plantCategoryService.add(plantCategory);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新绿植种类
     */
    @OperationLogAnnotation(module = "绿植种类", type = "update", description = "修改绿植种类")
    @PutMapping("/update")
    public Result<String> update(@RequestBody PlantCategory plantCategory) {
        try {
            plantCategoryService.update(plantCategory);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID删除绿植种类
     */
    @OperationLogAnnotation(module = "绿植种类", type = "delete", description = "删除绿植种类")
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        try {
            plantCategoryService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除绿植种类
     */
    @OperationLogAnnotation(module = "绿植种类", type = "delete", description = "批量删除绿植种类")
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        try {
            plantCategoryService.deleteBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
