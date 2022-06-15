package com.geekbrains.cloud;

import lombok.Data;

@Data
public class WarningMessage implements CloudMessage{
    private final String warning;
}
