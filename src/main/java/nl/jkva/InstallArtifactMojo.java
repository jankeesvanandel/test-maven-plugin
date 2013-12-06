package nl.jkva;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * @goal resolve-artifact
 * @requiresProject false
 * @requiresDependencyResolution runtime
 */
public class InstallArtifactMojo extends AbstractMojo {
    /**
     * The project currently being build.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;

    /**
     * The current Maven session.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession mavenSession;

    /**
     * The Maven BuildPluginManager component.
     *
     * @component
     * @required
     */
    private BuildPluginManager pluginManager;

    /**
     * Base directory of the project.
     * @parameter expression="${basedir}"
     */
    private File baseDirectory;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> remoteRepos;

    /**
     * The {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} of the artifact to resolve.
     *
     * @parameter expression="${aether.artifactCoords}"
     */
    private String artifactCoords1 = "javax.jms:jms-api:1.1-rev-1";

    public void execute()
            throws MojoExecutionException, MojoFailureException {
        //Workaround for having a ${project.basedir} in a project-less mojo. This pom.xml does not exist
        mavenProject.setFile(new File(baseDirectory, "pom.xml"));
        mavenProject.getProperties().setProperty("project.build.sourceEncoding", "UTF-8");
        getLog().error("" + baseDirectory);

        File file = new File(baseDirectory, "config.xml");
        List<XmlReader.ArtifactConfig> configs = XmlReader.listAllArtifacts(file);
        for (XmlReader.ArtifactConfig config : configs) {
            String artifactCoords = createArtifactCoords(config);
            Artifact artifact = ArtifactInstaller.downloadArtifact(artifactCoords, remoteRepos, repoSystem, repoSession, getLog());
            ArtifactCopier.copy(artifact, baseDirectory, getLog());
        }

        Element directory = new Element("directory", "${project.basedir}/properties");
        Element filtering = new Element("filtering", "true");
        Element element = new Element("resource", directory, filtering);

        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-resources-plugin"),
                        version("2.6")
                ),
                goal("copy-resources"),
                configuration(
                        element("resources", element),
                        element("outputDirectory", "${project.basedir}/target/foo")
                ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                )
        );
    }

    private String createArtifactCoords(XmlReader.ArtifactConfig config) {
        return String.format("%s:%s:%s", config.groupId, config.artifactId, config.version);
    }

}
