package com.geekbrains.cloud;

import lombok.Data;

@Data
public class NewDirRequest implements CloudMessage{
    private final String dirName;
}
