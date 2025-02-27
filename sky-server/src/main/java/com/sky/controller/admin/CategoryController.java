package com.sky.controller.admin;



import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "分类相关接口")
@Slf4j
@RequestMapping("/admin/category")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询
     * @return
     */
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询：{}",categoryPageQueryDTO);
        PageResult p = categoryService.page(categoryPageQueryDTO);
        return Result.success(p);
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @ApiOperation("新增分类")
    @PostMapping
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 修改分类
     * @return
     */
    @ApiOperation("修改分类")
    @PutMapping
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类：{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用禁用
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("启用禁用")
    @PostMapping("/status/{status}")
    public Result<String> setStatus(@PathVariable Integer status,Long id){
        log.info("启用禁用分类：{},{}",status,id);
        categoryService.setStatus(id,status);
        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @ApiOperation("删除分类")
    @DeleteMapping
    public Result<String> delete(Long id){
        log.info("删除分类：{}",id);
        categoryService.delete(id);
        return  Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @ApiOperation("根据类型查询分类")
    @GetMapping("/list")
    public Result<List<Category>> listByType(Integer type){
        log.info("根据类型分类：{}",type);
        List<Category> categoryList = categoryService.listByType(type);
        return Result.success(categoryList);
    }
}
