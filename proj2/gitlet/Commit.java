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

    public String getSha1Id() {
        return Utils.sha1(message, timeStamp, parent, blobs);
    }

    private String generateTimestamp(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss 'UTC', MM-dd--yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }




}
