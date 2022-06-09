package com.geekbrains.cloud;

import lombok.Data;

@Data
public class DeleteRequest implements CloudMessage{
    private final String deleteFileName;
}
