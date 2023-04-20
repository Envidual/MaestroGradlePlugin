package org.envidual.plugins.maestroTesting

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

interface MaestroPluginExtension {
    Property<String> getDevice()
    Property<Integer> getApiLevel() //Temporary
    Property<String> getOutputDirectory()
    Property<String> getTestDirectory()
    Property<String> getAndroidSdkPath()
    Property<String> getMaestroPath()
    Property<String> getEmulatorOptions()
}