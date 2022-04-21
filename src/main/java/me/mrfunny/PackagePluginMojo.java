package me.mrfunny;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
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
import java.util.List;

@Mojo(name = "spigot-package", defaultPhase = LifecyclePhase.PACKAGE)
public class PackagePluginMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(property = "pluginsFolder", required = true)
    String pluginsFolder;

    @Parameter(property = "host", required = false)
    String host;

    @Parameter(property = "port", required = false)
    String port;

    @Parameter(property = "username", required = false)
    String username;

    @Parameter(property = "password", required = false)
    String password;

    @Parameter(property = "executeAfter", required = false)
    String executeAfter;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // check if credentials are set
        File outputFile = new File(project.getBuild().getDirectory(), project.getArtifactId() + "-"+ project.getVersion() + ".jar");
        if (outputFile.exists()) {
            if (username != null && password != null && host != null && port != null) {
                // connect to ssh via SSHJ SSHClient
                SSHClient ssh = new SSHClient();
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                try {
                    ssh.connect(host, Integer.parseInt(port));
                    ssh.authPassword(username, password);
                    SFTPClient sftpClient = ssh.newSFTPClient();
                    // upload jar file
                    sftpClient.put(outputFile.getAbsolutePath(), pluginsFolder + "/" + project.getArtifactId() + "-"+ project.getVersion() + ".jar");
                    sftpClient.close();
                    if(executeAfter != null) {
                        Session session = ssh.startSession();
                        session.exec(executeAfter);
                        session.close();
                    }
                    ssh.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    System.out.println("Copying plugin to " + pluginsFolder);
                    File pluginsFile = new File(this.pluginsFolder, project.getArtifactId() + "-"+ project.getVersion() + ".jar");
                    Files.copy(outputFile.toPath(), pluginsFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new MojoExecutionException("Output file does not exist. " + outputFile.getAbsolutePath());
        }


    }
}
