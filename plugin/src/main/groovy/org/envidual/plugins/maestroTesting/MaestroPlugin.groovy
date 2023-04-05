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
            //use options set by the -P command line parameters or maestroTestOptions block in build.gradle
            device = project.getProperty('device') ?: project.maestroTestOptions.device
            outputDirectory = project.getProperty('outputDirectory') ?: project.maestroTestOptions.outputDirectory
            testDirectory = project.getProperty('testDirectory') ?: project.maestroTestOptions.testDirectory
            dependsOn 'assemble'
        }
    }
}
