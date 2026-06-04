package com.project.www.service.impl;

import com.project.www.entity.Course;
import com.project.www.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl implements CourseService {

    @Override
    public Object findById(Long id) {
        return getCourseById(id);
    }

    @Override
    public Course getCourseById(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setCourseName("Course " + id);
        return course;
    }
}
