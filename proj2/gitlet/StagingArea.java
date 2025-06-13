package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StagingArea implements Serializable {
    //instance variables
    public Map<String, String> addStage; //maps file name (e.g. hello.txt) to sha1ID of the file
    public Set<String> removeStage; //set that contains the file names of those that need to be removed

    public StagingArea(HashMap<String, String> a, HashSet<String> r) {
        this.addStage = a;
        this.removeStage = r;
    }
}
