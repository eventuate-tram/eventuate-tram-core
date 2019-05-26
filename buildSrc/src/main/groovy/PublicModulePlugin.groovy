import org.gradle.api.Plugin
import org.gradle.api.Project

class PublicModulePlugin implements Plugin<Project> {
    void apply(Project project) {

        project.ext.genjavadoc = true

        project.task("javadocJar", type: org.gradle.api.tasks.bundling.Jar, dependsOn: project.javadoc) {
            classifier = 'javadoc'
            from 'build/docs/javadoc'
//    manifest = defaultManifest()
        }

        project.task("sourcesJar", type: org.gradle.api.tasks.bundling.Jar) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
//    manifest = defaultManifest()
        }

        project.artifacts {
            archives project.jar
            archives project.javadocJar
            archives project.sourcesJar
        }
    }
}