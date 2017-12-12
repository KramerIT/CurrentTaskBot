package com.kramar.bot.bot;


import com.kramar.bot.dbo.TaskDbo;
import com.kramar.bot.dbo.UserDbo;
import com.kramar.bot.repository.TaskRepository;
import com.kramar.bot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private boolean collectAnswerFromUsers = false;
    private Map<Long, String> usersTaskMap = new HashMap<>();
    private static final Long DREAM_TEAM_CHAT = -298029401L;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Override
    public String getBotUsername() {
        return "CurrentTaskBot";
    }

    @Override
    public String getBotToken() {
        return "500927826:AAFiZjIPydhv0bc300QuBTsEOnk2TKJW7Ps";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handle(update.getMessage());
        }
    }

    @Scheduled(cron = "0 30 10 * * *")
    public void askCurrentTaskForToday() {
        collectAnswerFromUsers = true;
        List<UserDbo> all = userRepository.findAll().stream().filter(userDbo -> userDbo.getApprovedUser() && userDbo.getTelegramId() != null).collect(Collectors.toList());
        SendMessage message = new SendMessage();
        message.setText("Чё буш сёння делать?");
        for (UserDbo userDbo : all) {
            message.setChatId(userDbo.getTelegramId());
            sendToUserMessage(message);
        }
    }

    @Scheduled(cron = "0 35 10 * * *")
    @Scheduled(cron = "0 05 19 * * *")
    public void collectAnswersFromUsers() {
        collectAnswerFromUsers = false;
        for (Long id : usersTaskMap.keySet()) {
            UserDbo userDbo = userRepository.findByTelegramId(id);
            TaskDbo taskDbo = new TaskDbo();
            taskDbo.setDate(LocalDate.now());
            taskDbo.setOwner(userDbo);
            taskDbo.setDescription(usersTaskMap.get(id));
            taskRepository.save(taskDbo);
            SendMessage message = new SendMessage();
            message.setText(userDbo.getName() + ":\n" + "");
        }
        usersTaskMap.clear();
    }

    @Scheduled(cron = "0 0 19 * * *")
    public void askResultOfCurrentTask() {
        collectAnswerFromUsers = true;
        List<UserDbo> all = userRepository.findAll().stream().filter(userDbo -> userDbo.getApprovedUser() && userDbo.getTelegramId() != null).collect(Collectors.toList());
        SendMessage message = new SendMessage();
        message.setText("Чё сделал сегодня?");
        for (UserDbo userDbo : all) {
            message.setChatId(userDbo.getTelegramId());
            sendToUserMessage(message);
        }
    }

    @Scheduled(cron = "0 06 19 * * *")
    public void pullToChatAllTodayTasks() {
        List<UserDbo> all = userRepository.findAll().stream().filter(userDbo -> userDbo.getApprovedUser() && userDbo.getTelegramId() != null).collect(Collectors.toList());
        for (UserDbo userDbo : all) {
            StringBuilder text = null;
            List<TaskDbo> byDate = taskRepository.findByDateAndOwnerOrderById(LocalDate.now(), userDbo);
            if (byDate.size() == 1) {
                text = new StringBuilder()
                        .append(byDate.get(0).getDescription())
                        .append("\n");

            } else if (byDate.size() == 2) {
                text = new StringBuilder()
                        .append(byDate.get(0).getDescription())
                        .append("\n")
                        .append("Результат:\n")
                        .append(byDate.get(1).getDescription());
            }
            if (text != null) {
                SendMessage message = new SendMessage();
                message.setText(userDbo.getName() + ":\n" + text.toString());
                message.setChatId(DREAM_TEAM_CHAT);
                sendToUserMessage(message);
            }
        }
    }

    private void handle(Message message) {

        List<UserDbo> allUsers = userRepository.findAll();
        List<Long> approvedUsers = allUsers.stream().filter(userDbo -> userDbo.getApprovedUser() && userDbo.getTelegramId() != null).map(UserDbo::getTelegramId).collect(Collectors.toList());

        Long userId = message.getChatId();
        Integer token = message.getMessageId();
        log.info(String.format("ChatId %s; token: %s; Get request: %s", userId, token, message.getText()));

        if ("/start".equals(message.getText())) {
            sendStartScreen(message);
        } else if (message.getContact() != null && message.getContact().getPhoneNumber() != null) {
            Optional<UserDbo> byPhone = userRepository.findByPhone(message.getContact().getPhoneNumber().replace("+", ""));
            if (byPhone.isPresent()) {
                UserDbo userDbo = byPhone.get();
                userDbo.setTelegramId(message.getChatId());
                userDbo.setApprovedUser(true);
                userRepository.save(userDbo);
                SendMessage callback = createResponse("" +
                        "Поздравляем Вас, " +
                        userDbo.getName() +
                        ", с успешной регистрацией!\n" +
                        "Бот тупой и создан только для опросника текущих задач сотрудников. Утром он у Вас спросит чем Вы планируете сегодня заниматься, а вечером поинтересуется " +
                        "что Вы сделали. После данная информация попадёт вашему БигБосу в лице тим лида, манагера или т.п.", message);
                callback.setReplyMarkup(new ReplyKeyboardRemove());
                sendToUserMessage(callback);
            } else {
                SendMessage callback = createResponse("Вас не удалось зарегистрировать :(\n" +
                        "Обратитесь к разработчику бота (375333103331) для добавления вашего телефонного номера в список доверенных лиц", message);
                sendToUserMessage(callback);
            }
        } else if (message.getSticker() != null || !collectAnswerFromUsers) {
            SendMessage callback = createResponse(":)", message);
            sendToUserMessage(callback);
        } else if (collectAnswerFromUsers && approvedUsers.contains(message.getChatId())) {
            String task = usersTaskMap.get(message.getChatId());
            if (task == null) {
                task = message.getText();
            } else {
                task = task.concat("\n" + message.getText());
            }
            usersTaskMap.put(message.getChatId(), task);
            SendMessage callback = createResponse("Ok!", message);
            sendToUserMessage(callback);
        }
    }


    private void sendStartScreen(Message message) {
        sendToUserMessage(createResponseForRegistration(message));
    }

    private SendMessage createResponse(String text, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText(text);
        return response;
    }

    private SendMessage createResponse(Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText(message.getText());
        return response;
    }

    private SendMessage createResponseForRegistration(Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());

        if ("/start".equals(message.getText())) {
            response.setText(
                    "Даный бот предназначен только для сотрудников GP Solutions.\n" +
                            "Для использования бота необходимо зарегистрироваться, предоставив Ваш номер телефона.\n"
            );
            response.setReplyMarkup(getTelegramKeyboardForRegistration());
        }
        return response;
    }

    private void sendToUserMessage(SendMessage message) {
        message.disableWebPagePreview();
        try {
            sendApiMethod(message);
        } catch (TelegramApiException e) {
            log.error(String.format("ChatId %s. Error send message to user: %s", message.getChatId(), e));
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getTelegramKeyboardForRegistration() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
//        replyKeyboardMarkup.setOneTimeKeyboad(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

//        List<Button> existButtons = arrayKeyboard.getKeyboard();
//
//        if (existButtons.size() <= 4) {
//            for (int i = 0; i < existButtons.size(); i++) {
//                keyboardRow.add(new KeyboardButton(existButtons.get(i).getText()));
//                keyboard.add(keyboardRow);
//                keyboardRow = new KeyboardRow();
//            }
//
//        } else {
//            for (int i = 0; i < existButtons.size(); i++) {
//                keyboardRow.add(new KeyboardButton(existButtons.get(i).getText()));
//                if (i % 2 != 0) {
//                    keyboardRow = new KeyboardRow();
//                } else {
//                    keyboard.add(keyboardRow);
//                }
//            }
//        }
        KeyboardButton keyboardButton = new KeyboardButton("Зарегистрироваться в боте!");
        keyboardButton.setRequestContact(true);
        keyboardRow.add(keyboardButton);
        keyboard.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

}
