package com.sky.aspect;

import com.sky.anno.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 公共字段自动填充切面类
 */
@Slf4j
@Component
@Aspect
public class AutoFillAspect {

    @Before("@annotation(com.sky.anno.AutoFill)")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("autoFill运行，公共字段自动填充...");
        //1. 获取目标方法上的注解，并拿到注解里的属性值
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;  //得到方法签名
        Method method = methodSignature.getMethod();//得到方法对象
        AutoFill annotation = method.getAnnotation(AutoFill.class);
        OperationType value = annotation.value();

        //2. 获取到目标方法的参数对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];


        if(value == OperationType.INSERT){
            //通过反射去补充属性值
            //3. 判断注解中属性值，如果是INSERT，就补充四个字段值(createTime,updateTime,createUser,updateUser)
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setCreateTime.invoke(entity,LocalDateTime.now());
            setUpdateTime.invoke(entity,LocalDateTime.now());
            setCreateUser.invoke(entity,BaseContext.getCurrentId());
            setUpdateUser.invoke(entity,BaseContext.getCurrentId());

        }else if(value == OperationType.UPDATE){
            //4. 判断注解中属性值，如果是UPDATE，就补充两个字段值(updateTime,updateUser)
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(entity,LocalDateTime.now());
            setUpdateUser.invoke(entity,BaseContext.getCurrentId());
        }

    }
}
