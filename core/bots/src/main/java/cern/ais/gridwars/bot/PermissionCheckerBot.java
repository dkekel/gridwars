package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.List;


/**
 * Used during development to analyse the environment with enabled SecurityManager in order to lock
 * down the bot code in the match runtime.
 *
 * For the different permissions see: https://docs.oracle.com/javase/8/docs/technotes/guides/security/permissions.html
 */
public class PermissionCheckerBot implements PlayerBot {

    private boolean printedOutputDuringTurn = false;
    private SecurityManager secMgr = System.getSecurityManager();

    public PermissionCheckerBot() {
        logAvailablePermissions();
    }

	@Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
		// Stand your ground and die like a man!
        if (printedOutputDuringTurn) {
            return;
        }

        try {
            logAvailablePermissions();
        } finally {
            printedOutputDuringTurn = true;
        }
	}

	private void logAvailablePermissions() {
	    log("Start checking permissions ...");

        if (secMgr == null) {
            log("NO security manager active, I can do whatever the hell I want!!");
            return;
        }

        checkAccessToRuntimePermission("createClassLoader");
        checkAccessToRuntimePermission("getClassLoader");
        checkAccessToPermission(new ReflectPermission("suppressAccessChecks"));
        checkAccessToRuntimePermission("accessDeclaredMembers");

        checkAccessTo("Can access all system properties", secMgr::checkPropertiesAccess);
        checkAccessTo("Can access specific system property", () -> secMgr.checkPropertyAccess("gridwars.runtime.bot1JarPath"));

        checkAccessTo("Can read file", () -> secMgr.checkRead("foo.txt"));
        checkAccessTo("Can write file", () -> secMgr.checkWrite("foo.txt"));
        checkAccessTo("Can delete file", () -> secMgr.checkDelete("foo.txt"));
        checkAccessTo("Can execute file", () -> secMgr.checkExec("foo.txt"));

        checkAccessToRuntimePermission("setIO");

        checkAccessTo("Modify current thread", () -> secMgr.checkAccess(Thread.currentThread()));
        checkAccessTo("Modify current thread group", () -> secMgr.checkAccess(Thread.currentThread().getThreadGroup()));
        checkAccessTo("Create new threads", () -> {
            Thread thread = new Thread();
            thread.start();
        });
        checkAccessToRuntimePermission("stopThread");
        checkAccessTo("Can exit the VM", () -> secMgr.checkExit(0));

        checkAccessToRuntimePermission("setSecurityManager");

        checkAccessTo("Can listen on port", () -> secMgr.checkListen(8080));
        checkAccessTo("Can conntect to remot host", () -> secMgr.checkConnect("http://www.google.de", 80));
        checkAccessTo("Set socket factory", secMgr::checkSetFactory);

        log("... finished checking permissions");
    }

    private void checkAccessToRuntimePermission(String runtimePermission) {
	    checkAccessToPermission(new RuntimePermission(runtimePermission));
    }

    private void checkAccessToPermission(Permission permission) {
	    checkAccessTo(permission.toString(), () -> secMgr.checkPermission(permission));
    }

    private void checkAccessTo(String permissionDescription, Runnable permissionCheck) {
	    boolean hasAccess;

	    try {
	        permissionCheck.run();
	        hasAccess = true;
        } catch (SecurityException e) {
            hasAccess = false;
        }

        log(permissionDescription + ": " + (hasAccess ? "YES" : "NO"));
    }

    private void log(String message) {
        System.out.println(message);
    }
}
