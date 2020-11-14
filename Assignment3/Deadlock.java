import java.io.*;
import java.util.*;

public class Deadlock{

    public static void main (String[]args){

        String path = args[0];

        try {
            //file reader
            BufferedReader reader = new BufferedReader(new FileReader(path));
            
            //read each line
            String line = reader.readLine();
            
            //RAG instance
            RAG rag = new RAG();

            //check if process/resource objects/nodes were already created
            boolean processFound = false;
            boolean resourceFound = false;

            //init them 
            RAGnode processNode = new RAGnode(-1, "init");
            RAGnode resourceNode = new RAGnode(-1, "init");
            
            while (line != null){
                
                //get process 
                char p = line.charAt(0);
                int process = Integer.parseInt(String.valueOf(p));

                //check if process node was created already
                for (int i = 0; i < RAGnode.processesList.size(); i++){

                    if (RAGnode.processesList.get(i).getNodeIntValue() == process){
                        
                        //just get the object already created before
                        processNode = RAGnode.processesList.get(i);
                        processFound = true;
                    }

                }

                if (!processFound){

                    //create new process object/node
                    processNode = new RAGnode(process, "process");
                }

                
                //get action (wants or releases)
                char a = line.charAt(2);
                String action = "";
                
                if (a == 'W'){
                    action = "wants";
                }
                else if (a == 'R'){
                    action = "releases";
                }

                //get resource
                char r = line.charAt(4);
                int resource = Integer.parseInt(String.valueOf(r));

                //check if resource node was created already
                for (int i = 0; i < RAGnode.resourcesList.size(); i++){
                    if (RAGnode.resourcesList.get(i).getNodeIntValue() == resource){

                        //just get the object already created before
                        resourceNode = RAGnode.resourcesList.get(i);
                        resourceFound = true;
                    }

                }

                if (!resourceFound){

                    resourceNode = new RAGnode(resource, "resource");
                }

                System.out.printf("Process %d %s resource %d - ", process, action, resource);

                if (action.equals("wants")){
                    //assign resource if resource isn't being used
                    rag.addEdge(processNode, resourceNode);
                }

                else if (action.equals("releases")){
                    //assign resource if resource isn't being used
                    rag.removeEdge(processNode, resourceNode);
                }
                
                
                //reset values to false
                processFound = false;
                resourceFound = false;

                //go to the next line
                line = reader.readLine();

            }

            //close file
            reader.close();

            //if code reaches here it means no deadlock was found
            System.out.println("EXECUTION COMPLETED: No deadlock encountered.");
        
        }catch (IOException e){
            e.printStackTrace();
        }
        
    }
}

class RAG{

    public RAG(){
        //empy constructor
    }

    public void addEdge(RAGnode process, RAGnode resource){

        //check if deadlock has occurred
        boolean deadlock = false;
        //check if deadlock has not occurred but it was checked for so program must keep going
        boolean deadlockCheck = false;

        //keeps track of process and resources in the deadlock cycle
        ArrayList<Integer> processInDLCycle = new ArrayList<Integer>();
        ArrayList<Integer> resourcesInDLCycle = new ArrayList<Integer>(); 
        

        //resource is not being used
        if (resource.getNextRAGnode() == null){

            process.setNextRAGnode(resource);
            resource.setNextRAGnode(process);

            System.out.printf("Resource %d is allocated to process %d \n", resource.getNodeIntValue(), process.getNodeIntValue());
            
        }

        //resource is being used
        else{

            //deadlock will only happen if process is already holding another resource, so first we check for that
            if (process.getNextRAGnode() != null){

                //check for deadlock
                //start interating through the process and resources and see if a cycle is present
                //if a cycle is present, this loop should ended up back to the current process that's 
                //requesting for a resource at this moment

                //find in which waiting list of which resource the process is on it
                
                //while deadlock hasn't happened neither checked
                while (!deadlock && !deadlockCheck){

                    //get process holding research and resource being requested
                    RAGnode processObj = resource.getNextRAGnode();
                    processInDLCycle.add(processObj.getNodeIntValue());
                    resourcesInDLCycle.add(resource.getNodeIntValue());

                    //check if process is in the waiting list of any resource
                    for (int i = 0; i < RAGnode.resourcesList.size(); i++){

                        RAGnode resourceObj = RAGnode.resourcesList.get(i);

                        //if it is
                        if (RAGnode.resourcesList.get(i).getWaitingList().contains(processObj)){

                            processObj = resourceObj.getNextRAGnode();
                            processInDLCycle.add(processObj.getNodeIntValue());
                            resourcesInDLCycle.add(resourceObj.getNodeIntValue());

                            if (processObj == process){
                                System.out.printf("Process %d must wait \n", process.getNodeIntValue());
                                System.out.printf("DEADLOCK DETECTED: Processes ");

                                Collections.sort(processInDLCycle);

                                for (int j = 0; j < processInDLCycle.size(); j++){
                                    
                                    System.out.printf("%d", processInDLCycle.get(j));
                                    System.out.print(", ");
                                }
                                
                                System.out.print("and Resources ");

                                Collections.sort(resourcesInDLCycle);

                                for (int k = 0; k < resourcesInDLCycle.size(); k++){
                                    
                                    System.out.print(resourcesInDLCycle.get(k));
                                    System.out.print(", ");
                                }

                                System.out.print("are found in a cycle.\n");
                                deadlock = true;
                                break;

                            }

                            else{
                                //reset for loop
                                i = 0;
                                continue;
                            }
                        }

                        //check if its the last resource that was just checked
                        //if it is and if statements above didn't occur
                        //that means there's no deadlock
                        else if (i + 1 == RAGnode.resourcesList.size()){
                            deadlockCheck = true;
                            deadlock = false;
                            
                            //reset
                            processInDLCycle.clear();
                            resourcesInDLCycle.clear();
                            break;
                        }
                        
                    }
                }

                //if deadlock was checked (meaning there's no deadlock)
                if (deadlockCheck){
                    //just add process to the waiting list of the resource, no deadlock present
                    resource.getWaitingList().add(process);
                    System.out.printf("Process %d must wait \n", process.getNodeIntValue());
                    
                }

                else if (deadlock){
                    //break program
                    System.exit(1);
                }
            }

            
            else { 
                //just add process to the waiting list of the resource, no deadlock possible
                resource.getWaitingList().add(process);
                System.out.printf("Process %d must wait \n", process.getNodeIntValue());
            }
        }

    }
    
    public void removeEdge(RAGnode process, RAGnode resource){

        process.setNextRAGnode(null);
        resource.setNextRAGnode(null);

        //get process in the waiting list for the research if there is any
        if (resource.getWaitingList().size() > 0){

            RAGnode nextProcessWaiting = resource.getWaitingList().get(0);
            
            resource.setNextRAGnode(nextProcessWaiting);
            nextProcessWaiting.setNextRAGnode(resource);
            
            System.out.printf("Resource %d is allocated to process %d \n", resource.getNodeIntValue(), nextProcessWaiting.getNodeIntValue());
            
            resource.getWaitingList().remove(0);
        }

        else{
            System.out.printf("Resource %d is now free \n", resource.getNodeIntValue());
        }
    }

}

class RAGnode{

    private int nodeIntValue;
    private RAGnode nextRAGnode;


    //hashmap of all processes nodes created 
    public static ArrayList<RAGnode> processesList = new ArrayList<RAGnode>();

    //list of all processes nodes created
    public static ArrayList<RAGnode> resourcesList = new ArrayList<RAGnode>();

    //only used when node is a resource
    private ArrayList<RAGnode> waitingList;

    //getters

    public RAGnode(int nodeIntValue, String type){
        
        this.nodeIntValue = nodeIntValue;

        if (type.equals("process")){
            processesList.add(this);
        }

        if (type.equals("resource")){
            resourcesList.add(this);
            waitingList = new ArrayList<RAGnode>();  
        }
    }

    public int getNodeIntValue(){
        return this.nodeIntValue;
    }
    
    public RAGnode getNextRAGnode(){
        return this.nextRAGnode;
    }


    public ArrayList<RAGnode> getWaitingList(){
        return this.waitingList;
    }

    //setters

    public void setNextRAGnode(RAGnode rn){
        this.nextRAGnode = rn;
    }

}