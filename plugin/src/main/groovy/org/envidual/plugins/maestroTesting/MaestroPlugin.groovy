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
        // maestro test configuration
        project.extensions.create('maestroTestOptions', MaestroPluginExtension)

        // find out if maestro is installed
        def existingMaestroPath = null
        def process = "which maestro".execute()
        process.waitFor()
        def foundPath = process.text.trim()
        if (path) {
            existingMaestroPath = path
            println "Found maestro installed at $existingMaestroPath"
        }

        // read configuration from the extension (always select the first non-null parameter from the list)
        // precedence: command line options > build.gradle file > system > default values
        def _device = selectParam(project.findProperty('device'),  project.maestroTestOptions.device)
        def _outputDirectory = selectParam(project.findProperty('outputDirectory'),  project.maestroTestOptions.outputDirectory)
        def _testDirectory = selectParam(project.findProperty('testDirectory'),  project.maestroTestOptions.testDirectory)
        def _emulatorOptions = selectParam(project.findProperty('emulatorOptions'),  project.maestroTestOptions.emulatorOptions, '-netdelay none -netspeed full -no-window -noaudio -no-boot-anim')
        def _sdkPath = selectParam(project.findProperty('sdkPath') ,project.maestroTestOptions.androidSdkPath, System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"), project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android")
        def _maestroPath = selectParam(project.findProperty('maestroPath'), project.maestroTestOptions.maestroPath, existingMaestroPath)
        def _apiLevel = selectParam(project.findProperty('apiLevel')?.toInteger, project.maestroTestOptions.apiLevel, 29)

        // register the tasks
        project.tasks.register('runMaestroTests', MaestroTestTask){
            device = _device
            outputDirectory = _outputDirectory
            testDirectory = _testDirectory
            sdkPath = _sdkPath
            emulatorOptions = _emulatorOptions
            maestroPath = _maestroPath
            dependsOn 'assemble'
        }
        project.tasks.register('installMaestro', InstallMaestroTask)
        project.tasks.register('installAndroidSdk', InstallAndroidSdkTask){
            sdkPath = _sdkPath
        }
        project.tasks.register('installAvd', InstallAvdTask) {
            device = _device
            sdkPath = _sdkPath
            apiLevel = _apiLevel
        }

    }

    /**
     * Select the first parameter in the list which isn't null
     * @param params
     * @return the first parameter which isn't null
     */
    static def selectParam(...params){
        for (param in params){
            if(param) return param
        }
        throw new IllegalArgumentException("No suitable non-null parameter found")
    }
}
