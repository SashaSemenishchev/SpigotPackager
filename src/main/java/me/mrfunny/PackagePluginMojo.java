package me.mrfunny;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mojo(name = "spigot-package", defaultPhase = LifecyclePhase.COMPILE)
public class PackagePluginMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(property = "pluginsFolder", required = true)
    String pluginsFolder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("Copying plugin to " + pluginsFolder);
        File outputFile = new File(project.getBuild().getDirectory(), project.getArtifactId() + "-"+ project.getVersion() + ".jar");
        File pluginsFile = new File(this.pluginsFolder, project.getArtifactId() + "-"+ project.getVersion() + ".jar");
        if (outputFile.exists()) {
            try {
                Files.copy(outputFile.toPath(), pluginsFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new MojoFailureException("File not found: " + outputFile.getAbsolutePath());
        }
    }
}
