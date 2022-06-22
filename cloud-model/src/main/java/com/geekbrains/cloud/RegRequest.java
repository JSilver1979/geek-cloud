package com.geekbrains.cloud;

import lombok.Data;

@Data
public class RegRequest implements CloudMessage{
    private final String login;
    private final String pwd;
}
