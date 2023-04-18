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
            androidSdkPath = project.hasProperty('androidSdkPath') ? project.getProperty('androidSdkPath') : (project.maestroTestOptions.androidSdkPath ? project.maestroTestOptions.androidSdkPath : project.android.sdkDirectory.getAbsolutePath())
            emulatorOptions = project.hasProperty('emulatorOptions') ? project.getProperty('emulatorOptions') : (project.maestroTestOptions.emulatorOptions ? project.maestroTestOptions.emulatorOptions : ['-netdelay', 'none', '-netspeed', 'full'])
            dependsOn 'assemble'
        }
        project.tasks.register('installMaestro', InstallMaestroTask)
        project.tasks.register('installAndroidSdk', InstallAndroidSdkTask)
        project.tasks.register('installAvd', InstallAvdTask){
            device = project.hasProperty('device') ? project.getProperty('device') : project.maestroTestOptions.device
            androidSdkPath = project.hasProperty('androidSdkPath') ? project.getProperty('androidSdkPath') : (project.maestroTestOptions.androidSdkPath ? project.maestroTestOptions.androidSdkPath : project.android.sdkDirectory.getAbsolutePath())
            apiLevel = project.hasProperty('apiLevel') ? project.getProperty('apiLevel') : (project.maestroTestOptions.apiLevel ? project.maestroTestOptions.apiLevel : 29)
        }
    }
}
