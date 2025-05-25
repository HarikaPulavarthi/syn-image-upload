package com.synchrony.cloudinary.repository;

import com.synchrony.cloudinary.entity.Image;
import com.synchrony.cloudinary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findById(Long id);
}
