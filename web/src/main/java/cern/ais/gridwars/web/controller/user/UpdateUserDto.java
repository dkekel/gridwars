package cern.ais.gridwars.web.controller.user;

import cern.ais.gridwars.web.util.validation.ValidEmail;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class UpdateUserDto {

    private String id;

    private String username;

    private String teamName;

    @Size(max = 32)
    @ValidEmail
    private String email;

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

    public String getTeamName() {
        return teamName;
    }

    public UpdateUserDto setTeamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UpdateUserDto setEmail(String email) {
        this.email = email;
        return this;
    }
}
