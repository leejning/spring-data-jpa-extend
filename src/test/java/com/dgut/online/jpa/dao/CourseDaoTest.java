package com.dgut.online.jpa.dao;


import com.dgut.online.jpa.JpaApplication;
import com.dgut.online.jpa.bean.Course;
import com.dgut.online.jpa.bean.CourseDTO;
import com.dgut.online.jpa.querybuilder.CourseEntityQueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@SpringBootTest(classes = JpaApplication.class)
@RunWith(SpringRunner.class)
public class CourseDaoTest {
    @Autowired
    private CourseDao courseDao;

    /**
     * 只查询部分字段
     */
    @Test
    public void querySelectSomefield(){
        CourseEntityQueryBuilder queryDTO = new CourseEntityQueryBuilder();
        List<Course> data = courseDao.findAll(CourseEntityQueryBuilder.getWhere(queryDTO));
        data.forEach(System.out::println);
    }

    /**
     * 只查询部分字段,结果自动封装到DTO
     */
    @Test
    public void querySelectSomefield2() {
        CourseEntityQueryBuilder queryDTO = new CourseEntityQueryBuilder();
        List<CourseDTO> data = (List<CourseDTO>) courseDao.findAll(CourseEntityQueryBuilder.getWhere(queryDTO), CourseDTO.class);
        data.forEach(System.out::println);
    }
    /**
     * 只查询部分字段,结果自动封装到DTO,加分页
     */
    @Test
    public void querySelectSomefield2WithPage() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 3, sort);
        CourseEntityQueryBuilder queryDTO = new CourseEntityQueryBuilder();
        Page<CourseDTO> data = (Page<CourseDTO>) courseDao.findAll(CourseEntityQueryBuilder.getWhere(queryDTO), pageable, CourseDTO.class);
        data.forEach(System.out::println);
    }


    /**
     * 加分页
     */
    @Test
    public void queryWithPage(){
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 3, sort);
        CourseEntityQueryBuilder queryDTO = new CourseEntityQueryBuilder();
        Page<Course> data = courseDao.findAll(CourseEntityQueryBuilder.getWhere(queryDTO),pageable);
        data.forEach(System.out::println);
    }

    /**
     * in 范围查询 查询课程类型是："编程语言"或者"web"的课程
     */
    @Test
    public void queryWithIn(){
        CourseEntityQueryBuilder queryDTO = new CourseEntityQueryBuilder();
        queryDTO.setWithIn(true);
        queryDTO.setInField("courseType");
        queryDTO.setInValue(Arrays.asList("编程语言","web"));
        List<Course> data = courseDao.findAll(CourseEntityQueryBuilder.getWhere(queryDTO));
        data.forEach(System.out::println);
    }

    /**
     * in 范围查询 加 分页
     */
    @Test
    public void queryWithInAndPage(){
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, 3, sort);
        CourseEntityQueryBuilder queryDTO = new CourseEntityQueryBuilder();
        queryDTO.setWithIn(true);
        queryDTO.setInField("courseType");
            queryDTO.setInValue(Arrays.asList("编程语言","web"));
        Page<Course> page = courseDao.findAll(CourseEntityQueryBuilder.getWhere(queryDTO),pageable);
        page.forEach(System.out::println);
    }
}
