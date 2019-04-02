package edu.ouc.maven.plugin;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generate java classes from Protocol Buffers files using protoc.
 */
@Mojo(name = "protoc", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ProtocMojo extends AbstractMojo {

    @Parameter(defaultValue = "protoc")
    private String protocCmd;

    @Parameter(required = true)
    private String protocVersion;

    @Parameter(required = true)
    private List<String> sources;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/java")
    private String output;

    public void execute() throws MojoExecutionException, MojoFailureException {
        new ProtocExecutor(this, protocCmd, protocVersion, sources, output).execute();
    }
}
