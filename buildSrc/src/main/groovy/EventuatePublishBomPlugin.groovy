import org.gradle.api.Plugin
import org.gradle.api.Project

class EventuatePublishBomPlugin implements Plugin<Project> {


    void apply(Project project) {

        project.uploadArchives {
            repositories {
                mavenDeployer {
                    configuration = project.configurations.deployerJars
                    repository(url: project.deployUrl) {
                        authentication(userName: System.getenv('S3_REPO_AWS_ACCESS_KEY'), password: System.getenv('S3_REPO_AWS_SECRET_ACCESS_KEY'))
                    }
                    pom.project {
                        licenses {
                            license {
                                name 'The Apache Software License, Version 2.0'
                                url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                distribution 'repo'
                            }
                        }
                        dependencyManagement {
                            dependencies {
                                project.parent.subprojects.sort { "$it.name" }.findAll { !it.name.endsWith("-bom") }.each { dep ->
                                    dependency {
                                        groupId = project.group
                                        artifactId = dep.name
                                        version = project.version
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        def installer = project.install.repositories.mavenInstaller
        def deployer = project.uploadArchives.repositories.mavenDeployer

        [installer, deployer]*.pom*.whenConfigured { pom ->
            pom.version = project.version
            pom.packaging = "pom"
        }

    }
}