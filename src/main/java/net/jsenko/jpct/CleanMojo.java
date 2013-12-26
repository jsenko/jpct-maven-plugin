/*
    Copyright 2013 Red Hat

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package net.jsenko.jpct;

import net.jsenko.jpct.jenkins.client.JenkinsClient;
import net.jsenko.jpct.jenkins.client.Job;
import net.jsenko.jpct.jenkins.client.impl.JenkinsClientFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Objects;

/**
 * Mojo that deletes all Jenkins jobs that have been created by this plugin. This is verified by the presence of local job file
 * in dataDir. If these files are deleted by 'mvn clean' for example before this mojo is executed, those jobs must be deleted
 * manually.
 * 
 * @author Jakub Senko
 */
@Mojo(name = "clean", aggregator = true)
public class CleanMojo extends AbstractMojo
{

    @Parameter(property = "basedir", required = true)
    private File basedir;

    /**
     * Place to store plugin data (e.g. generated config).
     */
    @Parameter(property = "dataDir", defaultValue = "${project.build.directory}/jpct",
            required = true)
    private File dataDir;

    private Log log;

    public void execute() throws MojoExecutionException
    {
        log = getLog();

        for (Config config : Config.getConfigForAllJobs(dataDir, log)) {
            String jobName = config.getJobDir().getName(); // TODO improve
            log.info("Deleting '" + jobName + "'.");
            String savedUrl = config.get("jenkinsUrl");
            String savedUser = config.get("jenkinsUser");
            String savedToken = config.get("jenkinsToken");
            JenkinsClient client = null;
            if (savedUrl != null) {
                if (savedUser == null || savedToken == null)
                    client = JenkinsClientFactory.createClient(savedUrl, log);
                else
                    client = JenkinsClientFactory
                            .createClient(savedUrl, savedUser, savedToken, log);
            }
            if (client == null) {
                log.warn("Could not retrieve Jenkins job for '" + jobName + "'.");
                continue;
            }
            Job job = client.getJobByName(jobName);
            if (job == null) {
                log.warn("Could not retrieve Jenkins job for '" + jobName + "'.");
                continue;
            }
            deleteRecursively(config.getJobDir());
            if (job.delete())
                log.info("Successfully deleted '" + jobName + "'.");
        }

    }

    /**
     * Delete single file or folder and all it's contents.
     */
    private void deleteRecursively(File file)
    {
        Objects.requireNonNull(file);
        if (file.isDirectory())
            for (File subFile : file.listFiles())
                deleteRecursively(subFile);
        file.delete();
    }
}
