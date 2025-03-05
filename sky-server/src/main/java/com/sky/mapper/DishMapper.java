package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 添加菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    //@Options(useGeneratedKeys = true,keyProperty = "id") //获取主键值，并且赋值给id属性,若sql语句写在xml中，则此处不能使用
    void insert(Dish dish);


    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    List<DishVO> list(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    Dish getById(Long id);

    /**
     * 修改菜品
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void updateById(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 用户端根据categoryId和status查询菜品
     * @param dish
     * @return
     */
    List<Dish> getByCategoryIdAndStatus(Dish dish);

    /**
     * 统计启售或停售的菜品数量
     * @param map
     * @return
     */
    @Select("select count(id) from dish where status = #{status}")
    Integer countByMap(Map map);
}
