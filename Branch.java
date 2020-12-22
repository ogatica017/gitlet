package gitlet;

import java.io.Serializable;
import java.io.File;

public class Branch implements Serializable {
    //List of Class Variables
    protected String head; //shaID of branch
    protected String name;
    protected final String branchCommitID;


    public Branch(String name, String shaID) {
        this.head = shaID;
        this.name = name;
        this.branchCommitID = shaID;

//        Pointer branchPointer = Pointer();
//        branchPointer.addBranch(name);
        Pointer currPointer = (Pointer) Main.deserialization("Curr Pointer");
        currPointer.addBranch(name); //Omar: adds the new branch name to the Pointer branch list
        Main.serialization("Curr Pointer", currPointer); //serializes pointer
        Main.serialization("Branches/" + name, this); // serializes branch
        //close pointer

//        this.set(); //We don't need this.
    }

    public void moveHead(String moveHead) {
        this.head = moveHead;
    }

    public void changeName(String changeName) {
        this.name = changeName;
    }

    public void set() {
        File newBranch = new File("./.gitlet/Branch" + branchCommitID);
        //try catch method
    }


}

