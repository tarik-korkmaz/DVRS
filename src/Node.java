import java.util.Arrays;
import java.util.Random;

public class Node
{
    private static int count = 0;

    // Each node will store a costs vector, its own distance vector
    // and a distance vector for each of its neighbors
    private int[] neighbors;
    private int[] cost = new int[DVSimulator.NUMNODES];
    private int[] myDV = new int[DVSimulator.NUMNODES];
    private int[][] neighborDV = new int[DVSimulator.NUMNODES][DVSimulator.NUMNODES];
    private int id;
    // fwd table specifies for reaching each destination (index) from current node
    // which neighbor node we should visit first
    private int[] fwdTable = new int[DVSimulator.NUMNODES];
    // bestPath is a temporary changing forwarding table
    private int[] bestPath = new int[DVSimulator.NUMNODES];
    private int numUpdates = 0;

    /**
     * Initializes a node.
     */
    public Node() {
        this.id = count++;

        /**
         * Initialization of neighbors and cost arrays. DVSimulator already stores these arrays.
         * Therefore, neighbors and cost variables can be assigned to the arrays in DVSimulator directly.
         */
        neighbors = DVSimulator.neighbors[id];
        cost = DVSimulator.cost[id];

        /**
         * To be sure Arrays.binarySearch, which is used to check whether a node is one the neighbors of this node,
         * works properly, neighbors array is sorted in case it is not sorted in DVSimulator.
         */
        Arrays.sort(neighbors);

        for (int i = 0; i<DVSimulator.NUMNODES; i++) {
            // reading from the DVSimulator variables,
            // for each node:
            // 1. initialize its cost and myDV value
            // 2. specify the neighbors
            // 3. Initialize the forwarding table (bestPath variable)

            // BestPath to any node should be initialized as follows:
            // If node has id = this node's id, use id
            // Else if node is a direct neighbor, use the neighbor id
            // Otherwise, choose a random neighbor (see randomNeighbor method)

            /**
             * Initially, myDV values should be identical to the cost values.
             * Cost array is copied to myDV.
             */
            myDV[i] = cost[i];

            /**
             * If the node's id is same as current node's id, initial value in bestPath array should be this id.
             * Arrays.binarySearch method (which is a method of Arrays class in java) is used to check if node with
             * id: i is a neighbor of current node by searching i in neighbors array. If i is a neighbor,
             * then result must be non-negative. If that is the case, initial value in bestPath array
             * should be i which is neighbor's id. If node is not this node and is not one of this node's neighbors,
             * then initially bestPath stores a random neighbor, which is obtained by calling randomNeighbor() method.
             */
            if(i == id){
                bestPath[i] = id;
            }else if(Arrays.binarySearch(neighbors, i) >= 0){
                bestPath[i] = i;
            }else{
                bestPath[i] = randomNeighbor();
            }
        }

        // send initial DV to neighbors
        notifyNeighbors();
    }

    /**
     *
     * @return The ID of this node.
     */
    public int getId() {
        return id;
    }

    /**
     * Prints the distance vector of this node.
     */
    public void printDV() {
        System.out.print("i            " );
        for (int i = 0; i<DVSimulator.NUMNODES; i++) {
            System.out.print(i + "      ");
        }
        System.out.println();
        System.out.print("cost         " );
        for (int i = 0; i<DVSimulator.NUMNODES; i++) {
            System.out.print(myDV[i] + "      ");
        }
        System.out.println();
    }

    /**
     * Prints the forwarding table of this node.
     */
    public void printFwdTable() {
        System.out.println("dest         next Node" );
        for (int i = 0; i<DVSimulator.NUMNODES; i++) {
            System.out.println(i + "            " + fwdTable[i]);
        }
    }

    /**
     *
     * @return id of a node that is randomly selected among neighbors of this node.
     */
    public int randomNeighbor() {
        int rnd = new Random().nextInt(neighbors.length);
        return neighbors[rnd];
    }

    /**
     * Notifies neighbor nodes by sending them the distance vector DV.
     */
    public void notifyNeighbors() {
        // for each neighbor, create a new packet (see Packet class)
        // with current node id as source, neighbor id as destination
        // and current node's DV as the dv
        // then send packet using helper method sendPacket in DVSimulator

        /**
         * For each neighbor node, create a packet and send it using DVSimulator class.
         * Source id field of the packet is set to the ID of this node, destination id field of the packet
         * is set to the id of the neighbor node. Distance vector field, dv, is set to the distance vector of this
         * node which is myDV.
         */
        for(int i=0; i<neighbors.length; i++){
            int neighborID = neighbors[i];
            Packet p = new Packet(id, neighborID, myDV);
            DVSimulator.sendPacket(p);
        }
    }

    /**
     * Updates the distance vector of this node according to the received packet if an update is necessary.
     *
     * @param p: Received packet which contains the necessary information for an update of distance vector myDV, if
     *         it is needed.
     */
    public void updateDV(Packet p) {
        // this method is called by the simulator each time a packet is received from a neighbor

        /**
         * ID of the node that sent this packet is extracted.
         * neighborDV is updated to hold the newest distance vector of neighbor that sent this packet.
         */
        int neighbor_id = p.getSource();
        neighborDV[neighbor_id] = p.getDV();

        // for each value in the DV received from neighbor, see if it provides a cheaper path to
        // the corresponding node. If it does, update myDV and bestPath accordingly
        // current DV of i is min { current DV of i, cost to neighbor + neighbor's DV to i  }

        // If you do any changes to myDV:
        // 1. Notify neighbors about the new myDV using notifyNeighbors() method
        // 2. Increment the convergence measure numUpdates variable once

        /**
         * Received distance vector is extracted.
         */
        int[] receivedDV = p.getDV();

        /**
         * Variable dvUpdated shows whether the distance vector of this node is updated.
         * Cost to the neighbor which sent the packet to this node is assigned to costToNeighbor variable.
         * For each entry with index i in received distance vector, costToNeighbor + receivedDV's i indexed value
         * is calculated. If this value is better than the index i value of this node's distance vector myDV,
         * index i of myDV is updated. This means a better path to node i from this node is found. If any updates to myDV
         * is made, dvUpdated variable is assigned to true and also bestPath[i] is assigned to neighbor_id since new best
         * path to node i passes through the neighbor that sent this packet.
         */
        boolean dvUpdated = false;
        int costToNeighbor = cost[neighbor_id];
        for(int i=0; i<receivedDV.length; i++){
            if(costToNeighbor + receivedDV[i] < myDV[i]){
                dvUpdated = true;
                myDV[i] = costToNeighbor + receivedDV[i];
                bestPath[i] = neighbor_id;
            }
        }

        /**
         * If any updates to myDV is made, which is checked by the variable dvUpdated, numUpdates is incremented by one
         * and neighbors of this node is notified by calling notifyNeighbors() method.
         */
        if(dvUpdated){
            numUpdates++;
            notifyNeighbors();
        }
    }

    /**
     * Builds the forwarding of this node.
     */
    public void buildFwdTable() {
        // just copy the final values of bestPath vector
        for (int i = 0; i < DVSimulator.NUMNODES; i++) {
            fwdTable[i] = bestPath[i];
        }
    }

    /**
     *
     * @return Number of updates made to the distance vector of this node.
     */
    public int getNumUpdates() {
        return numUpdates;
    }
}
