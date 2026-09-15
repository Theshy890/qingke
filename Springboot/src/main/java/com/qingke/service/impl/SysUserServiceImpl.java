package com.qingke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.SysUser;
import com.qingke.mapper.SysUserMapper;
import com.qingke.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public SysUser findByZh(String zh) { return sysUserMapper.findByZh(zh); }

    @Override
    public SysUser findByPhone(String phone) { return sysUserMapper.findByPhone(phone); }

    @Override
    public void increasePasswordWrongNum(Long id) { sysUserMapper.increasePasswordWrongNum(id); }

    @Override
    public void resetPasswordWrongNum(Long id) { sysUserMapper.resetPasswordWrongNum(id); }

    @Override
    public void updatePassword(Long id, String password) { sysUserMapper.updatePassword(id, password); }

    @Override
    public List<SysUser> findAll() { return sysUserMapper.findAll(); }

    @Override
    public PageInfo<SysUser> findByPage(Integer pageNum, Integer pageSize, String zh, String name, String role) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> list = sysUserMapper.findByCondition(zh, name, role);
        return new PageInfo<>(list);
    }

    @Override
    public SysUser findById(Long id) { return sysUserMapper.findById(id); }

    @Override
    public void add(SysUser user) { sysUserMapper.insert(user); }

    @Override
    public void update(SysUser user) { sysUserMapper.updateById(user); }

    @Override
    public void deleteById(Long id) { sysUserMapper.deleteById(id); }

    @Override
    public void deleteBatch(List<Long> ids) { sysUserMapper.deleteBatch(ids); }

    @Override
    public void updateStatus(Long id, Integer status) { sysUserMapper.updateStatus(id, status); }

    @Override
    public void updateLastLoginTime(Long id) { sysUserMapper.updateLastLoginTime(id); }

    @Override
    public void addPoints(Long id, int delta) { sysUserMapper.addPoints(id, delta); }

    @Override
    public boolean deductPoints(Long id, int cost) { return sysUserMapper.deductPoints(id, cost) > 0; }
}
