package com.geekbrains.cloud;

import lombok.Data;

@Data
public class RenameRequest implements CloudMessage{
    private final String oldFilename;
    private final String newFilename;
}
