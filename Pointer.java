package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

public class Pointer implements Serializable {
    protected String head; //Name of the Branch
    protected ArrayList<String> branchPointerList = new ArrayList<>();

    public Pointer(String head) { //Head should be the string name of the branch it is pointing to
        this.head = head;
    }

    public void moveHead(String movehead) {
        this.head = movehead;
    }

    public void addBranch(String name) {
        branchPointerList.add(name);
    }

    public ArrayList<String> getBranchList() {
        return branchPointerList;
    }

    public void removeBranch(String name) {
        if (branchPointerList.contains(name)) {
            branchPointerList.remove(name);
        } else {
            System.out.println("No such branch exists.");
        }
    }

    //close method
}
