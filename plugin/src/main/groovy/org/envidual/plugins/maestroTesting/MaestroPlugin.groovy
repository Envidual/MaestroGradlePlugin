package org.envidual.plugins.maestroTesting

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.provider.Property


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
            //set default value for android sdk path
            if(!project.maestroTestOptions.androidSdkPath){
                project.maestroTestOptions = project.android.sdkDirectory.getAbsolutePath()
            }
            //use options set by the -P command line parameters or maestroTestOptions block in build.gradle
            device = project.hasProperty('device') ? project.getProperty('device') : project.maestroTestOptions.device
            outputDirectory = project.hasProperty('outputDirectory') ? project.getProperty('outputDirectory') : project.maestroTestOptions.outputDirectory
            testDirectory = project.hasProperty('testDirectory') ? project.getProperty('testDirectory') : project.maestroTestOptions.testDirectory
            androidSdkPath = project.hasProperty('androidSdkPath') ? project.getProperty('androidSdkPath') : project.maestroTestOptions.androidSdkPath
            dependsOn 'assemble'
        }
    }
}
