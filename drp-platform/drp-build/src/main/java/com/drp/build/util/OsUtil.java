package com.drp.build.util;

public class OsUtil {

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();

    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final boolean IS_LINUX = OS_NAME.contains("linux") || OS_NAME.contains("mac");

    /**
     * 是否为 Windows 系统
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * 是否为 Linux/Mac 系统
     */
    public static boolean isLinux() {
        return IS_LINUX;
    }

    /**
     * 获取 Shell 路径
     * Windows: cmd.exe
     * Linux/Mac: bash
     */
    public static String getShell() {
        return isWindows() ? "cmd.exe" : "bash";
    }

    /**
     * 获取 Shell 参数
     * Windows: /c
     * Linux/Mac: -c
     */
    public static String getShellArg() {
        return isWindows() ? "/c" : "-c";
    }

    /**
     * 执行 cd 命令
     * Windows: cd /d "path"
     * Linux/Mac: cd "path" 或 cd path
     */
    public static String getCdCommand(String path) {
        return isWindows() ? "cd /d \"" + path + "\"" : "cd \"" + path + "\"";
    }

    /**
     * 获取换行符
     */
    public static String getNewline() {
        return isWindows() ? "\r\n" : "\n";
    }
}
