package com.kramar.bot.enam;

public enum UserPlanStatus {

    WAIT_BUTTON_ANSWER (""),
    WRITE_ANSWER ("\u270F Написать ответ"),
    COMMIT_ANSWER ("\u2705 Зафиксировать ответ"),
    FACK_YOU("\uD83D\uDD25 Иди нахуй!"),
    FACK_YOU_2("\uD83D\uDD25 Ф пизду!");

    private String value;

    UserPlanStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
