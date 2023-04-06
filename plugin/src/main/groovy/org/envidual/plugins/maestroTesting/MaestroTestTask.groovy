package org.envidual.plugins.maestroTesting

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction


abstract class MaestroTestTask extends DefaultTask {
    @Input
    abstract Property<String> getDevice()

    @Input
    abstract Property<String> getOutputDirectory()

    @Input
    abstract Property<String> getTestDirectory()

    MaestroTestTask() {
        // Set default values for the properties
//        device.set("Pixel_6_API_33")
//        outputDirectory.set("build/reports/tests/maestroResults")
//        testDirectory.set("maestro")
    }

    @TaskAction
    void runMaestroTests() {
        def project = getProject()
        //task configuration
        def testOutputDir = "${project.projectDir}" + File.separator + outputDirectory.get()
        def testDir = "${project.projectDir}" + File.separator + testDirectory.get() + File.separator

        // android specific data which is used in the task
        def appID = project.android.defaultConfig.applicationId
        def androidSdkPath = project.android.sdkDirectory.getAbsolutePath()
        def apkPath = "build" + File.separator + "outputs" + File.separator + "apk" + File.separator + "debug" + File.separator + "${project.name}-debug.apk"

        //executable paths
        def adb = androidSdkPath + File.separator + "platform-tools" + File.separator + "adb"
        def emulator = androidSdkPath + File.separator + "emulator" + File.separator + "emulator"

        //local variables to store exit values
        def maestroResult  //variable to store the maestro test outcome
        def xunitPresent = 1

        //decide if an emulator should be created
        def createEmulator = (device.get() != "")

        //run the commands
        //start adb server
        runCommands(project,
                [adb, 'kill-server'],
                [adb, 'kill-server'],
                [adb, 'start-server'])
        sleep 3000
        if (createEmulator) {
            //start the emulator
            println "Starting Emulator"
            runCommandsAsync(
                    [emulator, '-avd', device.get(), '-netdelay', 'none', '-netspeed', 'full'])
        } else {
            println "Running with external emulator"
        }
        //wait for the emulator to be ready
        runCommands(project,
                [adb, 'wait-for-device'])
        def bootComplete = new ByteArrayOutputStream()
        println 'Waiting for boot to complete'
        while (!bootComplete.toString().contains('1')) {
            bootComplete = new ByteArrayOutputStream()
            runCommands(project, bootComplete, [adb, 'shell', 'getprop', 'sys.boot_completed'])
            sleep(1000)
        }
        //install the app on the emulator
        println "Installing apk"
        runCommands(project,
                [adb, '-s', 'emulator-5554', 'install', apkPath])
        //create output directory for the maestro test reports
        def dir = new File(testOutputDir)
        clearDirectory(dir)
        dir.mkdirs()
        println "Starting Maestro Tests"
        //run the maestro tests
        maestroResult = runCommands(project,
                ['maestro', 'test', '--format', 'junit', '--output', testOutputDir + File.separator + 'maestro-report.xml', testDir])
        //create html test report
        if(maestroResult != 0) {
            xunitPresent = runCommands(project,
                    [adb, 'uninstall', appID],
                    ['xunit-viewer', '-r', testOutputDir + File.separator + "maestro-report.xml", '-o', testOutputDir + File.separator + "index.html"])
        }
        if (createEmulator) {
            //shut down the emulator
            runCommands(project,
                    [adb, '-s', 'emulator-5554', 'emu', 'kill'])
        }
        println "Maestro Tests done!"
        //check if there were failing tests
        assert maestroResult == 0: ("Not all Maestro Tests passed." + ((xunitPresent == 0) ? " See file://$testOutputDir/index.html for Maestro test results." : " Couldn't generate html report. Please install xunit-viewer"))
    }

    //some utility to make the code shorter & clearer

    /**
     * Execute multiple command-line commands sequentially.
     *
     * @param project The Gradle project in which to execute the commands.
     * @param outputStream An optional OutputStream to redirect the standard output of the commands.
     * @param commands One or more List<String> objects, each containing a command in the form [<executable> <options>...].
     * @return The exit value of the last command executed.
     */
    static int runCommands(Project project, ByteArrayOutputStream outputStream = null, List<String>... commands) {
        def exitValue = 0
        for (command in commands) {
            exitValue = project.exec {
                if (outputStream) {
                    standardOutput = outputStream
                }
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

    /**
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

