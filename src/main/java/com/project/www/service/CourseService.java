package com.project.www.service;

import com.project.www.entity.Course;

public interface CourseService {
    Object findById(Long id);
    Course getCourseById(Long id);
}
