package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     * @param employee
     */
    @AutoFill(OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    List<Employee> list(EmployeePageQueryDTO employeePageQueryDTO);


    /**
     * 启用禁用
     * @param employee
     */
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Select("select id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user from employee where id=#{id}")
    Employee getById(Long id);
}
