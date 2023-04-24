# Maestro Gradle Plugin
Automating maestro UI tests with gradle
## Prerequisites
- currently only works for MacOS and Linux
## Configuring the plugin
- add ```id "org.envidual.maestroTesting" version "1.2.1"``` to the `plugins{}` section in the build.gradle file in the sub-project where the apk will be located (usually called app)
- configure the maestro tests:
  - **device**: name of the virtual device to be used for testing (as it is displayed in the device manager). If the device is set to "", the task will not create an emulator itself
  - **apiLevel**: API level for the device (used by installAvd)
  - **testDirectory**: all maestro flow files in this directory will be executed. The path is relative to the sub-project directory.
  - **outputDirectory**: location of the test reports. Also relative to the sub-project directory.
  - **emulatorOptions**: options passed to the emulator. The default value is `'-netdelay none -netspeed full -no-window -noaudio -no-boot-anim'`. Leave out the -no-window option to watch the tests being executed. More information about possible options can be found [here](https://developer.android.com/studio/run/emulator-commandline)
  - **androidSdkPath**: location of the Android Sdk. If Android Sdk is already installed via Android Studio or the ANDROID_HOME or ANDROID_SDK_ROOT environment variables are set correctly, this parameter can be left out and the existing Android Sdk will be used. If this option is set, installAndroidSdk will search for an existing installation at the specified location and install it there if none is found.
  - **maestroPath**: location of the maestro executable. If maestro is already installed and included in PATH on your system, this installation will be used and there is no need to specify this option. Note: installMaestro will not use this location and always place maestro in `$HOME/.maestro`

These options can either be set in the build.gradle file as well in a `maestroTestOptions{}` block or be passed to gradle as command line optioins of the form `-P<option_name>`. Command line options overwrite those from the build.gradle file, so you can have different options for local tests and in a pipeline.

**Example configuration:**

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
```
  ./gradlew --no-daemon installAndroidSdk installMaestro installAvd runMaestroTests -Pdevice="TestDevice" -PtestDirectory="maestro" -PoutputDirectory="maestroResults" -PapiLevel=29 -PemulatorOptions="-no-window -gpu swiftshader_indirect -no-snapshot -noaudio -no-boot-anim"
```

## Available Tasks
#### runMaestroTests
Runs all maestro tests from flow files located in `testDirectory`. Unless the device is set to `"`, an emulator with the avd specified by `device` is started by the task. Avds can be either downloaded with the installAvd task or directly created in the Android Studio Device manager, set `device` to the name displayed in the device manager (recommended for local tests). If the device is set to `""`, maestro will detect a running emulator and execute the tests on it. For example, the integrated emulator of Android Studio can be used for this.
`
#### installAndroidSdk
Installs the android Sdk in the directory specified by `androidSdkPath` if it doesn't exist there already. For local test, it is recommended to use the Android Sdk already installed with Android Studio instead of running this task. If `androidSdkPath` isn't set, the default location is `$HOME/Android/Sdk`.

#### installMaestro
Installs [Maestro](https://maestro.mobile.dev/) (Version 23) and [XunitViewer](https://github.com/lukejpreston/xunit-viewer) (used for generating html reports). The installation location is always `$HOME/.maestro`. You can also download the latest Maestro Version with `curl -Ls "https://get.maestro.mobile.dev" | bash`. This task exists primarily for usage in a pipelina as newer maestro versions don't work there.

#### installAvd
Creates an Avd (Android Virtual Device) with name `device` and api level `apiLevel`. For local tests it is recommended to install avds with the built-in device manager of Android Studio instead.

## Using the plugin from github packages
### Locally
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

### In a Pipeline

It is not recommended to add your local `gradle.properties` file to the github repository. Instead, the environment variables should be set in a safe way.

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