# Maestro Gradle Plugin
automating maestro UI tests with gradle
## Prerequisites
- [Maestro](https://maestro.mobile.dev/)
  install: `curl -Ls "https://get.maestro.mobile.dev" | bash`
- [XunitViewer](https://github.com/lukejpreston/xunit-viewer) (for generating html test teports, not required)
- Project with Java 11 or higher and Gradle 7.5 or higher
- Android Studio
- at least one virtual device installed locally
- currently only works for MacOS and Linux
## Configuring the plugin
- add ```id "org.envidual.maestroTesting" version "1.0.0"``` to the `plugins{}` section in the build.gradle file in the sub-project where the apk will be located (usually called app)
- configure the maestro tests:
    - **device**: name of the virtual device (as it is displayed in the device manager)
    - **testDirectory**: all maestro flow files in this directory will be executed. The path is relative to the sub-project directory.
    - **outputDirectory**: location of the test reports. Also relative to the sub-project directory.

Example configuration:

Groovy:
```Groovy
maestroTestOptions{  
  device = 'Pixel_6_API_33'  
  testDirectory = 'maestro'  
  outputDirectory = 'build/reports/tests/maestroResults'  
}
```
Kotlin:
```Kotlin
maestroTestOptions{  
  device.set("Pixel_6_API_33")  
  testDirectory.set("maestro")  
  outputDirectory.set("build/reports/tests/maestroResults")  
}
```
## Run the Maestro tests
currently, there is only one task available, which will start an emulator and run all maestro flows in the testDirectory:
```./gradlew runMaestroTests```
note: this task will also execute assemble in order to build the project

## Using the plugin with a local maven repository
Currently, the easiest way to use this plugin in any project is to publish it to a local maven repository.

#### Prerequisites:
- [Maven](https://maven.apache.org/)
#### Publish the Maven repository:
- clone this project
- run ```./gradlew clean build publishToMavenLocal ``` inside the project directory

#### Enable local Maven repositories in gradle:
add the following code to the *global* build.gradle file in your project:
```Groovy
pluginManagement {  
  repositories {  
	  mavenLocal()  
  }
}
  ```