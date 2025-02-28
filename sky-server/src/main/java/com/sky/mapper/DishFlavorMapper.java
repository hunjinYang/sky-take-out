package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入菜品口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 批量删除菜品口味
     * @param dishIds
     */
    void deleteByIds(List<Long> dishIds);

    /**
     * 根据菜品id查询口味
     * @param id
     * @return
     */
    List<DishFlavor> getByDishId(Long id);

    /**
     * 根据菜品id删除口味
     * @param dishId
     */
    void deleteByDishId(Long dishId);
}
