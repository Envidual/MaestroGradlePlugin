package org.envidual.plugins.maestroTesting

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

    @Input
    abstract Property<String> getSdkPath()

    @Input
    @Optional
    abstract Property<String> getMaestroPath()

    @Input
    abstract Property<String> getEmulatorOptions()

    MaestroTestTask() {
    }

    @TaskAction
    void runMaestroTests() {
        def project = getProject()
        //task configuration
        def testOutputDir = "${project.projectDir}/${outputDirectory.get()}"
        def testDir = "${project.projectDir}/${testDirectory.get()}/"

        // android specific data which is used in the task
        def appID = project.android.defaultConfig.applicationId
        def androidSdkPath = sdkPath.get()
        def apkPath = "build/outputs/apk/debug/${project.name}-debug.apk"

        def androidHome = new File(androidSdkPath).getParent().toString()
        def androidEnvironment = [
                'ANDROID_HOME': androidHome,
                'ANDROID_SDK_ROOT': androidHome,
                'ANDROID_SDK_HOME': androidHome,
        ]

        //executable paths
        def adb = "${androidSdkPath}/platform-tools/adb"
        def emulator = "${androidSdkPath}/emulator/emulator"

        //local variables to store exit values
        def maestroResult  //variable to store the maestro test outcome
        def xunitPresent = 1

        //decide if an emulator should be created
        def createEmulator = (device.get() != "")

        def emulatorProcess
        def emulatorOutputThread
        def keepRunning = true

        //start adb server
        Utils.runCommands(project,[:],
                ["command": [adb, 'kill-server']],
                ["command": [adb, 'start-server']])
        sleep 3000
        if (createEmulator) {
            //start the emulator
            println "Starting Emulator"

            emulatorProcess = Utils.runCommandsAsync(androidSdkPath, androidEnvironment,
                    ["command": [emulator.toString(), '-avd', device.get()] + emulatorOptions.get().split("\\s+").toList()])[0]
            emulatorOutputThread = new Thread({
                // Read output of child process and print to parent process's stdout
                def reader = new BufferedReader(new InputStreamReader(emulatorProcess.getInputStream()))
                while (keepRunning) {
                    String line = reader.readLine()
                    if (line == null) {
                        break // Exit loop when end of stream is reached
                    }
                    println "EMULATOR: " + line
                }
            })
            emulatorOutputThread.start()
        } else {
            println "Running with external emulator"
        }
        //wait for the emulator to be ready
        Utils.runCommands(project,[:],
                ["command": [adb, 'wait-for-device']])
        def bootComplete = new ByteArrayOutputStream()
        println 'Waiting for boot to complete'
        while (!bootComplete.toString().contains('1')) {
            bootComplete = new ByteArrayOutputStream()
            Utils.runCommands(project,[:], ["command": [adb, 'shell', 'getprop', 'sys.boot_completed'], "output": bootComplete])
            sleep(1000)
        }
        //TEST
        println "Devices:"
        Utils.runCommands(project,[:],
                ["command": [adb, 'devices']])
        sleep(5000) //wait a bit before installing the app because the emulator still isn't ready yet sometimes
        //install the app on the emulator
        println "Installing apk"
        Utils.runCommands(project,[:],
                ["command": [adb, '-s', 'emulator-5554', 'install', apkPath]])
        //create output directory for the maestro test reports
        def dir = new File(testOutputDir)
        Utils.clearDirectory(dir)
        dir.mkdirs()
        println "Starting Maestro Tests"
        //run the maestro tests
        maestroResult = Utils.runCommands(project,[:],
                ["command": [maestroPath.get(), 'test', '--format', 'junit', '--output', "${testOutputDir}/maestro-report.xml", testDir]])
        //create html test report
        if (maestroResult[0] != 0) {
            xunitPresent = Utils.runCommands(project,
                    ["command": [adb, 'uninstall', appID]],
                    ["command": ['xunit-viewer', '-r', "${testOutputDir}/maestro-report.xml", '-o', "${testOutputDir}/index.html"]])
        }
        if (createEmulator) {
            //shut down the emulator
            Utils.runCommands(project,[:],
                    ["command": [adb, '-s', 'emulator-5554', 'emu', 'kill']])
            keepRunning = false
            emulatorOutputThread.join()
        }
        println "Maestro Tests done!"
        //check if there were failing tests
        assert maestroResult[0] == 0: ("Not all Maestro Tests passed." + ((xunitPresent == 0) ? " See file://$testOutputDir/index.html for Maestro test results." : " Couldn't generate html report. Please install xunit-viewer"))
    }
}

