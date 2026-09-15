package com.qingke.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.qingke.common.JwtUtil;
import com.qingke.common.Result;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.entity.LoginRequest;
import com.qingke.entity.LoginResponse;
import com.qingke.entity.SysUser;
import com.qingke.service.SysUserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys-user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @OperationLogAnnotation(module = "用户账号", type = "login", description = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        return performLogin(loginRequest, null); // 不限制role，兼容旧接口
    }

    @OperationLogAnnotation(module = "管理员账号", type = "login", description = "管理员登录")
    @PostMapping("/admin-login")
    public Result<Map<String, Object>> adminLogin(@RequestBody LoginRequest loginRequest) {
        return performLogin(loginRequest, "admin"); // 强制role=admin
    }

    @OperationLogAnnotation(module = "用户账号", type = "login", description = "APP用户登录")
    @PostMapping("/app-login")
    public Result<Map<String, Object>> appLogin(@RequestBody LoginRequest loginRequest) {
        return performLogin(loginRequest, "user"); // 强制role=user
    }

    private Result<Map<String, Object>> performLogin(LoginRequest loginRequest, String requiredRole) {
        try {
            if (loginRequest.getZh() == null || loginRequest.getZh().trim().isEmpty()) return Result.error("账号不能为空");
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) return Result.error("密码不能为空");

            SysUser user = sysUserService.findByZh(loginRequest.getZh());
            if (user == null) return Result.error("账号不存在");

            // Role隔离校验
            if (requiredRole != null && !requiredRole.equals(user.getRole())) {
                if ("admin".equals(requiredRole)) {
                    return Result.error("该账号无管理员权限，请使用管理员账号登录");
                } else if ("user".equals(requiredRole)) {
                    return Result.error("管理员账号禁止登录APP，请使用普通用户账号");
                }
            }

            String encryptedPassword = DigestUtil.md5Hex(loginRequest.getPassword());
            if (!encryptedPassword.equals(user.getPassword())) {
                sysUserService.increasePasswordWrongNum(user.getId());
                if (user.getPasswordWrongNum() != null && user.getPasswordWrongNum() >= 4) {
                    sysUserService.updateStatus(user.getId(), 1);
                    return Result.error("密码错误次数过多，账号已被锁定，请联系管理员");
                }
                return Result.error("密码错误");
            }

            if (user.getStatus() != null && user.getStatus() == 1) return Result.error("账号已被禁用，请联系管理员");

            sysUserService.resetPasswordWrongNum(user.getId());
            sysUserService.addPoints(user.getId(), 5);
            sysUserService.updateLastLoginTime(user.getId());
            user = sysUserService.findById(user.getId());

            String token = JwtUtil.generateToken(user.getId(), user.getZh(), user.getRole());
            user.setPassword(null);

            LoginResponse response = new LoginResponse(token, user);
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("token", response.getToken());
            data.put("user", response.getUserInfo());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @OperationLogAnnotation(module = "用户账号", type = "add", description = "用户注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        try {
            if (user.getZh() == null || user.getZh().trim().isEmpty()) return Result.error("账号不能为空");
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) return Result.error("密码不能为空");
            if (user.getName() == null || user.getName().trim().isEmpty()) return Result.error("姓名不能为空");

            SysUser existUser = sysUserService.findByZh(user.getZh());
            if (existUser != null) return Result.error("账号已存在");

            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                SysUser existPhone = sysUserService.findByPhone(user.getPhone());
                if (existPhone != null) return Result.error("手机号已被注册");
            }

            user.setPassword(DigestUtil.md5Hex(user.getPassword()));
            if (user.getRole() == null || user.getRole().trim().isEmpty()) user.setRole("user");
            user.setStatus(0);
            user.setPasswordWrongNum(0);
            user.setRegisterTime(new java.util.Date());
            user.setCreateTime(new java.util.Date());

            sysUserService.add(user);
            sysUserService.addPoints(user.getId(), 50);

            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    @OperationLogAnnotation(module = "用户账号", type = "update", description = "重置密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody SysUser user) {
        try {
            if (user.getZh() == null || user.getZh().trim().isEmpty()) return Result.error("账号不能为空");
            SysUser existUser = sysUserService.findByZh(user.getZh());
            if (existUser == null) return Result.error("账号不存在");

            // 密保问题功能已移除，使用邮箱验证码重置密码
            // 这里可以添加邮箱验证码校验逻辑

            String newPassword = DigestUtil.md5Hex(user.getPassword());
            sysUserService.updatePassword(existUser.getId(), newPassword);
            sysUserService.resetPasswordWrongNum(existUser.getId());
            sysUserService.updateStatus(existUser.getId(), 0);
            return Result.success("密码重置成功，请重新登录");
        } catch (Exception e) {
            return Result.error("密码重置失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        try {
            List<SysUser> list = sysUserService.findAll();
            list.forEach(u -> u.setPassword(null));
            return Result.success(list);
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @GetMapping("/page")
    public Result<PageInfo<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String zh,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role
    ) {
        try {
            // 后台管理页面用户列表：强制只查询role=user的普通用户，隐藏管理员账号
            if (role == null) role = "user";
            return Result.success(sysUserService.findByPage(pageNum, pageSize, zh, name, role));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        try { return Result.success(sysUserService.findById(id)); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "用户管理", type = "add", description = "新增用户")
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysUser user) {
        try { sysUserService.add(user); return Result.success("添加成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "用户管理", type = "update", description = "修改用户信息")
    @PutMapping("/update")
    public Result<String> update(@RequestBody SysUser user) {
        try { sysUserService.update(user); return Result.success("更新成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "用户账号", type = "update", description = "修改密码")
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody SysUser user) {
        try {
            if (user.getId() == null) return Result.error("用户ID不能为空");
            if (user.getOldPassword() == null || user.getOldPassword().trim().isEmpty()) return Result.error("请输入原密码");
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) return Result.error("请输入新密码");

            SysUser existUser = sysUserService.findById(user.getId());
            if (existUser == null) return Result.error("用户不存在");
            if (!user.getOldPassword().equals(existUser.getPassword())) return Result.error("原密码不正确");
            if (user.getOldPassword().equals(user.getPassword())) return Result.error("新密码不能与原密码相同");

            sysUserService.updatePassword(user.getId(), user.getPassword());
            return Result.success("密码修改成功");
        } catch (Exception e) { return Result.error("密码修改失败：" + e.getMessage()); }
    }

    @OperationLogAnnotation(module = "用户管理", type = "delete", description = "删除用户")
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        try { sysUserService.deleteById(id); return Result.success("删除成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "用户管理", type = "delete", description = "批量删除用户")
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        try { sysUserService.deleteBatch(ids); return Result.success("批量删除成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "用户管理", type = "update", description = "修改用户状态")
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try { sysUserService.updateStatus(id, status); return Result.success("更新状态成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "用户账号", type = "update", description = "每日签到")
    @PostMapping("/{id}/signIn")
    public Result<String> signIn(@PathVariable Long id, @RequestParam(defaultValue = "10") int points) {
        try {
            sysUserService.addPoints(id, points);
            return Result.success("签到成功");
        } catch (Exception e) {
            return Result.error("签到失败：" + e.getMessage());
        }
    }

    @OperationLogAnnotation(module = "用户账号", type = "update", description = "更新隐私设置")
    @PutMapping("/privacySettings")
    public Result<String> updatePrivacySettings(@RequestBody SysUser user) {
        try {
            if (user.getId() == null) return Result.error("用户ID不能为空");

            SysUser existUser = sysUserService.findById(user.getId());
            if (existUser == null) return Result.error("用户不存在");

            // 只更新隐私相关字段
            existUser.setProfileVisibility(user.getProfileVisibility());
            existUser.setCareRecordsPublic(user.getCareRecordsPublic());
            existUser.setFavoritesPublic(user.getFavoritesPublic());

            sysUserService.update(existUser);
            return Result.success("隐私设置更新成功");
        } catch (Exception e) {
            return Result.error("隐私设置更新失败：" + e.getMessage());
        }
    }
}
