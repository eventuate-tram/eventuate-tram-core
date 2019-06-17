package io.eventuate.gradleplugins.versions

import org.gradle.api.Plugin;
import org.gradle.api.Project;
//import org.apache.commons.io.FileUtils;

public class UpgradeVersions implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        List<String> lines = FileUtils.readLines(new File("./gradle.properties"), (String)null);

        project.task("upgradeVersions", type: UpgradeVersionsTask)
    }
}
