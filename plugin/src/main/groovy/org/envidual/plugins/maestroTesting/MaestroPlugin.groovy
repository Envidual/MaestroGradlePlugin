package org.envidual.plugins.maestroTesting

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * Plugin for running maestro tests
 */
class MaestroPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        //maestro test configuration
        project.extensions.create('maestroTestOptions', MaestroPluginExtension)
        // task to run the maestro tests
        project.tasks.register('runMaestroTests', MaestroTestTask){
            device = project.maestroTestOptions.device
            outputDirectory = project.maestroTestOptions.outputDirectory
            testDirectory = project.maestroTestOptions.testDirectory
            dependsOn 'assemble'
        }
    }
}
