package com.dgut.online.jpa.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @ClassName Course
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/14 0014
 * @Version V1.0
 **/
@Data
@ToString
@NoArgsConstructor
@Entity
@Table(name = "ic_course")
public class Course implements Serializable {
    private static final long serialVersionUID = -435853666952892717L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseName;
    private String courseType;
    private String teacherName;
    private Long teacherId;
    private String courseCover;

    public Course(String courseName, String teacherName) {
        this.courseName = courseName;
        this.teacherName = teacherName;
    }

    public Course(Long id, String courseName, String teacherName) {
        this.id = id;
        this.courseName = courseName;
        this.teacherName = teacherName;
    }

    public Course(Long id, String courseName, String courseType, String teacherName) {
        this.id = id;
        this.courseName = courseName;
        this.courseType = courseType;
        this.teacherName = teacherName;
    }
}
