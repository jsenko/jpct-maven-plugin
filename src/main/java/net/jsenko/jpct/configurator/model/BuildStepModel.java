package net.jsenko.jpct.configurator.model;

/**
 * Jenkins job build step.
 * Currently there are two options:
 *  - execute shell command(s)
 *  - execute maven goal(s)
 * For now this class serves for both types, TODO improve this.
 *
 * @author Jakub Senko
 */
public class BuildStepModel
{
    /**
     * Shell command(s) to be executed.
     * Cannot be set together with {@link #goals}
     */
    private String shell;

    /**
     * Maven goal(s) to be executed.
     * Cannot be set together with {@link #shell}
     */
    private String goals;

    /**
     * Path to pom.xml from the workspace root.
     */
    private String pom;
    
    public String getShell() { return shell; }
    public void setShell(String shell) { this.shell = shell; }
    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }
    public String getPom() { return pom; }
    public void setPom(String pom) { this.pom = pom; }

    @Override
    public String toString()
    {
        return "BuildStepModel {shell = " + shell + ", goals = "
                + goals + ", pom = " + pom + '}';
    }


    @Override
    public int hashCode()
    {
        int result = shell != null ? shell.hashCode() : 0;
        result = 31 * result + (goals != null ? goals.hashCode() : 0);
        result = 31 * result + (pom != null ? pom.hashCode() : 0);
        return result;
    }
}
