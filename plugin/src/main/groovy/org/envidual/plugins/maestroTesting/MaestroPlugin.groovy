package org.envidual.plugins.maestroTesting

import org.gradle.api.Project
import org.gradle.api.Plugin


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

        // register the tasks and choose values for the properties
        // the precedence is always: command line options > build.gradle file > system values already present > default values
        project.tasks.register('runMaestroTests', MaestroTestTask){
            device = selectParam(project.findProperty('device'),  getExtensionProperty(project, 'device'))
            outputDirectory = selectParam(project.findProperty('outputDirectory'),  getExtensionProperty(project, 'outputDirectory'))
            testDirectory = selectParam(project.findProperty('testDirectory'),  getExtensionProperty(project, 'testDirectory'))
            sdkPath = selectParam(project.findProperty('androidSdkPath') ,getExtensionProperty(project,'androidSdkPath'), /**System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"),*/ project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android/Sdk")
            emulatorOptions = selectParam(project.findProperty('emulatorOptions'),  getExtensionProperty(project,'emulatorOptions'), '-netdelay none -netspeed full -no-window -noaudio -no-boot-anim')
            maestroPath = selectParam(existingMaestroPath, project.findProperty('maestroPath'), getExtensionProperty(project,'maestroPath'), "${System.getProperty('user.home')}/.maestro/bin/maestro")
            dependsOn 'assemble' //make sure the project is built before it is installed and run on an emulator
        }
        project.tasks.register('installMaestro', InstallMaestroTask)
        project.tasks.register('installAndroidSdk', InstallAndroidSdkTask){
            sdkPath = selectParam(project.findProperty('androidSdkPath'), getExtensionProperty(project,'androidSdkPath'), /**System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"),*/ project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android/Sdk")
        }
        project.tasks.register('installAvd', InstallAvdTask) {
            device = selectParam(project.findProperty('device'),  getExtensionProperty(project,'device'))
            sdkPath = selectParam(project.findProperty('androidSdkPath') ,getExtensionProperty(project,'androidSdkPath'), /**System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"),*/ project.android?.sdkDirectory?.getAbsolutePath(), "${System.getProperty('user.home')}/Android/Sdk")
            apiLevel = selectParam(project.findProperty('apiLevel')?.toInteger(), getExtensionProperty(project,'apiLevel'), 29)
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

    /**
     * convenience method for returning null if a property hasn't been configured in the extension
     * because it doesn't do that by default for some reason
     */
    static def getExtensionProperty(Project project, String property){
        if(project.hasProperty("maestroTestOtions.$property")){
            return project.maestroTestOption.getProperty(property)
        }
        else{
            return null
        }
    }
}
