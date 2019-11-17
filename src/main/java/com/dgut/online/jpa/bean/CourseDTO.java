package com.dgut.online.jpa.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName CourseDTO
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/17 0017
 * @Version V1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {

    private Long id;
    private String courseName;
    private String courseType;
    private String teacherName;
}
