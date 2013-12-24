package net.jsenko.jpct.configurator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import net.jsenko.jpct.configurator.converter.BuildStepConverter;
import net.jsenko.jpct.configurator.converter.GitModelConverter;
import net.jsenko.jpct.configurator.converter.MavenJobModelConverter;
import net.jsenko.jpct.configurator.converter.ParameterModelConverter;
import net.jsenko.jpct.configurator.model.BuildStepModel;
import net.jsenko.jpct.configurator.model.JobModel;
import net.jsenko.jpct.configurator.model.ParameterModel;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generates Jenkins job configuration:
 * 1. provide job model from pom.xml
 * 2. use builder to add parameters from command line
 * 3. builder will combine these values together, preferring POM over command line options
 *    and modifying so it can be executed by this plugin (e.g. adds patch parameter)
 * 4. Finally, convert to XML and use the file to create the job This is a very flexible
 *    design, allows for addition of new job features, such as definition of shell build steps
 *    and adding support for other possible Jenkins plugins.
 * 
 * @author Jakub Senko <jsenko@redhat.com>
 */
public class JobConfigurator
{

    private final JobModel jobModel;

    private Log log;

    private JobConfigurator(JobModel jobModel, Log log)
    {
        this.jobModel = jobModel;
        this.log = log;
    }

    /**
     * Get builder so parameters from the command line can be added.
     * Cannot be null.
     */
    public static Builder getBuilder(JobModel jobModel, Log log) {
        Objects.requireNonNull(jobModel);
        Objects.requireNonNull(log);
        return new Builder(jobModel, log);
    }

    /**
     * Get the job model.
     */
    public JobModel getJobModel() {
        return jobModel;
    }

    /**
     * Use XStream to create xml file from model.
     */
    private void parse(Writer writer, JobModel jobModel)
    {
        XStream xstream = new XStream(new DomDriver());

        xstream.registerConverter(new MavenJobModelConverter());
        xstream.alias("maven2-moduleset", JobModel.class);

        xstream.registerConverter(new ParameterModelConverter());
        xstream.registerConverter(new GitModelConverter());
        xstream.registerConverter(new BuildStepConverter());
        xstream.toXML(jobModel, writer);
    }

    /**
     * Create Jenkins job config file.
     * 
     * @param configFile target file, must exist and be writeable
     * @return true on success
     */
    public boolean createJobConfig(File configFile) {
        try (FileWriter writer = new FileWriter(configFile)) {
            parse(writer, jobModel);
        } catch (IOException e) {
            log.debug("Error while creating job config file: " + e);
            return false;
        }
        return true;
    }

    public static class Builder
    {
        private Log log;

        private JobModel jobModel;

        public Builder(JobModel jobModel, Log log) {
            this.jobModel = jobModel;
            this.log = log;
        }

        /**
         * In this case name is overwritten.
         */
        public Builder setName(String name) {
            jobModel.setName(name);
            return this;
        }

        public Builder setDescription(String description) {
            if (jobModel.getDescription() == null)
                jobModel.setDescription(description);
            return this;
        }

        public Builder setGitUrl(String gitUrl) {
            if (jobModel.getGit().getUrl() == null)
                jobModel.getGit().setUrl(gitUrl);
            return this;
        }

        /**
         * Path to the POM file, relative to repo root. This is a necessary parameter.
         */
        public Builder setPomPath(String pomPath) {
            if (jobModel.getPomPath() == null)
                jobModel.setPomPath(pomPath);
            return this;
        }

        public JobConfigurator build()
        {
            // Add the necessary parameters:
            ParameterModel patchFile =
                    new ParameterModel("file", "patch", null,
                            "Patch file containing the changes.");

            ParameterModel commitId =
                    new ParameterModel("string", "commitID", "",
                            "Id of the commit on which the patch will be applied.");

            ParameterModel nonce =
                    new ParameterModel("string", "nonce", "",
                            "Dummy random string to check if we have correct build " +
                                    "in case of concurrent execution.");

            ParameterModel goals =
                    new ParameterModel("string", "goals", "", "Maven top-level goals. "
                            + "Must be a variable to enable job reuse.");

            List<ParameterModel> parameters = jobModel.getParameters();
            parameters.add(patchFile);
            parameters.add(commitId);
            parameters.add(nonce);
            parameters.add(goals);

            /*
             * branchspec will be replaced by the parameter value again to enable job reuse
             */
            jobModel.getGit().setBranchspec("$commitID");

            /*
             * fill in the pom path also for other maven build steps
             */
            for (BuildStepModel buildStep : jobModel.getBefore())
                if (buildStep.getGoals() != null && buildStep.getPom() == null)
                    buildStep.setPom(jobModel.getPomPath());
            for (BuildStepModel buildStep : jobModel.getAfter())
                if (buildStep.getGoals() != null && buildStep.getPom() == null)
                    buildStep.setPom(jobModel.getPomPath());

            /*
             * Create a shell buildstep to revert the previous patched changes in the workspace and patch again
             */
            BuildStepModel patch = new BuildStepModel();
            patch.setShell("git reset --hard\ngit apply ${patch}");
            List<BuildStepModel> before = new ArrayList<>();
            before.add(patch);
            before.addAll(jobModel.getBefore());// this build step must be always first
            jobModel.setBefore(before);

            jobModel.setGoals("$goals");

            return new JobConfigurator(jobModel, log);
        }
    }
}
