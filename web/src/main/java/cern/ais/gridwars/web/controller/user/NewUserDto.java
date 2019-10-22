package cern.ais.gridwars.web.controller.user;

import cern.ais.gridwars.web.util.validation.ValidEmail;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


public class NewUserDto {

    @NotNull
    @NotEmpty
    @Size(min = 4, max = 16)
    @Pattern(regexp = "[A-Za-z0-9-_]+")
    private String username;

    @Size(max = 32)
    @ValidEmail
    private String email;

    @Size(min = 4, max = 32)
    private String teamName;

    private String ip;

    public String getUsername() {
        return username;
    }

    public NewUserDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public NewUserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getTeamName() {
        return teamName;
    }

    public NewUserDto setTeamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public NewUserDto setIp(String ip) {
        this.ip = ip;
        return this;
    }
}
