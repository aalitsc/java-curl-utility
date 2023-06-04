package org.ers.model;

public class LoginRequest {

    private String username;
    private String password;

    private LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static LoginRequest of(String username, String password) {
        return new LoginRequest(username, password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
