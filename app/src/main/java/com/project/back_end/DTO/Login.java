package com.project.back_end.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Login {

    @JsonAlias({"email", "username"})
    private String identifier;
    private String password;

    public Login() {}

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
