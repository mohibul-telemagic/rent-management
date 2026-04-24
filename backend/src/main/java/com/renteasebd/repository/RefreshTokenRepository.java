package com.renteasebd.repository;

import com.renteasebd.domain.auth.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("delete from RefreshToken rt where rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from RefreshToken rt where rt.id = :id and rt.user.id = :userId")
    int deleteByIdAndUserId(@Param("id") String id, @Param("userId") Long userId);
}
