package com.geekbrains.cloud.netty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private class UserData {
        String login;
        String password;

        public UserData(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

    private List<UserData> users;
    private Connection authConnection;
    private Statement authStatement;
    private boolean isAuthenticated;

    public AuthService(Connection authConnection, Statement authStatement) {
        this.users = new ArrayList<>();
        this.authConnection = authConnection;
        this.authStatement = authStatement;

        try (ResultSet rs = authStatement.executeQuery("SELECT * FROM users;")) {
            while (rs.next()) {
                users.add(new UserData(rs.getString("uname"), rs.getString("upwd")));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public boolean passAuthentication(String userName, String userPwd) {
        isAuthenticated = false;

        for (UserData user : users) {
            if (user.login.equals(userName) && user.password.equals(userPwd)) {
                isAuthenticated = true;
            }
        }

        return isAuthenticated;
    }
}
