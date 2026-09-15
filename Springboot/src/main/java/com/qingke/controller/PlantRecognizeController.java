package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.AiRecognitionRequest;
import com.qingke.entity.AiRecognitionResponse;
import com.qingke.entity.PlantRecognize;
import com.qingke.service.PlantRecognizeService;
import com.qingke.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plant-recognize")
public class PlantRecognizeController {

    private static final Logger logger = LoggerFactory.getLogger(PlantRecognizeController.class);

    @Autowired
    private PlantRecognizeService plantRecognizeService;
    
    @Autowired
    private SysUserService sysUserService;
    
    /**
     * AI识别绿植病害
     */
    @OperationLogAnnotation(module = "AI绿植识别", type = "add", description = "AI识别绿植")
    @PostMapping("/recognize")
    public Result<AiRecognitionResponse> recognize(@RequestBody AiRecognitionRequest request) {
        try {
            if (request.getUserid() == null) {
                return Result.error("请先登录");
            }
            
            // 积分兑换机制：每识别一次消耗10积分
            boolean success = sysUserService.deductPoints(request.getUserid(), 10);
            if (!success) {
                return Result.error("AI识别次数不足，请通过每日登录获取积分后兑换次数");
            }
            
            AiRecognitionResponse response;
            try {
                response = plantRecognizeService.recognizePlant(request);
            } catch (Exception e) {
                // 识别失败，返还消耗的积分
                sysUserService.addPoints(request.getUserid(), 10);
                throw e;
            }
            
            // 自动保存识别记录
            if (request.getUserid() != null) {
                PlantRecognize record = new PlantRecognize();
                record.setUserId(request.getUserid());
                record.setImgUrl(request.getImage());
                record.setPlantName(response.getPlantName());
                record.setPlantIntro(response.getPlantIntro());
                record.setDiseaseName(response.getDiseaseName());
                record.setDiseaseDesc(response.getDiseaseDesc());
                record.setTreatSuggest(response.getTreatment());
                plantRecognizeService.saveRecord(record);
            }
            
            return Result.success(response);
        } catch (Exception e) {
            logger.error("识别失败", e);
            return Result.error("识别失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取识别历史（分页）
     */
    @GetMapping("/history")
    public Result<PageInfo<PlantRecognize>> getHistory(
            @RequestParam Long userid,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageInfo<PlantRecognize> pageInfo = plantRecognizeService.findByPage(pageNum, pageSize, userid);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有识别记录（管理员用）
     */
    @GetMapping("/all")
    public Result<List<PlantRecognize>> getAll() {
        try {
            List<PlantRecognize> list = plantRecognizeService.findAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取识别历史列表（按用户）
     */
    @GetMapping("/list")
    public Result<List<PlantRecognize>> list(@RequestParam Long userid) {
        try {
            List<PlantRecognize> list = plantRecognizeService.findByUserid(userid);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询识别记录
     */
    @GetMapping("/{id}")
    public Result<PlantRecognize> getById(@PathVariable Long id) {
        try {
            PlantRecognize record = plantRecognizeService.findById(id);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除识别记录
     */
    @OperationLogAnnotation(module = "AI绿植识别", type = "delete", description = "删除识别记录")
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        try {
            plantRecognizeService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除
     */
    @OperationLogAnnotation(module = "AI绿植识别", type = "delete", description = "批量删除识别记录")
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        try {
            plantRecognizeService.deleteBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}