package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File STAGING_FILE = join(GITLET_DIR, "staging_area");
    public static final File HEAD_FILE = join(GITLET_DIR, "head");
    public static final File BRANCH_FILE = join(GITLET_DIR, "branch");

    private StagingArea stage;
    private String currentBranch;
    private Map<String, String> branches; // maps branch to latest commit in that branch


    //Constructor
    @SuppressWarnings("unchecked")
    public Repository() {
        if (GITLET_DIR.exists()) {
            currentBranch = readContentsAsString(HEAD_FILE);
            branches = (HashMap<String, String>) readObject(BRANCH_FILE, HashMap.class);
            stage = readObject(STAGING_FILE, StagingArea.class);

        }
        else {
            currentBranch = "master";
            branches = new HashMap<String, String>();
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

        File commitFile = join(COMMITS_DIR, initialCommit.getSha1Id());
        writeObject(commitFile, initialCommit);

        writeObject(STAGING_FILE, stage);

        branches.put(currentBranch, initialCommit.getSha1Id());

        writeContents(HEAD_FILE, currentBranch);
        writeObject(BRANCH_FILE, (Serializable) branches);
    }

    // add command
    @SuppressWarnings("unchecked")
    public void add(String fileName) {
        File fileToAdd = join(CWD, fileName);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        stage = readObject(STAGING_FILE, StagingArea.class);
        byte[] content = readContents(fileToAdd);
        String blobSha1ID = sha1((Object) content);

        if (BRANCH_FILE.exists() && HEAD_FILE.exists()) {
            branches = (HashMap<String, String>) readObject(BRANCH_FILE, HashMap.class);
            currentBranch = readContentsAsString(HEAD_FILE);

            Commit latest = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);
            String committedBlob = latest.getBlobs().get(fileName);
            String stagedBlob = stage.getAddStage().get(fileName);

            if (blobSha1ID.equals(committedBlob)) {
                stage.getAddStage().remove(fileName);
                stage.getRemoveStage().remove(fileName);
            } else if (blobSha1ID.equals(stagedBlob)) {
                // Already staged with same content
                return;
            } else {
                stage.getAddStage().put(fileName, blobSha1ID);

                // Write blob file
                File blobFile = join(BLOBS_DIR, blobSha1ID);
                writeContents(blobFile, content);
            }
            writeObject(STAGING_FILE, stage);
        }
    }

    // commit command
    @SuppressWarnings("unchecked")
    public void commit(String message) {
        // failure case -- if the staging area is empty or if the message is empty
        stage = readObject(STAGING_FILE, StagingArea.class);
        if (stage.getAddStage().isEmpty() && stage.getRemoveStage().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        // get the latest commit
        branches = (HashMap<String, String>) readObject(BRANCH_FILE, HashMap.class);
        currentBranch = readContentsAsString(HEAD_FILE);
        Commit latest = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);

        // make a copy
        Map<String, String> newBlobs = new HashMap<String, String>(latest.getBlobs());
        Commit newCommit = new Commit(message, latest.getSha1Id(), newBlobs);

        // use the staging area to remove/add tracked files
        for (String key: stage.getAddStage().keySet()) {
            newCommit.getBlobs().put(key, stage.getAddStage().get(key));
        }

        for (String fileName: stage.getRemoveStage()) {
            newCommit.getBlobs().remove(fileName);
        }

        // clear the staging area
        stage.clear();
        writeObject(STAGING_FILE, stage);

        // update instance variables
        branches.put(currentBranch, newCommit.getSha1Id());

        // add the commit to the commitsDirectory as a new commit file and write the object for persistence updating branches
        File commitFile = join(COMMITS_DIR, newCommit.getSha1Id());
        writeObject(commitFile, newCommit);
        writeObject(BRANCH_FILE, (Serializable) branches);
    }

    // log command
    @SuppressWarnings("unchecked")
    public void log() {
        // get the latest commit
        branches = (HashMap<String, String>) readObject(BRANCH_FILE, HashMap.class);
        currentBranch = readContentsAsString(HEAD_FILE);
        Commit current = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);
        while (current.getParent() != null) {
            //print current
            System.out.println("===");
            System.out.println("commit " + current.getSha1Id());
            System.out.println("Date: " + formatForLog(current.getTimeStamp()));
            System.out.println(current.getMessage());
            System.out.println();
            // set current to the parent
            current = readObject(join(COMMITS_DIR, current.getParent()), Commit.class);
        }

        // IMPLEMENT MERGE COMMITS LATER
    }

    private String formatForLog(String utcTimestamp) {
        try {
            // Format in which your commit timestamps were stored
            SimpleDateFormat storedFormat = new SimpleDateFormat("HH:mm:ss 'UTC', MM-dd--yyyy");
            storedFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Desired Git-style format (e.g., "Thu Nov 9 20:00:05 2017 -0800")
            SimpleDateFormat gitFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            gitFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles")); // PST/PDT

            Date parsed = storedFormat.parse(utcTimestamp);
            return gitFormat.format(parsed);
        } catch (Exception e) {
            return utcTimestamp; // Fallback if parsing fails
        }
    }

    // checkout command
    @SuppressWarnings("unchecked")
    public void checkout(String fileName) {
        // Failure Case
        branches = (HashMap<String, String>) Utils.readObject(BRANCH_FILE, HashMap.class);
        currentBranch = Utils.readContentsAsString(HEAD_FILE);
        Commit latest = Utils.readObject(Utils.join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);

        if (latest.getBlobs().get(fileName) == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File newFile = Utils.join(BLOBS_DIR, latest.getBlobs().get(fileName));
        // replace the file in the working directory with the new file
        byte[] content = Utils.readContents(newFile);
        Utils.writeContents(Utils.join(CWD, fileName), (Object) content);
    }

    @SuppressWarnings("unchecked")
    public void checkout(String commitID, String fileName) {
        // failure cases
        File commitFile = join(COMMITS_DIR, commitID);
        Commit commitToCheck = null;
        try {
            commitToCheck = readObject(commitFile, Commit.class);
        } catch (IllegalArgumentException e){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        String blobID = commitToCheck.getBlobs().get(fileName);
        if (blobID == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File blobFile = Utils.join(BLOBS_DIR, blobID);
        byte[] content = Utils.readContents(blobFile);
        Utils.writeContents(Utils.join(CWD, fileName), (Object) content);
    }
}
