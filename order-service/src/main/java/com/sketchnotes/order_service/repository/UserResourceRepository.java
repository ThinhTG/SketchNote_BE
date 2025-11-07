package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.entity.UserResource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserResourceRepository  extends JpaRepository<UserResource,Long> {
    // lấy tất cả resource mà đang có trong library của họ ( trả thông tin resource và cả list items của resource do)

    // tạo 1 user_resource



    List<UserResource> findByUserIdAndActiveTrue(Long userId);
    boolean existsByUserIdAndResourceTemplateId(Long userId, Long resourceTemplateId);
    boolean existsByUserIdAndResourceTemplateIdAndActiveTrue(Long userId, Long resourceTemplateId);
    Optional<UserResource> findFirstByUserIdAndResourceTemplateId(Long userId, Long resourceTemplateId);

    @Query("select ur.resourceTemplateId from UserResource ur where ur.userId = ?1 and ur.active = true")
    List<Long> findActiveTemplateIdsByUserId(Long userId);
}
