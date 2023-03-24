package org.envidual.plugins.maestroTesting

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


abstract class MaestroTestTask extends DefaultTask {
    @Input
    Property<String> device

    @Input
    Property<String> outputDirectory

    @Input
    Property<String> testDirectory

    MaestroTestTask() {
        // Set default values for the properties
        device.set("Pixel_6_API_33")
        outputDirectory.set("build/reports/tests/maestroResults")
        testDirectory.set("maestro")
    }

    @TaskAction
    void runMaestroTests() {
        def project = getProject()
        doLast {
            //task configuration
            def testOutputDir = "${project.projectDir}" + File.separator + outputDirectory
            def testDir = "${project.projectDir}" + File.separator + testDirectory + File.separator

            //executable paths
            def adb = androidSdkPath + File.separator + "platform-tools" + File.separator + "adb"
            def emulator = androidSdkPath + File.separator + "emulator" + File.separator + "emulator"

            //local variables to store exit values
            def maestroResult = 0 //variable to store the maestro test outcome
            def xunitPresent = 0

            // android specific data which is used in the task
            def appID = project.android.defaultConfig.applicationId
            def androidSdkPath = project.android.sdkDirectory.getAbsolutePath()
            def apkPath = "build" + File.separator + "outputs" + File.separator + "apk" + File.separator + "debug" + File.separator + "app-debug.apk"

            //run the commands
            //start adb server
            runCommands(project,
                    [adb, 'kill-server'],
                    [adb, 'kill-server'],
                    [adb, 'start-server'])
            sleep 3000
            //start the emulator
            println "Starting Emulator"
            runCommandsAsync(
                    [emulator, '-avd', emulatorName, '-netdelay', 'none', '-netspeed', 'full'])
            sleep 30000 //give the emulator time to boot TODO check status with adb instead
            //install the app on the emulator
            println "Installing apk"
            runCommands(project,
                    [adb, '-s', 'emulator-5554', 'install', apkPath])
            //create output directory for the maestro test reports
            def dir = new File(testOutputDir)
            if (!dir.exists())
                dir.mkdirs()
            println "Starting Maestro Tests"
            //run the maestro tests
            maestroResult = runCommands(project,
                    ['maestro', 'test', '--format', 'junit', '--output', testOutputDir + File.separator + 'maestro-report.xml', testDir])
            //create html test report
            xunitPresent = runCommands(project,
                    [adb, 'uninstall', appID],
                    ['xunit-viewer', '-r', testOutputDir + File.separator + "maestro-report.xml", '-o', testOutputDir + File.separator + "index.html"])
            //shut down the emulator
            runCommands(project,
                    [adb, '-s', 'emulator-5554', 'emu', 'kill'])
            println "Maestro Tests done!"
            //check if there were failing tests
            assert maestroResult == 0: "Not all Maestro Tests passed." + ((xunitPresent == 0) ? " See file://$testOutputDir/index.html for Maestro test results." : " Couldn't generate html report. Please install xunit-viewer")
        }
    }

    //some utility to make the code shorter & clearer

    /**
     * run multiple commands sequentially
     * @param commands list of commands of the form [<executable> <options>...]
     * @return exit value of the last command in the list
     */
    static int runCommands(Project project, List<String>... commands) {
        def exitValue = 0
        for (command in commands) {
            exitValue = project.exec {
                ignoreExitValue true
                workingDir "${project.projectDir}"
                commandLine command
            }.getExitValue()
        }
        return exitValue
    }

    /**
     * run each command in a new process, don't wait for it to finish
     * @param command of the form [<executable> <options>...]
     */
    static void runCommandsAsync(List<String>... commands) {
        for (command in commands) {
            def pb = new ProcessBuilder(command)
            pb.redirectErrorStream(true)
            pb.start()
        }
    }
}

