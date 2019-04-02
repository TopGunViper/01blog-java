package edu.ouc.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Execute protoc command.
 */
public class ProtocExecutor {
    Mojo mojo;

    private String protocCmd;
    private String protocVersion;
    private List<String> sources;
    private String output;

    public ProtocExecutor(Mojo mojo, String protocCmd, String protocVersion, List<String> sources, String output) {
        this.mojo = mojo;
        this.protocCmd = protocCmd;
        this.protocVersion = protocVersion;
        this.sources = sources;
        this.output = output;
    }

    /**
     * Execute protoc.
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        // check version
        String versionCmd = protocCmd + " --version";
        String actualVersion;
        try {
            actualVersion = execCmd(versionCmd);
            actualVersion = actualVersion.split(" ")[1];
        } catch (Exception e) {
            mojo.getLog().error("Error occur during checking version.", e);
            throw new MojoExecutionException("");
        }
        if (!protocVersion.equals(actualVersion)) {
            mojo.getLog().error("");
            throw new MojoExecutionException(String.format("Expected version:%s, actual:%s"
                    , protocVersion, actualVersion));
        }

        // generate source code
        StringBuilder cmd = new StringBuilder();
        cmd.append(protocCmd)
                .append(" --java_out=")
                .append(output)
                .append(" ")
                .append(" --proto_path=")
                .append(sources.get(0).substring(0, sources.get(0).lastIndexOf("/")))
                .append(" ")
                .append(sources.get(0));

        mojo.getLog().info(String.format("CMD:%s", cmd.toString()));

        File outputFile = new File(output);
        if (!outputFile.exists() && !outputFile.mkdirs()) {
            throw new MojoExecutionException(String.format("Can't create output:%s dir.", output));
        }
        try {
            String res = execCmd(cmd.toString());
            mojo.getLog().info(String.format("res:%s", res));
        } catch (Exception e) {
            mojo.getLog().error("Error occur during generating source code.", e);
            throw new MojoExecutionException("");
        }

        mojo.getLog().info("Successfully generates source code.");
    }

    private String execCmd(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        return br.readLine();
    }

}
