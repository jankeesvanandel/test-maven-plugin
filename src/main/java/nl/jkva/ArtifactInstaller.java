package nl.jkva;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.util.List;

public class ArtifactInstaller {
    static Artifact downloadArtifact(String artifactCoords, List<RemoteRepository> remoteRepos, RepositorySystem repoSystem, RepositorySystemSession repoSession, Log log) throws MojoFailureException, MojoExecutionException {
        Artifact artifact;
        try {
            artifact = new DefaultArtifact(artifactCoords);
        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);

        log.info("Resolving artifact " + artifact + " from " + remoteRepos);

        ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(repoSession, request);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        File file = result.getArtifact().getFile();
        log.info("Resolved artifact " + artifact + " to " + file + " from " + result.getRepository());
        return result.getArtifact();
    }
}
