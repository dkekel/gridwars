package cern.ais.gridwars.web.controller;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class UpdateUserDto {

    private String id;

    private String username;

    @Size(max = 32)
    private String password;

    @NotNull
    @NotEmpty
    @Size(max = 32)
    // TODO Validate email
    private String email;

    @NotNull
    @NotEmpty
    @Size(min = 4, max = 32)
    private String teamName;

    public String getId() {
        return id;
    }

    public UpdateUserDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UpdateUserDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UpdateUserDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UpdateUserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getTeamName() {
        return teamName;
    }

    public UpdateUserDto setTeamName(String teamName) {
        this.teamName = teamName;
        return this;
    }
}
