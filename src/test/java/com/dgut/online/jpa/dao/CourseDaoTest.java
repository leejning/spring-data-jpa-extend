package com.dgut.online.jpa.dao;


import com.dgut.online.jpa.JpaApplication;
import com.dgut.online.jpa.bean.Course;
import com.dgut.online.jpa.queryDTO.CourseQueryDTO;
import org.assertj.core.util.Lists;
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
import java.util.Collections;
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
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        List<Course> data = courseDao.findAll(CourseQueryDTO.getWhere(queryDTO));
        data.forEach(System.out::println);
    }

    /**
     * 加分页
     */
    @Test
    public void queryWithPage(){
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 3, sort);
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        Page<Course> data = courseDao.findAll(CourseQueryDTO.getWhere(queryDTO),pageable);
        data.forEach(System.out::println);
    }

    /**
     * in 范围查询 查询课程类型是："编程语言"或者"web"的课程
     */
    @Test
    public void queryWithIn(){
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        queryDTO.setWithIn(true);
        queryDTO.setInField("courseType");
        queryDTO.setInValue(Arrays.asList("编程语言","web"));
        List<Course> data = courseDao.findAll(CourseQueryDTO.getWhere(queryDTO));
        data.forEach(System.out::println);
    }

    /**
     * in 范围查询 加 分页
     */
    @Test
    public void queryWithInAndPage(){
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, 3, sort);
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        queryDTO.setWithIn(true);
        queryDTO.setInField("courseType");
            queryDTO.setInValue(Arrays.asList("编程语言","web"));
        Page<Course> page = courseDao.findAll(CourseQueryDTO.getWhere(queryDTO),pageable);
        page.forEach(System.out::println);
    }
}
