package com.kramar.bot.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class ScheduledTasks {


    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0 0 8-10 * * *")
    public void reportCurrentTime() {
//        log.info("The time is now {}", dateFormat.format(new Date()));
    }
}
