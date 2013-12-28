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

import net.jsenko.jpct.configurator.JobConfigurator;
import net.jsenko.jpct.configurator.model.JobModel;
import net.jsenko.jpct.jenkins.client.Build;
import net.jsenko.jpct.jenkins.client.JenkinsClient;
import net.jsenko.jpct.jenkins.client.Job;
import net.jsenko.jpct.jenkins.client.impl.JenkinsClientFactory;
import net.jsenko.jpct.result.ConsoleOutputFileRP;
import net.jsenko.jpct.result.ProgressRP;
import net.jsenko.jpct.result.ResultProcessor;
import net.jsenko.jpct.result.TestSummaryRP;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main Mojo of the Jenkins pre-commit test Maven plugin.
 * The plugin will automatically create a job in Jenkins and transfers local changes there.
 * Subsequently, Maven top goals provided as a parameter are executed on the server.
 * 
 * @author Jakub Senko
 */
@Mojo(name = "run", aggregator = true)
public class RunMojo extends AbstractMojo
{

    @Parameter(property = "basedir", required = true)
    private File baseDir;

    /**
     * Place to store generated job configurations, test reports and other settings.
     */
    @Parameter(property = "dataDir", defaultValue = "${project.build.directory}/jpct",
            required = true)
    private File dataDir;

    /**
     * Maven goals and properties that should be executed on Jenkins.
     * Required for automatic maven job creation. Example:
     * -Dgoals="test -DallTests"
     */
    @Parameter(property = "goals", defaultValue = "test")
    private String goals;

    /**
     * Url of the Jenkins instance on which the jobs will be executed.
     * If no user and token are specified, no authentiation will be used.
     * This property is remembered for specific job name so it does not have to be set again.
     */
    @Parameter(property = "jenkinsUrl")
    private String jenkinsUrl;

    @Parameter(property = "jenkinsUser")
    private String jenkinsUser;

    @Parameter(property = "jenkinsToken")
    private String jenkinsToken;

    /**
     * The branch to be tested. Default: HEAD
     */
    @Parameter(property = "topicBranch", defaultValue = "HEAD", required = true)
    private String topicBranch;

    /**
     * Also include uncommitted changes staged for commit in addition to local commits.
     * Default: false
     */
    @Parameter(property = "includeStaged", defaultValue = "false", required = true)
    private Boolean includeStaged;

    /**
     * Name of the remote (as listed in "gitTools remote") which will be cloned on Jenkins.
     * It must contain a branch with a common ancestor with topic branch being tested.
     * This commit will the be checked out, and a patch with the changes
     * will be applied on it, effectively duplicating changes made locally.
     */
    @Parameter(property = "gitRemoteName", defaultValue = "origin", required = true)
    private String gitRemoteName;

    /**
     * Use a Jenkins job with the specified name even if we cannot verify
     * that the job has been created by this plugin.
     * This may result in a job execution failure (invalid parameters etc.)
     */
    @Parameter(property = "forceJobReuse", defaultValue = "false")
    private Boolean forceJobReuse;

    /**
     * This property contains job model(s) defined in POM.
     * If the job name fits with one of these, they are combined with the
     * properties from command line.
     */
    @Parameter
    private List<JobModel> jobs;

    /**
     * This is an unique job identifier. On Jenkins it is the name of the job.
     * By default it is name of the current branch.
     */
    @Parameter(property = "jobName")
    private String jobName;

    /**
     * Provide custom job description.
     */
    @Parameter(defaultValue = "")
    private String description;

    /**
     * Interval between checking the build status, in milliseconds.
     * It is more efficient to be larger for longer builds
     */
    @Parameter(defaultValue = "4000", required = true)
    private Integer buildCheckInterval;

    private GitTools gitTools;

    private JenkinsClient jenkinsClient;

    private JobConfigurator configurator;

    private Log log;

    private Config config;

    private Random random = new Random();

    private static void fail(String message) throws MojoExecutionException
    {
        throw new MojoExecutionException(message + "\nTry to run the plugin in debug mode (-X).");
    }

    public void execute() throws MojoExecutionException
    {
        log = getLog();

        gitTools = GitTools.lookup(baseDir, log);
        if (gitTools == null)
            fail("Could not find the git repository in '"
                    + baseDir + "' or in any of its parents.");

        if (jobName == null)
            jobName = generateJobName();

        config = Config.getConfigByJobName(dataDir, jobName, log);
        if(config == null)
            fail("Could not get job config.");

        jenkinsClient = setupJenkins(jenkinsUrl, jenkinsUser, jenkinsToken);

        log.info("Using job name: '" + jobName + "'.");
        Job job = jenkinsClient.getJobByName(jobName);

        String remoteUrl = gitTools.getRemoteUrl(gitRemoteName);
        if (remoteUrl == null)
            fail("Could not determine remote repository url. "
                    + "Remote '" + gitRemoteName + "' does not exist.");

        configurator = JobConfigurator.getBuilder(getJobModel(), log)
                .setPomPath(getRelativePomPath(gitTools))
                .setDescription(description)
                .setGitUrl(remoteUrl)
                .setName(jobName)
                .build();

        if (job == null) { // create the job in jenkins if it does not exist
            File jobConfigFile = new File(config.getJobDir(), "config.xml");
            ensureFileExists(jobConfigFile);

            configurator.createJobConfig(jobConfigFile);

            job = jenkinsClient.createJob(jobName, jobConfigFile);

            if (job == null)
                fail("Could not create Jenkins job '" + jobName + "'.");

            // save job model hash code
            config.put("jobModelHashCode", String.valueOf(configurator.getJobModel().hashCode()));
            config.save();
        } else {
            if (!canReuse(configurator.getJobModel())) {
                fail("The Jenkins job named '" + jobName
                        + "' already exists on Jenkins, but it cannot be reused, "
                        + "because we cannot verify that it was created by this plugin "
                        + "or its configuration has not changed.\n"
                        + "To try to reuse the job anyway, use '-DforceJobReuse'.\n"
                        + "Note that this does not guard against changes via other means" +
                        " (e.g. web interface).");
            } else {
                log.info("Reusing existing Jenkins job.");
            }
        }

        File patchFile = new File(config.getJobDir(), "patch");
        ensureFileExists(patchFile);
        String patchCommitId = createPatch(patchFile).getName();

        String nonce = generateNonce(10);

        log.info("Running the job.");

        boolean result = job.getRunBuilder()
                .setParameter("commitID", patchCommitId)
                .setParameter("nonce", nonce)
                .setParameter("goals", getGoals())
                .setParameter("patch", patchFile)
                .run();

        if (!result)
            fail("Build could not be started.");

        log.info("Waiting for start of the build.");

        /*
         * Get the build by nonce to ensure we have the correct one.
         * Wait in a loop until the build is created and later while
         * it is executed.
         */
        Build build = null;
        do { // TODO timeout
            System.out.print(".");
            try {
                Thread.sleep(buildCheckInterval);
            } catch (Exception e) {
                log.error(e);
            }
            for (Build b : job.getAllBuilds())
                if (nonce.equals(b.getStringParameterValue("nonce")))
                    build = b;
        } while (build == null);
        System.out.println();

        log.info("Build started.");

        final List<ResultProcessor> resultProcessors = new ArrayList<>();
        resultProcessors.add(new ProgressRP());
        resultProcessors.add(new TestSummaryRP());
        resultProcessors.add(new ConsoleOutputFileRP());

        for (ResultProcessor r : resultProcessors)
            r.start(build, config, log);

        do {
            try {
                Thread.sleep(buildCheckInterval);
            } catch (InterruptedException e) {
                log.error(e);
            }
            for (ResultProcessor r : resultProcessors) {
                r.run();
            }
        } while (build.isBuilding());

        for (ResultProcessor r : resultProcessors)
            r.finish();

        if (build.isSuccess()) {
            log.info("Build SUCCESS!");
            config.save();
        } else {
            fail("Build FAILURE (" + build.getResult() + ")");
        }
    }

    private String getGoals() throws MojoExecutionException
    {
        if (goals == null) {
            goals = config.get("goals");

            if (goals == null)
                fail("Provide Maven goals to execute using -Dgoals parameter.");
            else
                log.info("Running maven goals used previously: " + goals);
        } else {
            config.put("goals", goals);
        }
        return goals;
    }

    /**
     * Check if the job can be reused. This happens if job model has not changed (locally).
     */
    private boolean canReuse(JobModel jobModel) {
        int jobModelHashCode = jobModel.hashCode();
        String savedJobModelHashCode = config.get("jobModelHashCode");
        return forceJobReuse || (savedJobModelHashCode != null
                && jobModelHashCode == Integer.parseInt(savedJobModelHashCode));
    }

    /**
     * Attempt to create new jenkins client instance. If credentials are null, attempt to connect without authentication.
     * 
     * @return null on failure
     */
    private JenkinsClient setupJenkins(String url, String user, String token)
            throws MojoExecutionException
    {
        String savedUrl = config.get("jenkinsUrl");
        String savedUser = config.get("jenkinsUser");
        String savedToken = config.get("jenkinsToken");
        if (url == null)
            url = savedUrl;
        if (user == null)
            user = savedUser;
        if (token == null)
            token = savedToken;

        if (url == null)
            fail("Jenkins URL (jenkinsUrl) is null and there is no saved value" +
                    " from previous executions. This is a required parameter. " +
                    "URL must point to the root of the server.");

        // this is to avoid problems when connecting via https
        System.setProperty("jsse.enableSNIExtension", "false");

        JenkinsClient jc;

        if (user == null || token == null) {
            jc = JenkinsClientFactory.createClient(url, log); // TODO url as argument?
            log.debug("Setting up the jenkins client without authentication.");
        } else {
            jc = JenkinsClientFactory.createClient(url, user, token, log);
            log.debug("Setting up the jenkins client with authentication.");
        }
        if (jc == null) {
            fail("Failed to connect to Jenkins. "
                    + "Check if the server is accessible and URL (" + url + ") and credentials" +
                    " (user = '" + user + "', token = '" + token + "') are correct.");
        } else {
            log.debug("Saving Jenkins access config.");
            config.put("jenkinsUrl", url);
            config.put("jenkinsUser", user);
            config.put("jenkinsToken", token);
        }
        return jc;
    }

    /**
     * Generate string of length "length" by concatenating pseudo-randomly chosen characters. Currently, there are 58 characters
     * (base 58).
     */
    private String generateNonce(int length)
    {
        String chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            result.append(chars.charAt(random.nextInt(chars.length())));
        log.debug("generated nonce: " + result);
        return result.toString();
    }

    /**
     * Return string representing a path to pom.xml that is being currently used from the repository root directory. This is
     * used as a path to pom.xml in Jenkins workspace.
     * 
     * @return relative path or null on failure
     */
    private String getRelativePomPath(GitTools git)
    {
        log.debug("Computing relative pom.xml path.");
        try
        {
            String repositoryRootAbsolutePath = git.getGitFolder().getParentFile()
                    .getCanonicalPath();
            log.debug("Canonical path to repo root = \"" + repositoryRootAbsolutePath + "\".");
            String pomAbsolutePath = new File(baseDir, "pom.xml").getCanonicalPath();
            log.debug("Canonical path to pom.xml = \"" + pomAbsolutePath + "\".");

            if (pomAbsolutePath.startsWith(repositoryRootAbsolutePath))
                return pomAbsolutePath.substring(repositoryRootAbsolutePath.length() + 1);
            else
                return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Generate the name of the (jenkins) job. Currently it is based on the topic branch name. this is in accordance to the job
     * reuse requirement. The name remains same for same branch.
     */
    private String generateJobName() {
        String refName = gitTools.getRef(topicBranch).getLeaf().getName();
        // because ref names contains namespaces, just use the actual branch name
        String[] tokens = refName.split("/");
        return tokens[tokens.length - 1];
    }

    /**
     * Helper method for creating patch file. If the destination file and parent directories do not exist, attempts to create
     * them
     */
    private ObjectId createPatch(File patchFile) throws MojoExecutionException
    {
        Ref topicBranchRef = gitTools.getRef(topicBranch);

        if (topicBranchRef == null)
            fail("Topic branch referenced by '" + topicBranch + "' does not exist.");

        ObjectId base = gitTools.findBase(topicBranch, gitRemoteName); // TODO reuse "topicBranchRef"
        if (base == null)
            fail("Failed to compute base commit. "
                    + "Check that there exist at least one 'refs/remotes/" + gitRemoteName
                    + "/*' branch.");

        if(base.equals(topicBranchRef.getObjectId()))
            log.warn("The changes do not contain any commit. If -DincludeStaged is not used " +
                             "(and something is actually staged) the build may fail!");

        if (!gitTools.createPatch(patchFile, base, topicBranchRef.getObjectId(), includeStaged))
            fail("Failed to generate a patch.");

        return base;
    }

    private void ensureFileExists(File file) throws MojoExecutionException
    {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent == null || (!parent.exists() && !parent.mkdirs()))
                fail("Could not create directory for file: " + file);
            try {
                file.createNewFile();
            } catch (IOException e) {
                fail("Could not create a file. " + e);
            }
        }
    }

    /**
     * @return job model by name or an empty mode
     */
    private JobModel getJobModel() {
        if(jobs != null)
        for (JobModel jm : jobs)
            if (jobName.equals(jm.getName()))
                return jm;
        return new JobModel();
    }
}
