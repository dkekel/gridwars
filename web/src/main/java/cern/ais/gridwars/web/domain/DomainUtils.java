package cern.ais.gridwars.web.domain;

import java.util.UUID;


public final class DomainUtils {

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    private DomainUtils() {
    }
}
