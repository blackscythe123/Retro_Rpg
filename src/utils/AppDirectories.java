package utils;

import java.io.File;

/**
 * Centralized helper for determining application data locations across platforms.
 */
public final class AppDirectories {
    private static final String ENV_OVERRIDE = "RETRO_RPG_DATA_DIR";
    private static final String WINDOWS_APP_DIR = "RetroGame";
    private static final String UNIX_APP_DIR = ".retro_game";
    private static final String LEADERBOARD_SUBDIR = "leaderboards";
    private static final String FALLBACK_DIR = "retro-game-data";

    private AppDirectories() {
    }

    /**
     * Returns the base writable directory for application data. The directory is created if necessary.
     */
    public static File getBaseDirectory() {
        File base = ensureDirectory(resolveOverrideDirectory());
        if (base != null) {
            return base;
        }

        base = ensureDirectory(resolveOsDirectory());
        if (base != null) {
            return base;
        }

        return ensureDirectory(new File(FALLBACK_DIR));
    }

    /**
     * Directory used for leaderboard CSV files.
     */
    public static File getLeaderboardsDirectory() {
        File base = getBaseDirectory();
        File leaderboards = new File(base, LEADERBOARD_SUBDIR);
        File ensured = ensureDirectory(leaderboards);
        return ensured != null ? ensured : base;
    }

    /**
     * Log file location for crash reports.
     */
    public static File getLogFile() {
        return new File(getBaseDirectory(), "retro-game.log");
    }

    private static File resolveOverrideDirectory() {
        String override = System.getenv(ENV_OVERRIDE);
        if (override == null || override.trim().isEmpty()) {
            return null;
        }
        return new File(override.trim());
    }

    private static File resolveOsDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isEmpty()) {
                return new File(localAppData, WINDOWS_APP_DIR);
            }
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isEmpty()) {
            return new File(userHome, UNIX_APP_DIR);
        }
        return null;
    }

    private static File ensureDirectory(File directory) {
        if (directory == null) {
            return null;
        }
        if (directory.exists()) {
            return directory.isDirectory() ? directory : null;
        }
        return directory.mkdirs() ? directory : null;
    }
}
