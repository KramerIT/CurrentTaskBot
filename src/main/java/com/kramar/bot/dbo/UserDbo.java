package com.kramar.bot.dbo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDbo extends AbstractEntity {

    private static final long serialVersionUID = -1803488657443938487L;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "telegram_id")
    private Long telegramId;

    @Column(name = "approved_user")
    private Boolean approvedUser;


}
