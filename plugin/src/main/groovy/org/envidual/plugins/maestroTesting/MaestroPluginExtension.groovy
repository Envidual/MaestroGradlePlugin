package org.envidual.plugins.maestroTesting

import org.gradle.api.provider.Property


interface MaestroPluginExtension {
    Property<String> getDevice()
    Property<Integer> getApiLevel() //Temporary
    Property<String> getOutputDirectory()
    Property<String> getTestDirectory()
    Property<String> getAndroidSdkPath()
    Property<String> getMaestroPath()
    Property<ArrayList<String>> getEmulatorOptions()
}