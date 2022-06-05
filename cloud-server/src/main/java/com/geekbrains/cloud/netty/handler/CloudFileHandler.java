package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;

    public CloudFileHandler() {
        currentDir = Path.of("server_files");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListFiles(currentDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(currentDir));
        } else if (cloudMessage instanceof PathInRequest pathInRequest) {
            if (Paths.get(String.valueOf(currentDir)).resolve(pathInRequest.getDirName()).toFile().isFile()) {
                // here needs new message for warning
                ctx.writeAndFlush(new WarningMessage(pathInRequest.getDirName() + " not a Directory!"));
            }else {
                currentDir = Paths.get(String.valueOf(currentDir)).resolve(pathInRequest.getDirName());
                ctx.writeAndFlush(new ListFiles(currentDir));
            }
        } else if (cloudMessage instanceof  PathUpRequest pathUpRequest) {
            if (Paths.get(String.valueOf(currentDir)).getParent() == null) {
                ctx.writeAndFlush(new WarningMessage("This is a root Directory.\n Cannot go higher."));
            } else {
                currentDir = Paths.get(String.valueOf(currentDir)).getParent().normalize();
                ctx.writeAndFlush(new ListFiles(currentDir));
            }
        }
    }
}
