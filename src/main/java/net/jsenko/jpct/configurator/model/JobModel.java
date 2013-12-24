package net.jsenko.jpct.configurator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO used to store job configuration that can be subsequently transformed
 * into Jenkins job configuration file.
 *
 * @author Jakub Senko
 */
public class JobModel
{

    /**
     * Unique job configuration identifier. Also used to derive actual Jenkins job name.
     */
    private String name;

    /**
     * Job description. Additional information may be added by the plugin automatically.
     */
    private String description;

    private List<ParameterModel> parameters = new ArrayList<>();

    private GitModel git = new GitModel();

    /**
     * Build steps before maven build
     */
    private List<BuildStepModel> before = new ArrayList<>();

    private String goals;

    private String pomPath;

    private String runAfterIf;

    /**
     * Build steps after maven build
     */
    private List<BuildStepModel> after = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<ParameterModel> getParameters() { return parameters; }
    public void setParameters(List<ParameterModel> parameters) { this.parameters = parameters; }
    public GitModel getGit() { return git; } public void setGit(GitModel git) { this.git = git; }
    public List<BuildStepModel> getBefore() { return before; }
    public void setBefore(List<BuildStepModel> before) { this.before = before; }
    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }
    public String getPomPath() { return pomPath; }
    public void setPomPath(String pomPath) { this.pomPath = pomPath; }
    public String getRunAfterIf() { return runAfterIf; }
    public void setRunAfterIf(String runAfterIf) { this.runAfterIf = runAfterIf; }
    public List<BuildStepModel> getAfter() { return after; }
    public void setAfter(List<BuildStepModel> after) { this.after = after; }

    @Override
    public String toString()
    {
        return "JobModel{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parameters=" + parameters +
                ", git=" + git +
                ", before=" + before +
                ", goals='" + goals + '\'' +
                ", pomPath='" + pomPath + '\'' +
                ", runAfterIf='" + runAfterIf + '\'' +
                ", after=" + after +
                '}';
    }


    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (git != null ? git.hashCode() : 0);
        result = 31 * result + (before != null ? before.hashCode() : 0);
        result = 31 * result + (goals != null ? goals.hashCode() : 0);
        result = 31 * result + (pomPath != null ? pomPath.hashCode() : 0);
        result = 31 * result + (runAfterIf != null ? runAfterIf.hashCode() : 0);
        result = 31 * result + (after != null ? after.hashCode() : 0);
        return result;
    }
}
