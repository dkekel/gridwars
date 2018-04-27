package cern.ais.gridwars.web.util;

import cern.ais.gridwars.web.controller.error.AccessDeniedException;
import cern.ais.gridwars.web.domain.User;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

public final class ControllerUtils {

    public static final CacheControl FOREVER_CACHE_CONTROL = CacheControl.maxAge(31556926, TimeUnit.SECONDS).cachePublic();
    public static final String GZIP = "gzip";

    public static ResponseEntity<byte[]> createNotFoundByteDataResponse() {
        return ResponseEntity.notFound().build();
    }

    public static void assertIsAdmin(User user) {
        if (!user.isAdmin()) {
            throw new AccessDeniedException();
        }
    }

    private ControllerUtils() {
    }
}
