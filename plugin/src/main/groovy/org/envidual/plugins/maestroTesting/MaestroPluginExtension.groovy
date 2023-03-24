package org.envidual.plugins.maestroTesting

import org.gradle.api.provider.Property


interface MaestroPluginExtension {
    Property<String> getDevice()
    Property<String> getOutputDirectory()
    Property<String> getTestDirectory()
}