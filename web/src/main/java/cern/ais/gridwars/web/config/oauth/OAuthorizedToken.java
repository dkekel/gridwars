package cern.ais.gridwars.web.config.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class OAuthorizedToken implements Serializable {

    private static final long serialVersionUID = -1L;

    private String username;
    private List<String> scope;
    @JsonProperty("exp")
    private long expirationTimestamp;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(final List<String> scope) {
        this.scope = scope;
    }

    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(final long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }
}
