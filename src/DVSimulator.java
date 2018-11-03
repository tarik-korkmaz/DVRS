// Distance Vector Simulator

public class DVSimulator
{    
    // This is the number of nodes in the simulator
    public static final int NUMNODES = 5;


    // Parameters of the simulation
    private static EventList eventList;
    private static double time;

    // Data used for the simulation
    private Node[] nodes;
    public static int[][] cost;
    public static int[][] neighbors;

    // Initializes the simulator
    public DVSimulator()
    {
        eventList = new EventListImpl();
        time = 0.0;

        neighbors = new int[NUMNODES][];
        neighbors[0] = new int[] {1, 2};
        neighbors[1] = new int[] {0, 2, 3, 4};
        neighbors[2] = new int[] {0, 1, 3};
        neighbors[3] = new int[] {1, 2, 4};
        neighbors[4] = new int[] {1, 3};

        //Weight of the edges in the graph
        cost = new int[NUMNODES][NUMNODES];
        cost[0][0] = 0;
        cost[0][1] = 2;
        cost[0][2] = 11;
		cost[0][3] = 999;
        cost[0][4] = 999;
        
		cost[1][0] = 2;
        cost[1][1] = 0;
        cost[1][2] = 3;
        cost[1][3] = 4;
		cost[1][4] = 8;
		
		cost[2][0] = 11;
        cost[2][1] = 3;
        cost[2][2] = 0;
        cost[2][3] = 2;
		cost[2][4] = 999;
        
		cost[3][0] = 999;
        cost[3][1] = 4;
        cost[3][2] = 2;
        cost[3][3] = 0;
		cost[3][4] = 6;
		
		cost[4][0] =999 ;
		cost[4][1] = 8;
		cost[4][2] = 999;
		cost[4][3] = 6;
		cost[4][4] = 0;

        nodes = new Node[NUMNODES];
        for(int i = 0; i< NUMNODES; i++) {
            nodes[i] = new Node();
        }
    }
    
    // Starts the simulation. It will end when no more packets are in the medium
    public void runSimulator()
    {
        Event next;
        Packet p;
        
        while(true)
        {
            next = eventList.removeNext();
            
            if (next == null)
            {
                break;
            }

            System.out.println();
            System.out.println("main(): event received.  t=" +
                               next.getTime() +", node=" +
                               next.getEntity());
            p = next.getPacket();
            System.out.print("  src=" + p.getSource() + ", ");
            System.out.print("dest=" + p.getDest() + ", ");
            System.out.print("DV=[");
            for (int i = 0; i < NUMNODES - 1; i++)
            {
                System.out.print(p.getDV(i) + ", ");
            }
            System.out.println(p.getDV(NUMNODES - 1) + "]");
            
            time = next.getTime();

            p = next.getPacket();
            if ((next.getEntity() < 0) || (next.getEntity() >= NUMNODES))
            {
                System.out.println("main(): Panic. Unknown event node.");
            }
            else {
                nodes[next.getEntity()].updateDV(p);
            }
        }
        System.out.println();

        // build Forwarding table for each node
        for (Node n:nodes) {
            System.out.println("Node# " + n.getId());
            System.out.println("Converged after " + n.getNumUpdates() + " updates.");
            System.out.println();
            System.out.println("Distance Vector:");
            n.printDV();
            System.out.println();
            n.buildFwdTable();
            System.out.println("Forwarding Table");
            n.printFwdTable();
            System.out.println();
        }

        System.out.println();

        System.out.println("Simulator terminated at t=" + time +
                           ", no packets in medium.");        
    }
    
    // Sends a packet into the medium
    public static void sendPacket(Packet p)
    {
        Packet currentPacket;
        double arrivalTime;
    
        if ((p.getSource() < 0) || (p.getSource() >= NUMNODES))
        {
            System.out.println("sendPacket(): WARNING: Illegal source id in " +
                               "packet; ignoring.");
            return;
        }
        if ((p.getDest() < 0) || (p.getDest() >= NUMNODES))
        {
            System.out.println("sendPacket(): WARNING: Illegal destination id " +
                               "in packet; ignoring.");
            return;
        }
        if (p.getSource() == p.getDest())
        {
            System.out.println("sendPacket(): WARNING: Identical source and " +
                               "destination in packet; ignoring.");
            return;
        }
        if (cost[p.getSource()][p.getDest()] == 999)
        {
            System.out.println("sendPacket(): WARNING: Source and destination " +
                               "not connected; ignoring.");
            return;
        }

        
        arrivalTime = eventList.getLastPacketTime(p.getSource(), p.getDest());
        if (arrivalTime == 0.0)
        {
            arrivalTime = time;
        }
        arrivalTime = arrivalTime + 9.0;

        currentPacket = new Packet(p);
        eventList.add(new Event(arrivalTime,
                                currentPacket.getDest(), currentPacket));
    }

}
