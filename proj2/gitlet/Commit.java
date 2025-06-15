package gitlet;

// TODO: any imports you need here

import javax.naming.directory.SearchResult;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String timeStamp;
    private String parent;
    private Map<String, String> blobs; // file name to blob sha1ID

    /* TODO: fill in the rest of this class. */
    //Constructor
    public Commit(String message, String parent, Map<String, String> blobs) {
        this.message = message;
        this.parent = parent;
        this.blobs = blobs;
        this.timeStamp = generateTimestamp();
    }

    //Getters
    public String getMessage() {
        return this.message;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public String getParent(){
        return this.parent;
    }
    public Map<String, String> getBlobs() {
        return this.blobs;
    }

    public String getSha1Id() {
        return Utils.sha1((Object) Utils.serialize(this));
    }

    private String generateTimestamp() {
        if (parent == null) {
            return "Wed Dec 31 16:00:00 1969 -0800";
        } else{
            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            formatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles")); // Spec uses PST
            return formatter.format(new Date());
        }
    }




}
