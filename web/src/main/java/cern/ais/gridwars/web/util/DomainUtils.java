package cern.ais.gridwars.web.util;

import java.util.UUID;


public final class DomainUtils {

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static String truncate(String value, int maxCharacters) {
        if ((value != null) && (value.length() > maxCharacters)) {
            return value.substring(0, maxCharacters);
        } else {
            return value;
        }
    }

    private DomainUtils() {
    }
}
