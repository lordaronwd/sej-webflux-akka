package com.zooplus.sej.msone;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lazar.agatonovic
 */
@Data
@AllArgsConstructor
public class UserModel {

    private Long id;
    private String name;
    private String surname;
}
