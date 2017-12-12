package com.kramar.bot.repository;

import com.kramar.bot.dbo.TaskDbo;
import com.kramar.bot.dbo.UserDbo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskDbo, Long> {

    List<TaskDbo> findByDateAndOwnerOrderById(LocalDate date, UserDbo userDbo);

}
