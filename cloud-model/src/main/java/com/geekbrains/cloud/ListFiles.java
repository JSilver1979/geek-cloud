package com.geekbrains.cloud;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListFiles implements CloudMessage {

    private final List<String> files;

    public ListFiles(Path path) throws IOException {
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        for (int i = 0; i < files.size(); i++) {
            if (path.resolve(files.get(i)).toFile().isFile()) {
                files.set(i,"F: " + files.get(i));
            } else {
                files.set(i, "D: " + files.get(i));
            }
        }

    }

}
