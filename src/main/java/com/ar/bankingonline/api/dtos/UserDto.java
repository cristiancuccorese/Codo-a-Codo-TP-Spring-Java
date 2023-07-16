package com.ar.bankingonline.api.dtos;

import lombok.*;

import java.util.List;

//Uso @anotaciones, para escribir menos codigo usando lombok
@Data
public class UserDto {

    public UserDto(){}

    private Long id;

    private String username;

    private String password;

    private List<Long> idAccounts;

}
