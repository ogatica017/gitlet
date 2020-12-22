package gitlet;
import java.io.Serializable;
import java.io.File;
import java.util.Date;
import java.util.TreeMap;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//need to import file system

// This class creates objects of each commit

//make sure to serialize the commit class.

//create new directory.

public class Commit implements Serializable {

    Date commitDate;
    protected TreeMap<String, String> blobs; //keys: filenames values: Sha-1 ID of file
    String logMessage;
    Commit parent;
    String commitID;

    /** constructor for all commits except initial commit */
    public Commit(String logMessage, TreeMap<String, String> blobs, Commit parent) {
        this.commitDate = new Date();
        this.blobs = blobs;
        this.logMessage = logMessage;
        this.parent = parent;

        Collection<String> collectionOfFiles = blobs.values(); //Get collection of File ShaIDs
        List listOfFiles = collectionOfFiles.stream().
                collect(Collectors.toList()); //Make a List from the Collection
        String toHash = ""; //This String will be a sum
        // of all unique File Ids in this commit to hash into commit ID
        for (int i = 0; i < listOfFiles.size(); i++) {
            toHash = toHash + listOfFiles.get(i);
        }
        commitID = Utils.sha1(toHash);
        //Serialize the commit
        //Main.serialization("Commits/" + commitID, this);
    }

    public void placeBlob(String path) {
        File newFilePath = new File(path); //essentially filename instead of path
        String shaID = Staging.getFileID(newFilePath, path);
        Pointer currPointer = (Pointer) Main.deserialization("Curr Pointer");
        Branch currBranch = (Branch) Main.deserialization("Branches/" + currPointer.head);
        String commitIDBlob = currBranch.head;
        //get commit object to add to stageArea
        Commit curr = (Commit) Main.deserialization("Commits" + commitIDBlob);
        curr.blobs.put(path, shaID);
        Main.serialization("Commit/Blobs/" + path, newFilePath);
    }
//    public static List<String>
    public Date getDate() {
        return this.commitDate;
    }
}
