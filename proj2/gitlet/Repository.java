package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = Utils.join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = Utils.join(GITLET_DIR, "blobs");
    public static final File STAGING_FILE = Utils.join(GITLET_DIR, "staging_area");
    public static final File HEAD_FILE = Utils.join(GITLET_DIR, "head");
    public static final File BRANCH_FILE = Utils.join(GITLET_DIR, "branch");

    private StagingArea stage;
    private String currentBranch;
    private Map<String, Commit> commits; // maps commit ids to commit objects corresponding to the id
    private Map<String, String> branches;


    //Constructor
    @SuppressWarnings("unchecked")
    public Repository() {
        if (GITLET_DIR.exists()) {
            currentBranch = Utils.readContentsAsString(HEAD_FILE);
            branches = (HashMap<String, String>) Utils.readObject(BRANCH_FILE, HashMap.class);
            commits = (HashMap<String, Commit>) Utils.readObject(Utils.join(COMMITS_DIR, "commits"), HashMap.class);
            stage = Utils.readObject(STAGING_FILE, StagingArea.class);

        }
        else {
            currentBranch = "master";
            branches = new HashMap<String, String>();
            commits = new HashMap<String, Commit>();
            stage = new StagingArea(new HashMap<String, String>(), new HashSet<String>());
        }
    }

    /* TODO: fill in the rest of this class. */
    // init command
    public void init() {
        if(GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BLOBS_DIR.mkdirs();

        Commit initialCommit = new Commit("initial commit", null, new HashMap<String, String>());
        commits.put(initialCommit.getSha1Id(), initialCommit);

        File commitFile = Utils.join(COMMITS_DIR, initialCommit.getSha1Id());
        Utils.writeObject(commitFile, initialCommit);

        Utils.writeObject(STAGING_FILE, stage);

        branches.put(currentBranch, initialCommit.getSha1Id());

        Utils.writeContents(HEAD_FILE, currentBranch);
        Utils.writeObject(BRANCH_FILE, (Serializable) branches);


    }
}
