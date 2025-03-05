package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @return
     */
    public User getByOpenid(String openid);

    /**
     * 添加用户
     * @param user
     */
    void insert(User user);

    /**
     * 查询用户信息
     * @param  id
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);

    /**
     * 统计每日新增用户
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
