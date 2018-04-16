package cern.ais.gridwars.web.controller;

import javax.persistence.Column;
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

    @Column(nullable = false)
    @Size(min = 6, max = 32) // This field stores a hash, so make it big enough!
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

    @NotNull
    @NotEmpty
    private String registrationPassword;

    private String ip;

    public String getUsername() {
        return username;
    }

    public NewUserDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NewUserDto setPassword(String password) {
        this.password = password;
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

    public String getRegistrationPassword() {
        return registrationPassword;
    }

    public NewUserDto setRegistrationPassword(String registrationPassword) {
        this.registrationPassword = registrationPassword;
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
