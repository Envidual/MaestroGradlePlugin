package org.envidual.plugins.maestroTesting

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.provider.Property

abstract class InstallAndroidSdkTask extends DefaultTask {

    @Input
    abstract Property<String> getSdkPath()

    @TaskAction
    void installAndroidSdk() {
        def commandLineToolsVersion = "9477386"
        def buildToolsVersion = "33.0.2"
        //check if Android sdk is already installed at the specified path
        if (new File(sdkPath.get() + "/cmdline-tools/latest/bin/sdkmanager").exists()) {
            println "Android SDK is installed at ${sdkPath.get()}. Nothing to do here."
        } else {
            println "Android SDK is not installed at ${sdkPath.get()}. Installing Android Sdk..."
            def commandLineToolsUrl
            // set the installation url for command line tools according to the operating system
            switch (Utils.getOS()) {
                case OperatingSystem.LINUX:
                    println "running on Linux"
                    commandLineToolsUrl = "https://dl.google.com/android/repository/commandlinetools-linux-${commandLineToolsVersion}_latest.zip"
                    break
                case OperatingSystem.MACOS:
                    println "running on MacOS"
                    commandLineToolsUrl = "https://dl.google.com/android/repository/commandlinetools-mac-${commandLineToolsVersion}_latest.zip"
                    break
                default:
                    throw new GradleException("Error: only MacOS and Linux are currently supported")
            }
            // install android command line tools
            println "installing command line tools "
            def dir = new File("${sdkPath.get()}/cmdline-tools")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            Utils.runCommands(project, "${System.getProperty('user.home')}", [:],
                    ["command": ["curl", "-o", "commandLineTools.zip", "-sS", commandLineToolsUrl]],
                    ["command": ["unzip", "commandLineTools.zip", "-d", "${sdkPath.get()}/cmdline-tools"]]
            )
            def cmdLineToolsDir = new File("${sdkPath.get()}/cmdline-tools/cmdline-tools")
            cmdLineToolsDir.renameTo("${sdkPath.get()}/cmdline-tools/latest")
        }
        // install build tools and platform tools
        println "Installing Build and Platform Tools"
        def sdkmanager = "${sdkPath.get()}/cmdline-tools/latest/bin/sdkmanager"
        def androidHome = new File(sdkPath.get()).getParent().toString()
        def androidEnvironment = [
                'ANDROID_HOME': androidHome,
                'ANDROID_SDK_ROOT': androidHome,
                'ANDROID_SDK_HOME': androidHome,
        ]
        def acceptLicenseInput = "y\n" * 10
        Utils.runCommands(project, "${sdkPath.get()}", androidEnvironment,
                ["command": ["bash", "-c", "echo \$ANDROID_HOME"]],
                ["command": [sdkmanager, "--licenses"], "input": new ByteArrayInputStream(acceptLicenseInput.getBytes())],
                ["command": [sdkmanager, "--install", "build-tools;${buildToolsVersion}", "platform-tools"]],
                ["command": [sdkmanager, "emulator"]])

    }
}