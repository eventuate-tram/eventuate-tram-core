import org.gradle.StartParameter
import org.gradle.api.tasks.GradleBuild

class PublishEventuateArtifactsTask extends GradleBuild {

    def executeCommand(command) {
        def lastLine = ""
        def proc = command.execute()
        proc.in.eachLine { line -> lastLine = line }
        proc.err.eachLine { line -> println line }
        proc.waitFor()
        lastLine
    }

    def gitBranch() {
        executeCommand("git rev-parse --abbrev-ref HEAD")
    }

    PublishEventuateArtifactsTask() {

        def branch = gitBranch()

        if (branch == "master") {

            def version = project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")

            def sp = new StartParameter()
            sp.getProjectProperties().put("version", version)
            sp.getProjectProperties().put("deployUrl", System.getenv("S3_REPO_DEPLOY_URL"))

            setStartParameter(sp)
            setTasks(["uploadArchives"])

        } else {

            def bintrayRepoType = determineRepoType(branch)
            def sp = new StartParameter()
            sp.getProjectProperties().put("version", branch)
            sp.getProjectProperties().put("bintrayRepoType", bintrayRepoType)
            sp.getProjectProperties().put("deployUrl", "https://dl.bintray.com/eventuateio-oss/eventuate-maven-${bintrayRepoType}")

            setTasks(["testClasses", "bintrayUpload"])

        }
    }

    static String determineRepoType(String branch) {
        if (branch ==~ /.*RELEASE$/)
            return "release"
        if (branch ==~ /.*M[0-9]+$/)
            return "milestone"
        if (branch ==~ /.*RC[0-9]+$/)
            return "rc"
        throw new RuntimeException("cannot figure out bintray for this branch $branch")
    }
}
