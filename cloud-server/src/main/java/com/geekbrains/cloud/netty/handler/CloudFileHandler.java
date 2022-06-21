package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import com.geekbrains.cloud.netty.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path rootDir;
    private Path userDir;
    AuthService authService;

    public CloudFileHandler(AuthService authService) {
        rootDir = Path.of("server_files");
        this.authService = authService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(new ListFiles(currentDir)); //no need to send this
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof AuthRequest authRequest) {
            if (authService.passAuthentication(authRequest.getLogin(), authRequest.getPwd())) {
                if (Files.notExists(rootDir.resolve(authRequest.getLogin()))) {
                    Files.createDirectory(rootDir.resolve(authRequest.getLogin()));
                }
                userDir = rootDir.resolve(authRequest.getLogin());
                ctx.writeAndFlush(new ListFiles(userDir));
                ctx.writeAndFlush(new AuthApprove());
            } else {
                ctx.writeAndFlush(new WarningMessage("wrong login or password"));
            }
        } else if (cloudMessage instanceof RegRequest regRequest) {
            ctx.writeAndFlush(new WarningMessage(authService.passRegistration(regRequest.getLogin(), regRequest.getPwd())));
        } else if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(userDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(userDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(userDir));
        } else if (cloudMessage instanceof PathInRequest pathInRequest) {
            if (Paths.get(String.valueOf(userDir)).resolve(pathInRequest.getDirName()).toFile().isFile()) {
                ctx.writeAndFlush(new WarningMessage(pathInRequest.getDirName() + " not a Directory!"));
            }else {
                userDir = Paths.get(String.valueOf(userDir)).resolve(pathInRequest.getDirName());
                ctx.writeAndFlush(new ListFiles(userDir));
            }
        } else if (cloudMessage instanceof  PathUpRequest) {
            if (Paths.get(String.valueOf(userDir)).getParent().equals(rootDir)) {
                ctx.writeAndFlush(new WarningMessage("This is a root Directory.\n Cannot go higher."));
            } else {
                userDir = Paths.get(String.valueOf(userDir)).getParent().normalize();
                ctx.writeAndFlush(new ListFiles(userDir));
            }
        } else if (cloudMessage instanceof DeleteRequest deleteRequest) {
            Files.deleteIfExists(Paths.get(userDir.toString()).resolve(deleteRequest.getDeleteFileName()));
            ctx.writeAndFlush(new ListFiles(userDir));
        } else if (cloudMessage instanceof RenameRequest renameRequest) {
            File toRename = Paths.get(userDir.toString()).resolve(renameRequest.getOldFilename()).toFile();
            toRename.renameTo(Paths.get(userDir.toString()).resolve(renameRequest.getNewFilename()).toFile());
            ctx.writeAndFlush(new ListFiles(userDir));
        }
    }
}
