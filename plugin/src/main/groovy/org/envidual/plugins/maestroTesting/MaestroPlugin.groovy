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
        if (foundPath) {
            existingMaestroPath = foundPath
            println "Found maestro installed at $existingMaestroPath"
        }

        // read configuration from the extension (always select the first non-null parameter from the list)
        // precedence: command line options > build.gradle file > system > default values
//        def _device = selectParam(project.findProperty('device'),  project.maestroTestOptions.device)
//        def _outputDirectory = selectParam(project.findProperty('outputDirectory'),  project.maestroTestOptions.outputDirectory)
//        def _testDirectory = selectParam(project.findProperty('testDirectory'),  project.maestroTestOptions.testDirectory)
//        def _emulatorOptions = selectParam(project.findProperty('emulatorOptions'),  project.maestroTestOptions.emulatorOptions, '-netdelay none -netspeed full -no-window -noaudio -no-boot-anim')
//        def _sdkPath = selectParam(project.findProperty('sdkPath') ,project.maestroTestOptions.androidSdkPath, System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"), project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android")
//        def _maestroPath = selectParam(existingMaestroPath, project.findProperty('maestroPath'), project.maestroTestOptions.maestroPath, "${System.getProperty('user.home')}/.maestro/bin/maestro")
//        def _apiLevel = selectParam(project.findProperty('apiLevel')?.toInteger(), project.maestroTestOptions.apiLevel, 29)
//
//        //TEST
//        println "PROJECT PROPERTY: ${project.findProperty('sdkPath')}"
//        println "EXT PROPERTY: ${project.maestroTestOptions.androidSdkPath}"
//        println "SDK PATH: ${_sdkPath} END SDK PATH"

        // register the tasks
        project.tasks.register('runMaestroTests', MaestroTestTask){
            device = selectParam(project.findProperty('device'),  getExtensionProperty(project, 'device'))
            outputDirectory = selectParam(project.findProperty('outputDirectory'),  getExtensionProperty(project, 'outputDirectory'))
            testDirectory = selectParam(project.findProperty('testDirectory'),  getExtensionProperty(project, 'testDirectory'))
            sdkPath = selectParam(project.findProperty('sdkPath') ,getExtensionProperty(project,'androidSdkPath'), /**System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"),*/ project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android")
            emulatorOptions = selectParam(project.findProperty('emulatorOptions'),  getExtensionProperty(project,'emulatorOptions'), '-netdelay none -netspeed full -no-window -noaudio -no-boot-anim')
            maestroPath = selectParam(existingMaestroPath, project.findProperty('maestroPath'), getExtensionProperty(project,'maestroPath'), "${System.getProperty('user.home')}/.maestro/bin/maestro")
            dependsOn 'assemble'
        }
        project.tasks.register('installMaestro', InstallMaestroTask)
        project.tasks.register('installAndroidSdk', InstallAndroidSdkTask){
            sdkPath = selectParam(project.findProperty('sdkPath'), getExtensionProperty(project,'androidSdkPath'), /**System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"),*/ project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android")
        }
        project.tasks.register('installAvd', InstallAvdTask) {
            device = selectParam(project.findProperty('device'),  getExtensionProperty(project,'device'))
            sdkPath = selectParam(project.findProperty('sdkPath') ,getExtensionProperty(project,'androidSdkPath'), /**System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"),*/ project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android")
            apiLevel = selectParam(project.findProperty('apiLevel')?.toInteger(), getExtensionProperty(project,'apiLevel'), 29)
        }
    }

    /**
     * Select the first parameter in the list which isn't null
     * @param params
     * @return the first parameter which isn't null
     */
    static def selectParam(...params){
        //DEBUG
        println "select params called with ${params}"
        for (param in params){
            if(param) return param
        }
        throw new IllegalArgumentException("No suitable non-null parameter found")
    }

    /**
     * convenience method for returning null if a property hasn't been configured in the extension
     */
    static def getExtensionProperty(Project project, String property){
        if(project.hasProperty("maestroTestOtions.$property")){
            println "PROPERTY $property available"
            return project.maestroTestOption.getProperty(property)
        }
        else{
            println "PROPERTY $property NOT available"
            return null
        }



        return project.maestroTestOptions.hasProperty(property) ? project.maestroTestOptions.getProperty(property) : null
    }
}
