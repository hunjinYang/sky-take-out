package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    List<SetmealVO> list(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    Setmeal getById(Long id);

    /**
     * 修改套餐
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 用户端根据分类和状态查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> listByCategoryIdAndStatus(Setmeal setmeal);

    /**
     * 根据状态统计套餐数量
     * @param map
     * @return
     */
    @Select("select count(id) from setmeal where status = #{status}")
    Integer countByMap(Map map);
}
