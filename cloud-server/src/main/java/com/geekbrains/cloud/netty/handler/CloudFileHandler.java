package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import com.geekbrains.cloud.netty.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
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
        switch(cloudMessage) {
            case AuthRequest authRequest -> getAuth(ctx, authRequest);
            case FileRequest fileRequest -> getFiles(ctx, fileRequest);
            case FileMessage fileMessage -> getFilesList(ctx, fileMessage);
            case RegRequest regRequest -> getRegistration(ctx, regRequest);
            case PathInRequest pathInRequest -> getPathIn(ctx, pathInRequest);
            case PathUpRequest pathUpRequest -> getPathUp(ctx);
            case DeleteRequest deleteRequest -> getDelete(ctx, deleteRequest);
            case RenameRequest renameRequest -> getRename(ctx, renameRequest);
            case NewDirRequest newDirRequest -> getNewDir(ctx, newDirRequest);
            default -> throw new IllegalStateException("Unexpected value: " + cloudMessage);
        }
    }

    private void getAuth(ChannelHandlerContext ctx, AuthRequest authRequest) throws IOException {
        if (authService.passAuthentication(authRequest.getLogin(), authRequest.getPwd())) {
            if (Files.notExists(rootDir.resolve(authRequest.getLogin()))) {
                Files.createDirectory(rootDir.resolve(authRequest.getLogin()));
            }
            userDir = rootDir.resolve(authRequest.getLogin());
            ctx.writeAndFlush(new AuthApprove());
            ctx.writeAndFlush(new ListFiles(userDir));
        } else {
            ctx.writeAndFlush(new WarningMessage("wrong login or password"));
        }
    }
    private void getFiles(ChannelHandlerContext ctx, FileRequest fileRequest) throws IOException {
        ctx.writeAndFlush(new FileMessage(userDir.resolve(fileRequest.getName())));
    }
    private void getFilesList(ChannelHandlerContext ctx, FileMessage fileMessage) throws IOException {
        Files.write(userDir.resolve(fileMessage.getName()), fileMessage.getData());
        ctx.writeAndFlush(new ListFiles(userDir));
    }

    private void getRegistration(ChannelHandlerContext ctx, RegRequest regRequest) {
        ctx.writeAndFlush(new WarningMessage(authService.passRegistration(regRequest.getLogin(), regRequest.getPwd())));
    }
    private void getPathIn(ChannelHandlerContext ctx, PathInRequest pathInRequest) throws IOException {
        if (Paths.get(String.valueOf(userDir)).resolve(pathInRequest.getDirName()).toFile().isFile()) {
            ctx.writeAndFlush(new WarningMessage(pathInRequest.getDirName() + " not a Directory!"));
        } else {
            userDir = Paths.get(String.valueOf(userDir)).resolve(pathInRequest.getDirName());
            ctx.writeAndFlush(new ListFiles(userDir));
        }
    }
    private void getPathUp(ChannelHandlerContext ctx) throws IOException {
        if (Paths.get(String.valueOf(userDir)).getParent().equals(rootDir)) {
            ctx.writeAndFlush(new WarningMessage("This is a root Directory.\n Cannot go higher."));
        } else {
            userDir = Paths.get(String.valueOf(userDir)).getParent().normalize();
            ctx.writeAndFlush(new ListFiles(userDir));
        }
    }
    private void getDelete(ChannelHandlerContext ctx, DeleteRequest deleteRequest) throws IOException {
        Files.deleteIfExists(Paths.get(userDir.toString()).resolve(deleteRequest.getDeleteFileName()));
        ctx.writeAndFlush(new ListFiles(userDir));
    }
    private void getRename(ChannelHandlerContext ctx, RenameRequest renameRequest) throws IOException {
        File toRename = Paths.get(userDir.toString()).resolve(renameRequest.getOldFilename()).toFile();
        toRename.renameTo(Paths.get(userDir.toString()).resolve(renameRequest.getNewFilename()).toFile());
        ctx.writeAndFlush(new ListFiles(userDir));
    }
    private void getNewDir(ChannelHandlerContext ctx, NewDirRequest newDirRequest) throws IOException {
        if (Files.notExists(userDir.resolve(newDirRequest.getDirName()))) {
            Files.createDirectory(userDir.resolve(newDirRequest.getDirName()));
            ctx.writeAndFlush(new WarningMessage("Dir: " + newDirRequest.getDirName() + " created"));
            ctx.writeAndFlush(new ListFiles(userDir));
        } else {
            ctx.writeAndFlush(new WarningMessage("such name already in use"));
        }
    }

}
