package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.entity.ResourceTemplateItem;
import com.sketchnotes.order_service.entity.ResourcesTemplateImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceImageRepository extends JpaRepository<ResourcesTemplateImage, Long> {
    Optional<List<ResourcesTemplateImage>> findByResourceTemplateAndIsThumbnailTrue(ResourceTemplate item);
}
