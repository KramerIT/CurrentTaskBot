package com.kramar.bot.repository;

import com.kramar.bot.dbo.UserDbo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDbo, Long> {

    Optional<UserDbo> findByPhone(String phone);

    Optional<UserDbo> findByName(String name);

    UserDbo findByTelegramId(Long id);
}
