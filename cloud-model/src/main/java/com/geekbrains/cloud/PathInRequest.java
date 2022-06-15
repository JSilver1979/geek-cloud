package com.geekbrains.cloud;

import lombok.Data;

@Data
public class PathInRequest implements CloudMessage{

    private final String dirName;
}
