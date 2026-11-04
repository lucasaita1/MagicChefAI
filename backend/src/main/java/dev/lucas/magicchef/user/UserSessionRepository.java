package dev.lucas.magicchef.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    @Modifying
    @Query("delete from UserSession s where s.expiresAt < :now")
    int deleteExpired(Instant now);
}
