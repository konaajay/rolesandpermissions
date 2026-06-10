package com.project.www.repository;

import com.project.www.enums.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.www.marketing.entity.PushNotification;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findAllByOrderByCreatedAtDesc();
}
