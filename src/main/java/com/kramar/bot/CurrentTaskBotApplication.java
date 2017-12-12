package com.kramar.bot;

import com.kramar.bot.bot.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Slf4j
@EnableScheduling
@SpringBootApplication
@EntityScan(basePackages = "com.kramar")
public class CurrentTaskBotApplication {

	static {
		ApiContextInitializer.init();
	}

	@Autowired
	private TelegramBot bot;

	public static void main(String[] args) {
		SpringApplication.run(CurrentTaskBotApplication.class, args);
	}

	@PostConstruct
	public void start() {
		TelegramBotsApi api = new TelegramBotsApi();
		try {
			api.registerBot(bot);
		} catch (TelegramApiException e) {
			log.error("Failed to register bot {} due to error {}", bot.getBotUsername(), e);
		}
	}
}
