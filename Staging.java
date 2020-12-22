package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

public class Staging implements Serializable {

    //<fileName, fileID>
    TreeMap<String, String> added;
    TreeMap<String, String> removed;

    public Staging() {
        added = new TreeMap<>();
        removed = new TreeMap<>();
    }

    public static String getFileID(File file, String filename) {
        byte[] fileContent = Utils.readContents(file);
        String fileID = Utils.sha1(fileContent);
        byte[] fileTitle = filename.getBytes();
        String shaTitle =  Utils.sha1(fileTitle);
        String toSha = fileID + shaTitle;
        String shaID = Utils.sha1(toSha);
        return shaID;
    }

    public void clear() {
        this.added = new TreeMap<>();
        this.removed = new TreeMap<>();
        File clearedStage = new File("./.gitlet/Stage");
        clearIfNotDirectory(clearedStage);
        Main.serialization("Stage/Stage Area", this);
        //Main.deserialization("Stage/stageArea");
    }

    public static void clearIfNotDirectory(File ifDir) {
        /*if (ifDir.isDirectory()) {
            clearIfNotDirectory();
        }*/
        for (File x : ifDir.listFiles()) {
            if (!x.isDirectory()) {
                x.delete();
            } else {
                clearIfNotDirectory(x);
            }
        }
    }
}
