package nl.jkva;

import com.google.common.io.Files;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.sonatype.aether.artifact.Artifact;

import java.io.File;
import java.io.IOException;

public class ArtifactCopier {
    static void copy(Artifact artifact, File toBaseDirectory, Log log) throws MojoExecutionException {
        File targetDir = new File(toBaseDirectory, "target");
        log.info("copy " + artifact + " to " + targetDir);

        if (targetDir.exists() && !targetDir.isDirectory()) {
            throw new MojoExecutionException("Target directory exists but is not a directory, aborting...");
        }

        try {
            File toFile = new File(targetDir, artifact.getFile().getName());
            Files.createParentDirs(toFile);
            Files.copy(artifact.getFile(), toFile);
            log.info("Copied file");
        } catch (IOException e) {
            throw new MojoExecutionException("Error", e);
        }
    }
}
