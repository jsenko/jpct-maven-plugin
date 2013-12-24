package net.jsenko.jpct.configurator.model;

/**
 * Git configuration of the Jenkins job.
 * This is not usually set directly by the user,
 * but is computet from the local git repository.
 *
 * @author Jakub Senko
 */
public class GitModel
{
    /**
     * Repository URL
     */
    private String url;

    /**
     * Unique repository name in Jenkins. Not required.
     */
    private String name;

    /**
     * Determine the refs (branches) that will be retrieved (git fetch)
     * and how they are mapped to local branches.
     * Because this plugin works only with a single specific branch,
     * this is useful to prevent all refs from being fetched (default behavior).
     */
    private String refspec;

    /**
     * Specifies branches that contain the code to be built (git checkout).
     */
    private String branchspec;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRefspec() { return refspec; }
    public void setRefspec(String refspec) { this.refspec = refspec; }
    public String getBranchspec() { return branchspec; }
    public void setBranchspec(String branchspec) { this.branchspec = branchspec; }

    @Override
    public String toString()
    {
        return "GitModel {url = " + url + ", name = " + name +
                ", refspec = " + refspec + ", branchspec = " + branchspec + '}';
    }

    @Override
    public int hashCode()
    {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (refspec != null ? refspec.hashCode() : 0);
        result = 31 * result + (branchspec != null ? branchspec.hashCode() : 0);
        return result;
    }
}
