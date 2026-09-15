package com.qingke.service;

import com.qingke.entity.SysUser;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface SysUserService {
    SysUser findByZh(String zh);
    SysUser findByPhone(String phone);
    void increasePasswordWrongNum(Long id);
    void resetPasswordWrongNum(Long id);
    void updatePassword(Long id, String password);
    List<SysUser> findAll();
    PageInfo<SysUser> findByPage(Integer pageNum, Integer pageSize, String zh, String name, String role);
    SysUser findById(Long id);
    void add(SysUser user);
    void update(SysUser user);
    void deleteById(Long id);
    void deleteBatch(List<Long> ids);
    void updateStatus(Long id, Integer status);
    void updateLastLoginTime(Long id);
    void addPoints(Long id, int delta);
    boolean deductPoints(Long id, int cost);
}
