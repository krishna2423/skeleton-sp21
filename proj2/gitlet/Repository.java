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

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
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

        } else {
            currentBranch = "master";
            branches = new HashMap<String, String>();
            stage = new StagingArea(new HashMap<String, String>(), new HashSet<String>());
        }
    }

    /* TODO: fill in the rest of this class. */
    // init command
    public void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
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
            return;
        }

        stage = readObject(STAGING_FILE, StagingArea.class);
        byte[] content = readContents(fileToAdd);
        String blobSha1ID = sha1((Object) content);

        File blobFile = Utils.join(BLOBS_DIR, blobSha1ID);
        if (!blobFile.exists()) {
            writeContents(blobFile, (Object) content);
        }

        branches = (HashMap<String, String>) readObject(BRANCH_FILE, HashMap.class);
        currentBranch = readContentsAsString(HEAD_FILE);
        Commit latest = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);
        String committedBlob = latest.getBlobs().get(fileName);
        String stagedBlob = stage.getAddStage().get(fileName);

        if (blobSha1ID.equals(committedBlob)) {
            stage.getAddStage().remove(fileName);
            stage.getRemoveStage().remove(fileName);
        } else if (blobSha1ID.equals(stagedBlob)) {
            return;
        } else {
            stage.getAddStage().put(fileName, blobSha1ID);
        }

        writeObject(STAGING_FILE, stage);
    }


    // commit command
    @SuppressWarnings("unchecked")
    public void commit(String message) {
        stage = readObject(STAGING_FILE, StagingArea.class);
        if (stage.getAddStage().isEmpty() && stage.getRemoveStage().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        branches = (HashMap<String, String>) readObject(BRANCH_FILE, HashMap.class);
        currentBranch = readContentsAsString(HEAD_FILE);
        Commit latest = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);

        // Finalize the new snapshot
        Map<String, String> newBlobs = new HashMap<String, String>(latest.getBlobs());

        for (String file : stage.getAddStage().keySet()) {
            newBlobs.put(file, stage.getAddStage().get(file));
        }
        for (String file : stage.getRemoveStage()) {
            newBlobs.remove(file);
        }

        // Create the commit with the finalized blob snapshot
        Commit newCommit = new Commit(message, latest.getSha1Id(), newBlobs);

        // Compute SHA and write to file
        File commitFile = join(COMMITS_DIR, newCommit.getSha1Id());
        writeObject(commitFile, newCommit);

        // Update branch pointer
        branches.put(currentBranch, newCommit.getSha1Id());

        // Clear and persist
        stage.clear();
        writeObject(STAGING_FILE, stage);
        writeObject(BRANCH_FILE, (Serializable) branches);
    }

    // log command
    @SuppressWarnings("unchecked")
    public void log() {
        // get the latest commit
        branches = (HashMap<String, String>) readObject(BRANCH_FILE, HashMap.class);
        currentBranch = readContentsAsString(HEAD_FILE);
        Commit current = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);

        while (true) {
            if (current.getParent2() != null) {
                System.out.println("===");
                System.out.println("commit " + current.getSha1Id());
                System.out.println("Merge: " + current.getParent().substring(0, 7) + " " + current.getParent2().substring(0, 7));
                System.out.println("Date: " + current.getTimeStamp());
                System.out.println(current.getMessage());
                System.out.println(); // required blank line
            } else {
                System.out.println("===");
                System.out.println("commit " + current.getSha1Id());
                System.out.println("Date: " + current.getTimeStamp());
                System.out.println(current.getMessage());
                System.out.println(); // required blank line
            }


            if (current.getParent() == null) {
                break;
            }
            current = readObject(join(COMMITS_DIR, current.getParent()), Commit.class);
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
            return;
        }
        File newFile = Utils.join(BLOBS_DIR, latest.getBlobs().get(fileName));
        // replace the file in the working directory with the new file
        byte[] content = Utils.readContents(newFile);
        Utils.writeContents(Utils.join(CWD, fileName), (Object) content);
    }

    public void checkout(String commitID, String fileName) {
        Commit commit = getCommit(commitID);
        if (commit == null) return;

        String blobID = commit.getBlobs().get(fileName);
        if (blobID == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        File blobFile = join(BLOBS_DIR, blobID);
        Utils.writeContents(join(CWD, fileName), (Object) Utils.readContents(blobFile));
    }

    //CHECKPOINT 1 COMPLETED ---

    @SuppressWarnings("unchecked")
    public void checkoutBranch(String branchName){
        branches = readObject(BRANCH_FILE, HashMap.class);
        currentBranch = readContentsAsString(HEAD_FILE);

        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (currentBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        Commit currentCommit = getCommit(branches.get(currentBranch));
        Commit targetCommit = getCommit(branches.get(branchName));
        if (hasUntrackedFileConflict(currentCommit, targetCommit)) return;

        if (targetCommit != null) {
            for (String fileName : targetCommit.getBlobs().keySet()) {
                String blobId = targetCommit.getBlobs().get(fileName);
                File blobFile = join(BLOBS_DIR, blobId);
                Utils.writeContents(join(CWD, fileName), (Object) Utils.readContents(blobFile));
            }

        }

        if (currentCommit != null) {
            for (String fileName : currentCommit.getBlobs().keySet()) {
                if (targetCommit != null && !targetCommit.getBlobs().containsKey(fileName)) {
                    Utils.restrictedDelete(fileName);
                }
            }
        }


        stage = readObject(STAGING_FILE, StagingArea.class);
        stage.clear();
        writeObject(STAGING_FILE, stage);

        currentBranch = branchName;
        writeContents(HEAD_FILE, currentBranch);

    }



    // rm command
    @SuppressWarnings("unchecked")
    public void rm(String fileName) {
        stage = readObject(STAGING_FILE, StagingArea.class);
        branches = readObject(BRANCH_FILE, HashMap.class);
        currentBranch = readContentsAsString(HEAD_FILE);
        Commit latest = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);

        boolean isStaged = stage.getAddStage().containsKey(fileName);
        boolean isTracked = latest.getBlobs().containsKey(fileName);

        if (!isStaged && !isTracked) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (isStaged) {
            stage.getAddStage().remove(fileName);
        }

        if (isTracked) {
            stage.getRemoveStage().add(fileName);
            File file = join(CWD, fileName);
            Utils.restrictedDelete(file);
        }

        writeObject(STAGING_FILE, stage); // Persist changes
    }

    // global-log command
    public void globalLog() {
        List<String> fileNames = Utils.plainFilenamesIn(COMMITS_DIR);
        assert fileNames != null;
        for (String fileName : fileNames) {
            Commit current = Utils.readObject(Utils.join(COMMITS_DIR, fileName), Commit.class);
            System.out.println("===");
            System.out.println("commit " + current.getSha1Id());
            System.out.println("Date: " + current.getTimeStamp());
            System.out.println(current.getMessage());
            System.out.println();
        }
    }

    // find command
    public void find(String message) {
        List<String> fileNames = Utils.plainFilenamesIn(COMMITS_DIR);
        List<String> filteredCommitIDS = new ArrayList<String>();
        if (fileNames == null) {
            return;
        }

        for (String fileName : fileNames) {
            Commit current = Utils.readObject(Utils.join(COMMITS_DIR, fileName), Commit.class);
            if (current.getMessage().equals(message)) {
                filteredCommitIDS.add(current.getSha1Id());
            }
        }
        if (filteredCommitIDS.isEmpty()) {
            System.out.println("Found no commit with that message.");
            return;
        } else {
            for (String commitID : filteredCommitIDS) {
                System.out.println(commitID);
            }
        }
    }

    //status command
    @SuppressWarnings("unchecked")
    public void status() {
        System.out.println("=== Branches ===");
        currentBranch = Utils.readContentsAsString(HEAD_FILE);
        branches = Utils.readObject(BRANCH_FILE, HashMap.class);
        System.out.println("*" + currentBranch);
        List<String> branchList = new ArrayList<String>(branches.keySet());
        Collections.sort(branchList);
        for (String branch : branchList) {
            if (branch.equals(currentBranch)) {
                continue;
            } else {
                System.out.println(branch);
            }
        }
        System.out.println(); //new line

        System.out.println("=== Staged Files ===");
        List<String> addList = new ArrayList<String>(stage.getAddStage().keySet());
        Collections.sort(addList);
        for (String file : addList) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> rmList = new ArrayList<String>(stage.getRemoveStage());
        Collections.sort(rmList);
        for (String file : rmList) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        //TO DO LATER
        System.out.println();

        System.out.println("=== Untracked Files ===");
        //TO DO LATER
        System.out.println();
    }


    // branch command
    @SuppressWarnings("unchecked")
    public void branch(String branchName) {
        branches = Utils.readObject(BRANCH_FILE, HashMap.class);
        Commit latest = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);

        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            branches.put(branchName, latest.getSha1Id());
            Utils.writeObject(BRANCH_FILE, (Serializable) branches);
        }
    }

    // rm-branch command
    @SuppressWarnings("unchecked")
    public void rmBranch(String branchName) {
        branches = Utils.readObject(BRANCH_FILE, HashMap.class);
        currentBranch = Utils.readContentsAsString(HEAD_FILE);
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } if (currentBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
        Utils.writeObject(BRANCH_FILE, (Serializable) branches);
    }

    //reset command
    @SuppressWarnings("unchecked")
    public void reset(String commitID) {
        Commit targetCommit = getCommit(commitID);
        if (targetCommit == null) return;

        branches = Utils.readObject(BRANCH_FILE, HashMap.class);
        currentBranch = Utils.readContentsAsString(HEAD_FILE);
        stage = Utils.readObject(STAGING_FILE, StagingArea.class);

        Commit currentCommit = readObject(join(COMMITS_DIR, branches.get(currentBranch)), Commit.class);
        if (hasUntrackedFileConflict(currentCommit, targetCommit)) return;
        for (String fileName: targetCommit.getBlobs().keySet()) {
            String blobId = targetCommit.getBlobs().get(fileName);
            File blobFile = join(BLOBS_DIR, blobId);
            byte[] content = Utils.readContents(blobFile);
            Utils.writeContents(join(CWD, fileName), (Object) content);
        }
        if (currentCommit != null) {
            for (String fileName : currentCommit.getBlobs().keySet()) {
                if (!targetCommit.getBlobs().containsKey(fileName)) {
                    Utils.restrictedDelete(fileName);
                }
            }
        }
        // change head pointer to the new commitID
        branches.put(currentBranch, targetCommit.getSha1Id());

        // clear staging area
        stage.clear();

        //Persist
        Utils.writeObject(BRANCH_FILE, (Serializable) branches);
        Utils.writeObject(STAGING_FILE, stage);
        Utils.writeContents(HEAD_FILE, currentBranch);
    }

    //merge command
    public void merge(String branchName) {

    }

    // HELPER METHODS

    /** Returns full commit ID if abbreviation is valid and unique, else null */
    public static String expandAbbreviatedCommitId(String shortId) {
        List<String> commitFileNames = Utils.plainFilenamesIn(COMMITS_DIR);
        if (commitFileNames == null) return null;

        List<String> matches = new ArrayList<String>();
        for (String id : commitFileNames) {
            if (id.startsWith(shortId)) {
                matches.add(id);
            }
        }
        return matches.size() == 1 ? matches.get(0) : null;
    }

    /** Returns Commit object from ID, handling abbreviated IDs and file errors */
    private Commit getCommit(String id) {
        if (id.length() < 40) {
            id = expandAbbreviatedCommitId(id);
            if (id == null) {
                System.out.println("No commit with that id exists.");
                return null;
            }
        }
        File commitFile = Utils.join(COMMITS_DIR, id);
        try {
            return readObject(commitFile, Commit.class);
        } catch (IllegalArgumentException e) {
            System.out.println("No commit with that id exists.");
            return null;
        }
    }

    /** Checks for untracked files that would be overwritten by the target commit */
    private boolean hasUntrackedFileConflict(Commit current, Commit target) {
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (cwdFiles == null) return false;
        for (String file : cwdFiles) {
            boolean untracked = !current.getBlobs().containsKey(file);
            boolean willBeOverwritten = target.getBlobs().containsKey(file);
            if (untracked && willBeOverwritten) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }
}

