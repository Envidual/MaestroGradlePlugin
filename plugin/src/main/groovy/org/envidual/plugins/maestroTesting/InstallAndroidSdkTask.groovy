package org.envidual.plugins.maestroTesting

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.provider.Property

abstract class InstallAndroidSdkTask extends DefaultTask {

    @Input
    Property<String> sdkPath

    @TaskAction
    void installAndroidSdk() {
        //check if Android sdk is already installed at the specified path
        if (new File(sdkPath.get() + "/platform-tools/adb").exists()) {
            println "Android SDK is installed at $sdkPath. Nothing to do here."
        } else {
            println "Android SDK is not installed at $sdkPath. Installing Android Sdk..."
            def commandLineToolsVersion = "9477386"
            def buildToolsVersion = "33.0.2"
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
            def dir = new File("${sdkPath.get()}/Sdk/cmdline-tools")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            Utils.runCommands(project, "${System.getProperty('user.home')}", [:],
                    ["command": ["curl", "-o", "commandLineTools.zip", "-sS", commandLineToolsUrl]],
                    ["command": ["unzip", "commandLineTools.zip", "-d", "${sdkPath.get()}/Sdk/cmdline-tools"]]
            )
            def cmdLineToolsDir = new File("${sdkPath.get()}/Sdk/cmdline-tools/cmdline-tools")
            cmdLineToolsDir.renameTo("${sdkPath.get()}/Sdk/cmdline-tools/latest")

            // install build tools and platform tools
            def sdkmanager = "${sdkPath.get()}/Sdk/cmdline-tools/latest/bin/sdkmanager"
            def androidEnvironment = [
                    'ANDROID_HOME'    : "${System.getProperty('user.home')}/Android".toString(),
                    'ANDROID_SDK_ROOT': "${System.getProperty('user.home')}/Android".toString(),
                    'ANDROID_SDK_HOME': "${System.getProperty('user.home')}/Android".toString()
            ]
            def acceptLicenseInput = "y\n" * 10
            Utils.runCommands(project, "${sdkPath.get()}/Sdk", androidEnvironment,
                    ["command": ["bash", "-c", "echo \$ANDROID_HOME"]],
                    ["command": [sdkmanager, "--licenses"], "input": new ByteArrayInputStream(acceptLicenseInput.getBytes())],
                    ["command": [sdkmanager, "--install", "build-tools;${buildToolsVersion}", "platform-tools"]])
        }
    }
}