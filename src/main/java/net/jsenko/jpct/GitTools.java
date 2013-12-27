package net.jsenko.jpct;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import static org.eclipse.jgit.api.ListBranchCommand.ListMode.ALL;

/**
 * Class for working with the project git repository.
 *
 * @author Jakub Senko
 */
public class GitTools
{
    private final Log log;

    private Git git;

    /**
     * Tool for interacting with project's git repository
     * 
     * @param gitDir .git directory
     * @throws IOException
     */
    public GitTools(File gitDir, Log log) throws IOException
    {
        this.log = log;
        git = Git.open(gitDir);
    }

    /**
     * Look up the first ancestor directory that contains git repository and use it to create GitTools instance. Return null on
     * failure.
     * 
     * @param dir directory in which to start the search
     * @return null when not found
     */
    public static GitTools lookup(File dir, Log log)
    {
        do
        {
            log.debug("Searching for .git in " + dir + '.');
            File gitDir = new File(dir, ".git");
            if (gitDir.exists())
            {
                log.debug("Found " + gitDir + '.');
                try {
                    return new GitTools(gitDir, log);
                } catch (IOException e) {
                    return null;
                }
            }
            dir = dir.getParentFile();
        } while (dir != null);
        return null;
    }

    public File getGitFolder()
    {
        return git.getRepository().getDirectory();
    }

    /**
     * Parse ref string (e.g. "HEAD") and return an abstraction of sha-1 id it points to.
     */
    public Ref getRef(String ref)
    {
        try {
            return git.getRepository().getRef(ref);
        } catch (IOException e) {
            log.error(e.toString());
            return null;
        }
    }

    /**
     * Get url of a remote repository or null on failure
     */
    public String getRemoteUrl(String remoteName)
    {
        Config storedConfig = git.getRepository().getConfig();
        return storedConfig.getString("remote", remoteName, "url");
    }

    /**
     * Used for creating a patch
     */
    private AbstractTreeIterator getTreeIterator(ObjectId objectId) throws IOException
    {
        final CanonicalTreeParser p = new CanonicalTreeParser();
        final ObjectReader or = git.getRepository().newObjectReader();

        p.reset(or, new RevWalk(git.getRepository()).parseTree(objectId));
        return p;
    }

    private Set<Ref> getAllBranches() {
        try {
            return new HashSet<>(git.branchList().setListMode(ALL).call());
        } catch (GitAPIException e) {
            log.debug("Error: Could not list branches.", e);
            return null;
        }
    }

    /**
     * Creates a patch file that represents a diff between two commits
     * 
     * @param out file into which the patch data is written, if it does not exist causes error
     * @param from starting commit
     * @param to target commit (result of patching the starting commit)
     * @param includeStaged include changes staged for commit in the patch
     * @return false when an error occurs
     */
    public boolean createPatch(File out, ObjectId from, ObjectId to, boolean includeStaged)
    {
        // TODO: remove file and use output stream as parameter
        try (OutputStream outputStream = new FileOutputStream(out, false))
        {
            git.diff().setOutputStream(outputStream)
                    .setCached(includeStaged)
                    .setOldTree(getTreeIterator(from))
                    .setNewTree(getTreeIterator(to))
                    .call();
            return true;
        } catch (IOException|GitAPIException e) {
            log.debug("Error when creating a patch from " + from + " to " + to
                    + ", include staged = "
                    + includeStaged + ": " + e);
            return false;
        }
    }

    /**
     * Given a start commit id and a list of branches, find the latest of all common ancestors between the start and the branch
     * tips latest = closest to commit, based on the commit time (assuming the value is correct, if not another of the ancestors
     * may be returned)
     * 
     * @return null on failure
     */
    private ObjectId findBase(ObjectId start, Set<ObjectId> others)
    {
        /*
         * commits in the start branch may load in multiple chunks in future because the branches should be relatively short
         */
        Set<RevCommit> workingCommits = new HashSet<>();
        Set<RevCommit> baseCandidates = new HashSet<>();

        RevWalk walk = new RevWalk(git.getRepository());

        try {
            walk.markStart(walk.parseCommit(start));
        } catch (IOException e) {
            log.error(e.toString());
        }
        for (RevCommit commit : walk) {
            workingCommits.add(commit);
        }
        walk.dispose();
        log.debug(workingCommits.toString());

        for (ObjectId other : others) {
            try {
                walk.markStart(walk.parseCommit(other));
            } catch (IOException e) {
                log.debug(e.toString());
            }
            for (RevCommit candidate : walk) {
                if (workingCommits.contains(candidate)) {
                    baseCandidates.add(candidate);
                    log.debug("Found base candidate "
                            + candidate.getId() + " from " + other);
                    break;
                }
            }
            walk.dispose();
        }

        // we have candidates, find the latest so the path contains the least amount of data
        RevCommit result = null;
        for (RevCommit candidate : baseCandidates) {
            if (result == null || (candidate.getCommitTime() > result.getCommitTime()))
                result = candidate;
        }

        log.debug("baseCandidates = " + baseCandidates);

        return result;
    }

    /**
     * Wrapper around the other method. Takes specific parameters (from user) for convenience.
     * 
     * @return null on failure
     */
    public ObjectId findBase(String startBranchRef, String remoteName)
    {
        Ref start = getRef(startBranchRef);
        Set<ObjectId> branches = new HashSet<>();
        for (Ref branch : getAllBranches()) {
            if (branch.getName().startsWith("refs/remotes/" + remoteName + "/")
                    && !branch.isSymbolic())
                branches.add(branch.getObjectId());
        }
        if (branches.size() == 0)
            log.debug("Warning: No remote branches found for " + remoteName);
        return findBase(start.getObjectId(), branches);
    }
}
