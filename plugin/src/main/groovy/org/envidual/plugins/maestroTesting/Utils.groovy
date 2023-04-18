package org.envidual.plugins.maestroTesting
import org.gradle.api.Project

enum OperatingSystem {
    MACOS,
    LINUX,
    WINDOWS,
    UNKNOWN
}

class Utils{
    /*
     * get the operating system the code is executed on
     * @return one of MACOS, WINDOWS, LINUX and UNKNOWN
     */
    static OperatingSystem getOS() {
        def os = System.getProperty('os.name').toLowerCase()
        if (os.contains('win')) {
            return OperatingSystem.WINDOWS
        } else if (os.contains('mac')) {
            return OperatingSystem.MACOS
        } else if (os.contains('nix') || os.contains('nux') || os.contains('aix')) {
            return OperatingSystem.LINUX
        } else {
            return OperatingSystem.UNKNOWN
        }
    }

    /*
     * Execute multiple commands sequentially.
     *
     * @param project The Gradle project in which to execute the commands.
     * @param workingDirectory The working directory for the executed commands. Default value is the project root directory
     * @param environmentVars Key value pairs of environment variables for all commands in the list
     * @param commands One or more Maps, each containing a command in the form ["command":[<executable> <options>...], "input":<inputStream>, "output":<outputStream>].
     * input and output are optional. The command must be a list of strings
     * @return The exit values of the commands.
     */
    static ArrayList<Integer> runCommands(Project project, String workingDirectory = null, Map<String, String> environmentVars, Map<String, Object>... commands) {
        def exitValues = []
        for (command in commands) {
            println "Executing command ${command['command']}"
            exitValues.add(project.exec {
                if (command.containsKey("output")) {
                    standardOutput = command["output"]
                }
                if (command.containsKey("input")) {
                    standardInput = command["input"]
                }
                if(environmentVars){
                    environment(environmentVars)
                }
                ignoreExitValue true
                workingDir(workingDirectory ?: "${project.projectDir}")
                commandLine command["command"]
            }.getExitValue())
            if (exitValues.last()) {
                println "WARNING: command ${command['command']} failed!"
            }
        }
        return exitValues
    }

    /*
     *
     * Start multiple commands asynchronously
     *
     * @param workingDirectory The working directory for the executed commands. Default value is the project root directory
     * @param environmentVars Key value pairs of environment variables for all commands in the list
     * @param commands One or more Maps, each containing a command in the form ["command":[<executable> <options>...], "input":<inputStream>, "output":<outputStream>].
     * input and output are optional. The command must be a list of strings
     * @return The processes that were started.
     */
    static ArrayList<Process> runCommandsAsync(String workingDirectory = null, Map<String, String> environmentVars, Map<String, Object>... commands) {
        def processes = []
        for (command in commands) {
            println "Executing async command ${command['command']}"
            def pb = new ProcessBuilder(command["command"] as String[])
            if (command.containsKey("output")) {
                pb.redirectOutput(command["output"])
            }
            else{
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            }
            if (command.containsKey("input")) {
                pb.redirectInput(command["input"])
            }
            if(environmentVars){
                pb.environment().putAll(environmentVars)
            }
            if(workingDirectory){
                pb.directory(new File(workingDirectory));
            }
            pb.redirectErrorStream(true)
            processes.add(pb.start())
        }
        return processes
    }

    /*
     * remove all files in a directory if it exists
     * @param directory the directory to delete the files from
     */
    static void clearDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            directory.listFiles()?.each { file ->
                if (file.isFile()) {
                    file.delete()
                }
            }
        }
    }
}