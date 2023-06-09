/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package org.envidual.plugins.maestroTesting

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import spock.lang.Specification

/**
 * A simple unit test for the 'maestro.plugin.greeting' plugin.
 */
class MaestroPluginPluginTest extends Specification {
    def "plugin registers task"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply("maestro.plugin.greeting")

        then:
        project.tasks.findByName("greeting") != null
    }
}
