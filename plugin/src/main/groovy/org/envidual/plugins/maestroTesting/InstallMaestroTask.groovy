package org.envidual.plugins.maestroTesting

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class InstallMaestroTask extends DefaultTask {
    @TaskAction
    void installMaestro() {
        runCommands(project, "${System.getProperty('user.home')}", ["MAESTRO_VERSION": "1.23.0"],
                ["command": ["curl", "-o", "maestroInstallScript.sh", "-Ls", "https://get.maestro.mobile.dev"]],
                ["command": ["/bin/bash", "./maestroInstallScript.sh"]],
                ["command": ["npm", "i", "-g", "xunit-viewer"]]
        )
        def tmpFile = new File("${System.getProperty('user.home')}/maestroInstallScript.sh")
        tmpFile.delete()
        project.maestroTestOptions.maestroPath = ":${System.getProperty('user.home')}/.maestro/bin"
    }
}