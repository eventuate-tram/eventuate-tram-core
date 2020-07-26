import org.gradle.api.Plugin
import org.gradle.api.Project

class EventuatePublishPlugin implements Plugin<Project> {


    void apply(Project rootProject) {


        rootProject.allprojects { project ->
            apply plugin: 'java'
            apply plugin: 'maven'
            apply plugin: 'com.jfrog.bintray'


            project.configurations {
                deployerJars
            }

            project.dependencies {
                deployerJars 'org.springframework.build:aws-maven:5.0.0.RELEASE'
            }

            if (!project.name.endsWith("-bom")) {

                project.uploadArchives {
                    repositories {
                        mavenDeployer {
                            configuration = configurations.deployerJars
                            repository(url: deployUrl) {
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
                            }
                        }
                    }
                }

            }

            project.bintray {
                publish = true
                user = System.getenv('BINTRAY_USER')
                key = System.getenv('BINTRAY_KEY')
                configurations = ['archives']
                pkg {
                    repo = "eventuate-maven-$bintrayRepoType"
                    name = project.bintrayPkgName
                    licenses = ['Apache-2.0']
                    vcsUrl = project.bintrayPkgVcsUrl
                }
            }


        }

        def publishTask = rootProject.task("publishEventuateArtifacts",
                type: PublishEventuateArtifactsTask,
                group: 'build setup',
                description: "Publish Eventuate Artifacts")


    }
}