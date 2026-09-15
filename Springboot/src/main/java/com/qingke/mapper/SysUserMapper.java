package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    SysUser findByZh(@Param("zh") String zh);
    SysUser findByPhone(@Param("phone") String phone);
    void increasePasswordWrongNum(@Param("id") Long id);
    void resetPasswordWrongNum(@Param("id") Long id);
    void updatePassword(@Param("id") Long id, @Param("password") String password);
    List<SysUser> findAll();
    List<SysUser> findByCondition(@Param("zh") String zh, @Param("name") String name, @Param("role") String role);
    SysUser findById(@Param("id") Long id);
    void deleteBatch(@Param("ids") List<Long> ids);
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
    void updateLastLoginTime(@Param("id") Long id);
    void addPoints(@Param("id") Long id, @Param("delta") int delta);
    int deductPoints(@Param("id") Long id, @Param("cost") int cost);
}
