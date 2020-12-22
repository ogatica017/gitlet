package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    public String blobName;
    public String blobID;
    public byte[] content;
    public File path;


    public Blob(String blobName) {
        this.blobName = blobName;
        File bob = new File("./" + blobName);
        this.blobID = Staging.getFileID(bob, blobName);
        this.content = Utils.readContents(bob);
        this.path = bob; //mistake. call this.path.toPath()
        Main.serialization("Blobs/" + this.blobID, this);
    }


}
