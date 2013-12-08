package nl.jkva;

import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Created by jankeesvanandel on 08-12-13.
 */
public class MavenProjectFaker {
    static void fakeMavenProject(MavenProject mavenProject, File baseDirectory) {
        //Workaround for having a ${project.basedir} in a project-less mojo. This pom.xml does not exist
        mavenProject.setFile(new File(baseDirectory, "pom.xml"));
        mavenProject.getProperties().setProperty("project.build.sourceEncoding", "UTF-8");
    }
}
