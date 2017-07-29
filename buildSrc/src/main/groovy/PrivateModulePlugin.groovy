import org.gradle.api.*

class PrivateModulePlugin implements Plugin<Project> {
    void apply(Project project) {

        project.task("sourcesJar", type: org.gradle.api.tasks.bundling.Jar) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
//    manifest = defaultManifest()
        }

        project.artifacts {
            archives project.jar
            archives project.sourcesJar
        }
    }
}