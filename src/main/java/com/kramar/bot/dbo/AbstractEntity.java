package com.kramar.bot.dbo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
@Data
@EqualsAndHashCode
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 8874971979191857414L;

    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO, generator="my_entity_seq_gen")
//    @SequenceGenerator(name="my_entity_seq_gen", sequenceName="MY_ENTITY_SEQ")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
