package org.envidual.plugins.maestroTesting

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional

abstract class InstallAvdTask extends DefaultTask {

    @Input
    abstract Property<String> getDevice()

    @Input
    abstract Property<Integer> getApiLevel()
    
    @Input
    @Optional
    abstract Property<String> getSdkPath()

    @TaskAction
    void installAvd() {
        def androidSdkPath = sdkPath.get()
        def sdkmanager = "${androidSdkPath}/cmdline-tools/latest/bin/sdkmanager"
        def avdmanager = "${androidSdkPath}/cmdline-tools/latest/bin/avdmanager"
        def androidHome = new File(androidSdkPath).getParent().toString()
        def androidEnvironment = [
                'ANDROID_HOME': androidHome,
                'ANDROID_SDK_ROOT': androidHome,
                'ANDROID_SDK_HOME': androidHome,
        ]
        def acceptLicenseInput = "y\n" * 10
        Utils.runCommands(project, androidHome, androidEnvironment,
                ["command": [sdkmanager, "platforms;android-${apiLevel.get()}"], "input": new ByteArrayInputStream("y\n".getBytes())],
                ["command": [sdkmanager, "system-images;android-${apiLevel.get()};default;x86", "--verbose"], "input": new ByteArrayInputStream(acceptLicenseInput.getBytes())],
                ["command": [avdmanager, "create", "avd", "--force", "-n", device.get(), "--abi", "default/x86","--package", "system-images;android-${apiLevel.get()};default;x86"], "input":new ByteArrayInputStream("no\n".getBytes())],
                ["command": [avdmanager, "list", "avd"]]
        )

    }
}