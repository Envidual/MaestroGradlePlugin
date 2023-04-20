# Maestro Gradle Plugin
automating maestro UI tests with gradle
## Prerequisites
- [Maestro](https://maestro.mobile.dev/)
  (install: `curl -Ls "https://get.maestro.mobile.dev" | bash`)
- [XunitViewer](https://github.com/lukejpreston/xunit-viewer) (for generating html test teports, not required)
- Project with Java 11 or higher and Gradle 7.5 or higher
- Android Studio
- at least one virtual device installed
- currently only works for MacOS and Linux
## Configuring the plugin
- add ```id "org.envidual.maestroTesting" version "1.1.6"``` to the `plugins{}` section in the build.gradle file in the sub-project where the apk will be located (usually called app)
- configure the maestro tests:
    - **device**: name of the virtual device to be used for testing (as it is displayed in the device manager). If the device is set to "", the task will not create an emulator itself
    - **apiLevel**: API level for the device (only necessary for installAvd)
    - **testDirectory**: all maestro flow files in this directory will be executed. The path is relative to the sub-project directory.
    - **outputDirectory**: location of the test reports. Also relative to the sub-project directory.
    - **androidSdkPath**: location of the Android Sdk. If Android Sdk is already installed via android studio or the ANDROID_HOME or ANDROID_SDK_ROOT environment variables are set correctly, this parameter can be left out and the existing Android Sdk will be used. If this option is set, installAndroidSdk will search for an existing installation at the specified location and install it there if none is found.
    - **maestroPath**: location of the maestro executable. If maestro is already installed and included in PATH on your system, this installation will be used and there is no need to specify this option. Note: installMaestro will not use this location and always place maestro in $HOME/.maestro
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

Command line: 
```./gradlew --no-daemon installAndroidSdk installMaestro installAvd runMaestroTests -Pdevice="TestDevice" -PtestDirectory="maestro" -PoutputDirectory="maestroResults" -PapiLevel=29 -PemulatorOptions="-no-window -gpu swiftshader_indirect -no-snapshot -noaudio -no-boot-anim"```

Note: command line options overwrite the values specified in the build.gradle file. This is especially useful if the configurations for local testing and in a pipeline differ


currently, there is only one task available, which will start an emulator and run all maestro flows in the testDirectory:
```./gradlew runMaestroTests```

Note: this task will also execute ```assemble``` in order to build the project

## Using the plugin from github packages 
**Locally**
- add to your `settings.gradle` file:
  ```Groovy
  pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/envidual/maestro-testing")
            credentials {
                username= System.getenv('GITHUB_USER') ?: githubuser
                password= System.getenv('GITHUB_TOKEN') ?: githubtoken
            }
        }
    }
  }
  ```
- add to your `gradle.properties` file:
  ```
  githubuser=<your_github_username>
  githubtoken=<your_github_token>
  ```
  OR set the environment variables GITHUB_USER and GITHUB_TOKEN accordingly

**In a Pipeline**
It is not recommended to add your local gradle.properties file to the github repository. Instead, the environment variables should be set in a safe way.
Example for Github Actions:
- modify the `settings.gradle` file as above
- add your github username nad token to the repository secrets, for example as USERNAME and TOKEN
  - add the secrets to your environment in the Github Actions script:
    ```Yaml
    jobs:
    my-job:
      env:
        GITHUB_USERNAME: ${{ secrets.USERNAME }}
        GITHUB_TOKEN: ${{ secrets.TOKEN }}
    ```

Note: The github token needs to have read access to packages

## Using the plugin with a local maven repository

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