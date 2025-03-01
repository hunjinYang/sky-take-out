package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    /**
     * 根据菜品id查询套餐数量
     * @param dishIds
     * @return
     */
    Integer countByDishId(List<Long> dishIds);

    /**
     * 批量添加
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐ids批量删除
     * @param setmealIds
     */
    void deleteByIds(List<Long> setmealIds);

    /**
     * 根据套餐id查询
     * @return
     */
    List<SetmealDish> getBySetmealId(Long setmealId);

    /**
     * 根据单个套餐id删除
     * @param setmealId
     */
    void deleteBySetmealId(Long setmealId);
}
