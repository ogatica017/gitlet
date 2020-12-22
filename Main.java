package gitlet;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @Sabrina An
   @Vishal Ambavaram
   @Omar Gatica
   @Spencer Song
*/

public class Main {
    private static ArrayList<Commit> commitArea;
    //private static String userDirectory;

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        //Hold list of available commands for Gitlet
        ArrayList<String> commands = new ArrayList<>();
        commands.add("init");
        commands.add("add");
        commands.add("commit");
        commands.add("rm");
        commands.add("log");
        commands.add("global-log");
        commands.add("find");
        commands.add("status");
        commands.add("checkout");
        commands.add("branch");
        commands.add("rm-branch");
        commands.add("reset");
        commands.add("merge");

        //Create gitlet file
        //String path = System.getProperty("user.dir");
        //File gitlet = new File(path,".gitlet");
        File gitlet = new File("./.gitlet");

        //Command Edge Cases and Basic Functionality

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (!commands.contains(args[0])) {
            System.out.println("No command with that name exists.");
            System.exit(0);
        } else if (args[0].equals("init") && args.length == 1) {
            init();
        } else if (!(gitlet.exists()) || (gitlet.exists() && !gitlet.isDirectory())) {
            if (!commands.contains(args[0])) {
                System.out.println("Not in an initialized gitlet directory");
            }
        } else {
            if (args[0].equals("add") && args.length == 2) {
                add(args[1]);
            } else if (args[0].equals("commit") && args.length == 2) {
                commit(args[1]);
            } else if (args[0].equals("rm") && args.length == 2) {
                rm(args[1]);
            } else if (args[0].equals("log") && args.length == 1) {
                log();
            } else if (args[0].equals("global-log") && args.length == 1) {
                globalLog();
            } else if (args[0].equals("find") && args.length == 2) {
                find(args[1]);
            } else if (args[0].equals("status") && args.length == 1) {
                status();
            } else if (args[0].equals("checkout")) { //FINISH LATER
                if (args.length == 2) {
                    checkoutBranch(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {
                    checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    checkoutCommit(args[1], args[3]);
                } else {
                    System.out.println("Incorrect Operands.");
                }
            } else if (args[0].equals("branch") && args.length == 2) {
                branch(args[1]);
            } else if (args[0].equals("rm-branch") && args.length == 2) {
                rmBranch(args[1]);
            } else if (args[0].equals("reset") && args.length == 2) {
                resetStart(args[1]);
            } else if (args[0].equals("merge") && args.length == 2) {
                merge(args[1]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        }
    }
    /** initialize a gitlet folder to store serialized files */
    private static void init() {
        // userDirectory = System.getProperty("user.dir");
        boolean initial = new File("./.gitlet").mkdir();
        if (initial) {
            new File("./.gitlet/Stage").mkdir();
            new File("./.gitlet/Branches").mkdir();
            new File("./.gitlet/Commits").mkdir();
            new File("./.gitlet/Blobs").mkdir();
            new File("./.gitlet/Tracked").mkdir();


            Staging stageArea = new Staging();

            Commit first = new Commit("initial commit",
                    new TreeMap<>(), null); // makes an initial commit.
            Pointer currentBranch = new Pointer("master");
            serialization("Curr Pointer", currentBranch);
            Branch masterBranch = new Branch("master", first.commitID);
            currentBranch = (Pointer) deserialization("Curr Pointer"); //deserialize current branch
//            currentBranch.head = "master";
            serialization("Curr Pointer", currentBranch); //Serialize current branch
//            commitArea.add(first);
//            head = first;
            serialization("Stage/Stage Area", stageArea);
            serialization("Commits/" + first.commitID, first);
        } else {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
        }
    }

    private static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }
        //get parent's snapshot of files
        Pointer currBranchPointer = (Pointer) deserialization("Curr Pointer");
        Branch currBranch = (Branch) deserialization("Branches/" + currBranchPointer.head);
        String parentCommitName = currBranch.head;
        Commit parentCommit = (Commit) deserialization("Commits/" + parentCommitName);
        TreeMap<String, String> snapshot = new TreeMap<>();
        for (String key : parentCommit.blobs.keySet()) {
            String value = parentCommit.blobs.get(key);
            snapshot.put(key, value);
        }
        // deserialize the stagingArea
        Staging stageArea = (Staging) deserialization("Stage/Stage Area");
        //make sure files have been staged
        if (stageArea.added.isEmpty() && stageArea.removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else {
            //update the parent snapshots with those in the "added" map
            for (String key : stageArea.added.keySet()) {
                String value = stageArea.added.get(key);
                if (!(snapshot.containsValue(value))) {
                    snapshot.put(key, value);
                }
            }

            //update the parent snapshots by removing those in the "removed" map
            for (String file : stageArea.removed.keySet()) {
                snapshot.remove(file);
            }
            //create a new commit. serializes in commit class
            Commit newCommit = new Commit(message, snapshot, parentCommit);
            /*for (String key : newCommit.blobs.keySet()) { // puts each file into tracked dir.
                Blob tracked = new Blob(key);
                serialization("Tracked/" + tracked.blobID, tracked);
            }*/
            //clears the stage
            serialization("Commits/" + newCommit.commitID, newCommit);
//            serialization("Commits/Blobs" + newCommit.commitID, newCommit.blobs);
            stageArea.clear();
            //sets the current branch's head pointer to the new commit
            currBranch.head = newCommit.commitID;
            serialization("Branches/" + currBranch.name, currBranch);
            currBranchPointer.moveHead(currBranch.name);
            serialization("Curr Pointer", currBranchPointer);
            serialization("Stage/Stage Area", stageArea);
        }
    }

    /** FIX HEAD */
    private static void add(String fileName) { //Sabrina modified add.
        //need to serialize the file and get shaID;
//        File file = new File("/fileName);

        File toAdd = new File(fileName);
        if (!toAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob toAddBlob = new Blob(fileName);
        Staging stageArea = (Staging) deserialization("Stage/Stage Area");
        Pointer currentPointer = (Pointer) deserialization("Curr Pointer"); //OG
        Branch currentBranch = (Branch) deserialization("Branches/" + currentPointer.head);
        String currCommitID = currentBranch.head;
        /*deserializte stageArea hashMaps*/
        if (stageArea.removed.containsValue(toAddBlob.blobID)) { // changed from toAddBlob.blobID
            stageArea.removed.remove(fileName);
        } else {
            //generate sha-1 ID`
//            String fileID = Staging.getFileID(file, fileName); //OG
            serialization("Blobs/" + toAddBlob.blobID, toAddBlob);
            //get commit object to add to stageArea
            Commit currCommit = (Commit) deserialization("Commits/" + currCommitID);
            //check if current working version of file is identical to the version in current commit
            if (currCommit.blobs.containsValue(toAddBlob.blobID)) {
                return;
            } else if (!stageArea.added.containsValue(toAddBlob.blobID)) {
                //unmarked files marked to be removed
//                System.out.println(stageArea.removed);
//                if (stageArea.removed.containsValue(toAddBlob.blobID)) {
//                    stageArea.removed.remove(fileName);
//                }
                stageArea.added.put(toAddBlob.blobName, toAddBlob.blobID);
            }

            //serialize the modified file. Saved in .gitlet under its unique shaID
            //needs to be .file/ + shaID
        }
        serialization("Stage/Stage Area", stageArea);
    }

    /** FIX HEAD */
    private static void rm(String fileName) {
        //find current commit
        Pointer currPointer = (Pointer) deserialization("Curr Pointer");
        Branch currBranch = (Branch) deserialization("Branches/" + currPointer.head);
        String commitID = currBranch.head;
        Commit curr = (Commit) deserialization("Commits/" + commitID);
        Staging stageArea = (Staging) deserialization("Stage/Stage Area");
        if (curr.blobs.containsKey(fileName)) { //Case 1
            stageArea.removed.put(fileName, curr.blobs.get(fileName));
            if (stageArea.added.containsKey(fileName)) { // unstage if staged
                stageArea.added.remove(fileName);
            }
            File toDelete = new File(fileName);
            toDelete.delete();
        } else if (stageArea.added.containsKey(fileName)) {
            stageArea.added.remove(fileName);
        } else {
            System.out.println("No reason to remove the file.");
        }
        serialization("Stage/Stage Area", stageArea);
        serialization("Curr Pointer", currPointer);
    }

    /** FIX HEAD */
    private static void log() {
        Pointer currPointer = (Pointer) deserialization("Curr Pointer");
        Branch currBranch = (Branch) deserialization("Branches/" + currPointer.head);
        String commitID = currBranch.head;
        Commit curr = (Commit) deserialization("Commits/" + commitID);
        boolean currParentnono = true;

        while (currParentnono) {
            System.out.println("===");
            System.out.println("Commit " + curr.commitID);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(dateFormat.format(curr.getDate()));
            System.out.println(curr.logMessage);
            //System.out.println();
            System.out.println();
            if (curr.parent != null) {
                Commit parentCurr = (Commit) deserialization("Commits/" + curr.parent.commitID);
                curr = parentCurr;
            } else {
                currParentnono = false;
            }
        }
    }

    private static void globalLog() {
        File allCommits = new File("./.gitlet/Commits");
        for (String a : allCommits.list()) {
            Commit thisCommit = (Commit) deserialization("Commits/" + a);
            System.out.println("===");
            System.out.println("Commit " + thisCommit.commitID);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(dateFormat.format(thisCommit.getDate()));
            System.out.println(thisCommit.logMessage);
            System.out.println();
        }
    }

    private static void find(String message) {
        File allCommits = new File(".gitlet/Commits");
        String[] allCommitsId = allCommits.list(); //gives you ids of all commits
        boolean found = false;
        for (String commitID : allCommitsId) {
            Commit currCommit = (Commit) deserialization("Commits/" + commitID);
            if (currCommit.logMessage.equals(message)) {
                found = true;
                System.out.println(currCommit.commitID);
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message");
        }
    }

    private static void status() {
        Pointer currPointer = (Pointer) deserialization("Curr Pointer");
        Staging statusStageArea = (Staging) deserialization("Stage/Stage Area");
        System.out.println("=== Branches ===");
        for (String branch : currPointer.branchPointerList) {
            if (!currPointer.head.equals(branch)) {
                System.out.println(branch);
            } else {
                System.out.println("*" + branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String name : statusStageArea.added.keySet()) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String name : statusStageArea.removed.keySet()) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Takes the version of the file as it exists in the head commit
     * puts it in the working directory overwriting the version of the file
     * that is already there if there is one */
    private static void checkoutFile(String fileName) {
        Blob wrkDirFile = new Blob(fileName);
        wrkDirFile.path = new File("./" + fileName);
        Pointer currPointer = (Pointer) deserialization("Curr Pointer");
        Branch currBranch = (Branch) deserialization("Branches/" + currPointer.head);
        String commitID = currBranch.head;
        Commit curr = (Commit) deserialization("Commits/" + commitID);
        if (!curr.blobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            Blob currFile = (Blob) deserialization("Blobs/" + curr.blobs.get(fileName));
            try {
                Files.deleteIfExists(wrkDirFile.path.toPath());
                Utils.writeContents(wrkDirFile.path, currFile.content);
            } catch (IOException e) {
                System.out.println("Checkout failure.");
            }
        }
    }

    /** Takes the version of the file as it exists in the commit with the given ID
     * puts it in the working directory overwriting the version of the file
     * that is already there if there is one */
    private static void checkoutCommit(String commitID, String fileName) {
        File wrkDirFile = new File("./" + fileName);
        String currCommitID = autoID(commitID);
        File commitFile = new File("./.gitlet/Commits/" + currCommitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        Commit curr = (Commit) deserialization("Commits/" + currCommitID);
        if (!curr.blobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            Blob currFile = (Blob) deserialization("Blobs/" + curr.blobs.get(fileName));
            try {
                Files.deleteIfExists(wrkDirFile.toPath());
                Utils.writeContents(wrkDirFile, currFile.content);
            } catch (IOException e) {
                System.out.println("Checkout failure.");
            }
        }
    }

    /** find head of branch
     * take all files in this commit
     * puts all these files in working directory (overwrite old versions if they exist)
     * given branch is now the head
     * files tracked in current branch but not in checked-out branch deleted
     * clear staging area */

    private static void checkoutBranch(String branch) { // Spencer and me.
        Pointer currPointer = (Pointer) deserialization("Curr Pointer");
        if (!currPointer.branchPointerList.contains(branch)) {
            System.out.println("No such branch exists");
            System.exit(0);
        } else if (branch.equals(currPointer.head)) {
            System.out.println("No need to checkout the current branch");
            System.exit(0);
        }

        Branch givenBranch = (Branch) deserialization("Branches/" + branch);
        Commit currCommit = (Commit) deserialization("Commits/" + givenBranch.head);
        reset(currCommit.commitID, givenBranch.name, true);
        currPointer.head = branch;
        serialization("Curr Pointer", currPointer);
    }

    private static void branch(String name) { // I think this is fine. This is all Spencer and me.
        Pointer branchPointer = (Pointer) deserialization("Curr Pointer");
        ArrayList branchList = branchPointer.getBranchList(); //got arraylist of branchPointers
//        Pointer currPointer = (Pointer) deserialization("Curr Pointer");
        Branch currBranch = (Branch) deserialization("Branches/"
                + branchPointer.head);  //get SHA ID for branch
        String currCommitID = currBranch.head;
        Branch createNewBranch = null;
        if (branchList.contains(name)) {
            System.out.println("A branch with that name already exists.");
        } else {
            createNewBranch = new Branch(name, currCommitID);
            branchPointer.addBranch(createNewBranch.name);
        }
    }

    private static void rmBranch(String name) { // might need to fix this.
        Pointer branchPointer = (Pointer) deserialization("Curr Pointer");
        ArrayList branchList = branchPointer.getBranchList(); //got arraylist of branchPointers
        Branch newBranch = (Branch) Main.deserialization("Branches/" + name); //maybe head

        if (newBranch == null || newBranch.head == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (name.equals(branchPointer.head)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            newBranch.moveHead(null);
            serialization("Branches/" + name, newBranch);
            //close branch
            // don't need : File newBranch = new File("./.gitlet/Branches/" + name);
            branchPointer.removeBranch(name);
            serialization("Curr Pointer", branchPointer);
        }
    }

    private static void resetStart(String commitID) {
        Pointer branchPointer = (Pointer) deserialization("Curr Pointer");
        reset(commitID, branchPointer.head, false);
    }
    private static void reset(String commitID, String branchName, Boolean fromCheckout) {
        String givenCommitID = autoID(commitID);
        if (givenCommitID == null) {
            System.out.println("No commit with that id exists");
            return;
        }
        Commit givenCommit = (Commit) deserialization("Commits/" + givenCommitID);
        Pointer currPointer = (Pointer) deserialization("Curr Pointer");
        Branch currBranch = (Branch) deserialization("Branches/" + currPointer.head);
        String currCommitID = currBranch.head;
        Commit currCommit = (Commit) deserialization("Commits/" + currCommitID);

        List<String> workDirFiles = Utils.plainFilenamesIn(".");
        for (String fileName : workDirFiles) {
            File file = new File("./" + fileName);
            String fileID = Staging.getFileID(file, fileName);
            if (!currCommit.blobs.containsValue(fileID)
                    && givenCommit.blobs.containsKey(fileName)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it or add it first.");
                return;
            }
        }
        Staging stagingArea = (Staging) deserialization("Stage/Stage Area");

        for (String fileName : givenCommit.blobs.keySet()) {
            String fileID = givenCommit.blobs.get(fileName);
            Blob currFile = (Blob) deserialization("Blobs/" + fileID);
            File wrkDirFile = new File("./" + fileName);
            try {
                Files.deleteIfExists(wrkDirFile.toPath());
                Utils.writeContents(wrkDirFile, currFile.content);
            } catch (IOException e) {
                System.out.println("Checkout failure.");
            }
        }
        Set<String> trackedCommitsNames = currCommit.blobs.keySet();
        Set<String> givenCommitsNames = givenCommit.blobs.keySet();
        for (String trackedFileName : trackedCommitsNames) {
            /**
             * if trackedFileName is not in commitID blob nanmes then remove
             * commitID.contains(trackedFileName)
             */
            if (!givenCommitsNames.contains(trackedFileName)) {
                // remove the file from working directory
                Blob wrkDirFile = new Blob(trackedFileName);
                wrkDirFile.path = new File("./" + trackedFileName);
                try {
                    Files.deleteIfExists(wrkDirFile.path.toPath());
                } catch (IOException excp) {
                    System.out.println("reset failure");
                    return;
                }
            }
        }
        if (!fromCheckout) {
            currBranch.moveHead(givenCommitID);
            //currPointer.head = currBranch.name;
            currPointer.head = branchName;
            serialization("Branches/" + currBranch.name, currBranch);
            serialization("Curr Pointer", currPointer);
        }
        stagingArea.clear();
    }

    private static void merge(String givenBranchName) {
        Pointer pointerTocurrent = (Pointer) deserialization("Curr Pointer");
        Branch currentBranch = (Branch) deserialization("Branches/" + pointerTocurrent.head);
        String currCommitID = currentBranch.head;
        Commit currCommit = (Commit) deserialization("Commits/" + currCommitID); //curr
        Branch givenBranch = null;
        try {
            givenBranch = (Branch) deserialization("Branches/" + givenBranchName);
            String branchName = givenBranch.name;
        } catch (NullPointerException x) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String givenCommitID = givenBranch.head; //String givenCommitID = givenBranch.head;
        Commit givenCommit = (Commit) deserialization("Commits/" + givenCommitID); //given
        Staging stageArea = (Staging) deserialization("Stage/Stage Area");
        if (!stageArea.added.isEmpty() || !stageArea.removed.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        List<String> workDirFiles = Utils.plainFilenamesIn(".");
        for (String fileName : workDirFiles) {
            File file = new File("./" + fileName);
            String fileID = Staging.getFileID(file, fileName);
            if (!currCommit.blobs.containsValue(fileID)
                    && givenCommit.blobs.containsKey(fileName)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it or add it first.");
                return;
            }
        }
        if (givenCommitID.equals(currCommitID)) {
            System.out.println("Cannot merge a branch with itself.");
        }
        Commit sPtComm = splitPointFinder(currCommit, givenCommit);
        if (sPtComm.commitID.equals(givenCommitID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (sPtComm.commitID.equals(currCommitID)) { //Failure Case:
            System.out.println("Current branch fast-forwarded.");
            reset(currCommitID, currentBranch.head, false);
            System.exit(0);
        }
        ArrayList<String> mergeConflicts = new ArrayList<>();
        mergeHelper1(currCommit, sPtComm, givenCommit, givenCommitID, mergeConflicts);
        byte[] headMessageU = "<<<<<<< HEAD\n".getBytes();
        byte[] dashU = "=======\n".getBytes(); // CASE 2,3,6 don't make any changes to file.
        byte[] arrowsU = ">>>>>>>\n".getBytes();
        if (mergeConflicts.isEmpty()) {
            commit("Merged " + pointerTocurrent.head + " with " + givenBranch.name + ".");
        } else {
            System.out.println("Encountered a merge conflict.");
            mergeHelper2(mergeConflicts, currCommit, givenCommit);
        }
    }

    public static void mergeHelper1(Commit currCom, Commit sPtComm,
                                    Commit givenCom, String givenID, ArrayList<String> conflicts) {
        for (String key : currCom.blobs.keySet()) { // in curr --CASE 5--
            Blob keyFile = (Blob) deserialization("Blobs/" + currCom.blobs.get(key));
            if (sPtComm.blobs.containsKey(keyFile.blobName)) { // in split
                if (currCom.blobs.get(keyFile.blobName).equals
                        (sPtComm.blobs.get(keyFile.blobName))) { // unmodified in curr
                    if (!givenCom.blobs.containsKey(keyFile.blobName)) { //NOT in given
                        rm(keyFile.blobName); //Case 5: Remove file
                    }
                }
            }
        }
        for (String key : currCom.blobs.keySet()) {
            Blob keyFile = (Blob) deserialization("Blobs/" + currCom.blobs.get(key));
            if (sPtComm.blobs.containsKey(keyFile.blobName)) { //in split
                if (!currCom.blobs.get(keyFile.blobName)
                        .equals(sPtComm.blobs.get(keyFile.blobName))) {
                    if (!givenCom.blobs.containsKey(keyFile.blobName)) { // file is absent in given
                        conflicts.add(keyFile.blobName);
                    }
                }
            }
        }
        for (String key : currCom.blobs.keySet()) { // --MERGE CONFLICT--
            Blob keyFile = (Blob) deserialization("Blobs/" + currCom.blobs.get(key));
            if (sPtComm.blobs.containsKey(keyFile.blobName)) {
                if (!currCom.blobs.get(keyFile.blobName).equals(sPtComm.
                        blobs.get(keyFile.blobName))) {
                    if (!(givenCom.blobs.get(keyFile.blobName) == null)) {
                        if (!(givenCom.blobs.get(keyFile.blobName).
                                equals(sPtComm.blobs.get(keyFile.blobName)))
                                && !(givenCom.blobs.get(keyFile.blobName).
                                equals(currCom.blobs.get(keyFile.blobName)))) {
                            conflicts.add(keyFile.blobName);
                        }
                    }
                }
            }
        }
        for (String key : givenCom.blobs.keySet()) { //in given --MERGE CONFLICT--
            Blob keyFile = (Blob) deserialization("Blobs/" + givenCom.blobs.get(key));
            if (sPtComm.blobs.containsKey(keyFile.blobName)) { // in split
                if (!givenCom.blobs.get(keyFile.blobName).equals
                        (sPtComm.blobs.get(keyFile.blobName))) { //given file is not modified.
                    if (!currCom.blobs.containsKey(keyFile.blobName)) {
                        conflicts.add(keyFile.blobName);
                    }
                }
            }
        }
        for (String key : givenCom.blobs.keySet()) { // in given --CASE 1--
            Blob keyFile = (Blob) deserialization("Blobs/" + givenCom.blobs.get(key));
            if (sPtComm.blobs.containsKey(keyFile.blobName)) { //in split
                if (!givenCom.blobs.get(keyFile.blobName).equals
                        (sPtComm.blobs.get(keyFile.blobName))) { //file is modified in given.
                    if (currCom.blobs.containsKey(keyFile.blobName)) {
                        if (currCom.blobs.get(keyFile.blobName).equals(sPtComm.blobs.
                                get(keyFile.blobName))) { //curr is NOT modified.
                            checkoutCommit(givenCom.commitID, keyFile.blobName);
                            add(keyFile.blobName);
                        }
                    }
                }
            }
        }
        for (String key : givenCom.blobs.keySet()) { // --CASE 4--
            Blob keyFile = (Blob) deserialization("Blobs/" + givenCom.blobs.get(key));
            if (!sPtComm.blobs.containsKey(keyFile.blobName)) { //file DNE in split.
                checkoutCommit(givenID, keyFile.blobName);
                add(keyFile.blobName);
            }
        }
    }

    public static void mergeHelper2(ArrayList<String> mergeConflicts,
                                    Commit currCommit, Commit givenCommit) {
        for (String key : mergeConflicts) {
            String value;
            byte[] h = "<<<<<<< HEAD\n".getBytes();
            byte[] d = "=======\n".getBytes(); // CASE 2,3,6 don't make any changes to file.
            byte[] a = ">>>>>>>\n".getBytes();
            if (currCommit.blobs.containsKey(key) && givenCommit.blobs.containsKey(key)) {
                String currValue = currCommit.blobs.get(key);
                Blob currConflict = (Blob) deserialization("Blobs/" + currValue);
                String givenValue = givenCommit.blobs.get(key);
                Blob givenConflict = (Blob) deserialization(("Blobs/" + givenValue));
                byte[] readCurr = currConflict.content;
                byte[] readGiven = givenConflict.content;
                byte[] newContentPls = new byte[h.length + d.length + a.length
                        + readCurr.length + readGiven.length];
                System.arraycopy(h, 0, newContentPls, 0, h.length);
                System.arraycopy(readCurr, 0, newContentPls, h.length, readCurr.length);
                System.arraycopy(d, 0, newContentPls, readCurr.length + h.length, d.length);
                System.arraycopy(readGiven, 0, newContentPls,
                        d.length + readCurr.length + h.length, readGiven.length);
                System.arraycopy(a, 0, newContentPls, d.length
                        + readCurr.length + h.length + readGiven.length, a.length);
                File fileOverWrite = new File("./" + currConflict.blobName);
                try {
                    Files.deleteIfExists(fileOverWrite.toPath());
                    Utils.writeContents(fileOverWrite, newContentPls);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { //case2: mergeConflict file only in current
                boolean check = true;
                Blob keyConflict;
                if (currCommit.blobs.containsKey(key)) { // present in curr and absent in given
                    value = currCommit.blobs.get(key); //case3: mergeConflict file only in given
                    keyConflict = (Blob) deserialization("Blobs/" + value);
                } else { //present in given and absent in curr
                    value = givenCommit.blobs.get(key);
                    keyConflict = (Blob) deserialization("Blobs/" + value);
                    check = false;
                }
                File oldFile = new File("./" + keyConflict.blobName);
                byte[] readFromFile = Utils.readContents(oldFile);
                byte[] newContent;
                newContent = new byte[h.length + readFromFile.length + d.length + a.length];
                if (check) { //top
                    System.arraycopy(h, 0, newContent, 0, h.length);
                    System.arraycopy(readFromFile, 0, newContent, h.length, readFromFile.length);
                    System.arraycopy(d, 0, newContent, h.length + readFromFile.length, d.length);
                    System.arraycopy(a, 0, newContent, h.length
                            + readFromFile.length + d.length, a.length);
                } else { //bottom
                    System.arraycopy(h, 0, newContent, 0, h.length);
                    System.arraycopy(d, 0, newContent, h.length, d.length);
                    System.arraycopy(readFromFile, 0, newContent,
                            d.length + h.length, readFromFile.length);
                    System.arraycopy(a, 0, newContent,
                            d.length + h.length + readFromFile.length, a.length);
                }
                Utils.writeContents(oldFile, newContent);
            }
        }
    }

    /** close. */
    protected static void serialization(String directoryName, Object obj) {
        File outFile = new File("./.gitlet/" + directoryName);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            System.out.println("Internal error serializing commit.");
            System.out.println(e.getMessage());
        }
    }

    /** open. */
    protected static Object deserialization(String directoryName) {
        File inFile = new File("./.gitlet/" + directoryName);
        Object obj;
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            obj = inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
        return obj;
    }

    /** Finds the split point in the tree. */
    private static Commit splitPointFinder(Commit currCommit, Commit commitToMerge) {
        if (currCommit.commitID.equals(commitToMerge.commitID)) {
            return currCommit;
        } else if (currCommit.getDate().before(commitToMerge.getDate())) {
            commitToMerge = (Commit) deserialization("Commits/" + commitToMerge.parent.commitID);
            return splitPointFinder(currCommit, commitToMerge);
        } else if (currCommit.getDate().after(commitToMerge.getDate())) {
            currCommit = (Commit) deserialization("Commits/" + currCommit.parent.commitID);
            return splitPointFinder(currCommit, commitToMerge);
        } else {
            System.out.println("Couldn't find splitpoint.");
            return null;
        }
    }

    private static String autoID(String id) {
        List<String> commits = Utils.plainFilenamesIn("./.gitlet/Commits");
        String fullID = null;
        for (String fileID : commits) {
            if (fileID.startsWith(id)) {
                fullID = fileID;
            }
        }
        return fullID;
    }
}
