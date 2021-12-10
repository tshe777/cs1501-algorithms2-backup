
/*************************************************************************
*  An Airline management system that uses a weighted-edge directed graph 
*  implemented using adjacency lists.
  Tao Sheng CS 1501 Project 4 Spring 2021
*************************************************************************/
import java.util.*;
import java.io.*;

public class AirlineSystem {
  private String [] cityNames = null; //Array to hold names of city from input file
  private Digraph G = null; //Init Digraph structure
  private static Scanner scan = null; //Main menu functionality
  private static final int INFINITY = Integer.MAX_VALUE; //INFINITY const
  private static boolean saveNow = false; //Updates to true if file was changed to prompt save and quit to save. 

  public static void main(String[] args) throws Exception {
    //Setup main menu to run always unless user quits 
    AirlineSystem airline = new AirlineSystem();
    scan = new Scanner(System.in);
    while(true){
      switch(airline.menu()){
        case 0:
          airline.readGraph();
          break;
        case 1:
          airline.printGraph();
          break;
        case 2:
          airline.printMST();
          break;
        case 3:
          airline.shortestHops();
          break;
        case 4:
          airline.shortestDistance();
          break;
        case 5: 
          airline.shortestCost();
          break;
        case 6: 
          airline.allPathsLessThan();
          break;
        case 7: 
          airline.addRoute();
          break;
        case 8:
          airline.removeRoute();
          break;
        case 9:
          airline.saveThenQuit();
          break;
        default:
          System.out.println("Incorrect option.");
      }
    }
  }

  //Prints menu repeatedly after each action. 
  private int menu(){
    System.out.println("*********************************");
    System.out.println("Welcome to FifteenO'One Airlines!");
    System.out.println("0. Read data from a file.");
    System.out.println("1. Display all routes, distances, and prices.");
    System.out.println("2. Display the MST");
    System.out.println("3. Compute shortest path based on number of hops.");
    System.out.println("4. Compute shortest path based on distance in miles.");
    System.out.println("5. Compute shortest path based on price.");
    System.out.println("6. Display all trips less than or equal to a given amount.");
    System.out.println("7. Add a route.");
    System.out.println("8. Remove a route.");
    System.out.println("9. Save and Exit.");
    System.out.println("*********************************");
    System.out.print("Please choose a menu option (0-9): ");

    int choice = Integer.parseInt(scan.nextLine());
    return choice;
  }
  //Reads graph from the standardized data file format of first line: 
  //number of vertices, next lines of city names and next lines of the edges with 2 weights: distance in miles and price in USD. 
  private void readGraph() throws Exception {
    System.out.println("Please enter graph filename:");
    String fileName = scan.nextLine();
    Scanner fileScan = new Scanner(new FileInputStream(fileName));
    int v = Integer.parseInt(fileScan.nextLine()); //Reads number of vertices from top of file
    G = new Digraph(v); //Init graph of size # of vertices

    //Reading city names into class array
    cityNames = new String[v];
    for(int i=0; i<v; i++){
      cityNames[i] = fileScan.nextLine();
    }
    //Reading edge information, distances, and prices. 
    while(fileScan.hasNext()){
      int from = fileScan.nextInt();
      int to = fileScan.nextInt();
      int weight = fileScan.nextInt();
      int price = (int)fileScan.nextDouble();
      G.addEdge(new WeightedDirectedEdge(from-1, to-1, weight, price));
      G.addEdge(new WeightedDirectedEdge(to-1, from-1, weight, price));
    }
    fileScan.close();
    System.out.println("Data imported successfully.");
    System.out.print("Please press ENTER to continue ...");
    scan.nextLine();
  }

  //Shows the entire list of direct routes, distances and prices.
  private void printGraph() {
    if(G == null){
      System.out.println("Please import a graph first (option 1).");
      System.out.print("Please press ENTER to continue ...");
      scan.nextLine();
    } else {
        for (int i = 0; i < G.v; i++) {
          for (WeightedDirectedEdge e: G.adj(i)) {
            System.out.println(cityNames[e.from()] + " to " + cityNames[e.to()] + " is " + e.weight() + " miles and costs $" + e.price());
          }
        }
        System.out.print("Please press ENTER to continue ...");
        scan.nextLine();
    }
  }
    //Given a dollar amount entered by the user, print out all trips whose cost is less than or equal to that amount.
    //Pruning search that uses a recursive helper method called allPathsHelper
  public void allPathsLessThan() {
    //Awaits user input for price to find <= routes of
    System.out.print("Please enter a price to find all routes less than or equal to that price. ");
    int cost = Integer.parseInt(scan.nextLine());
    ArrayList<String> route = new ArrayList<>(); //Arraylist to hold routes
    //Iterate through graph and check all possible paths for every vertex. Ignore the middle line in adjaceny matrix. 
    for (int i = 0; i < G.v; i++) {
      for (int j = 1; j < cityNames.length; j++) {
        if (j == i) {
          j++;
          if (j == cityNames.length) break;
        }
        //Init an arraylist of arraylists of integers to hold all of the paths. Call recursive helper to fill arraylist of arraylists. 
        ArrayList<ArrayList<Integer>> allPaths = new ArrayList<ArrayList<Integer>>();
        allPathsHelper(i, j, allPaths, new LinkedHashSet<Integer>());
        //Iterate over arraylist of arraylists and for every possible path, check which ones when added with the starting city, yield a price <= what the user requested. 
        for (int k = 0; k < allPaths.size(); k++) {
          Iterator<Integer> path = allPaths.get(k).iterator();
          int price = 0; int city1 = path.next(); int city2 = path.next();  
          StringBuilder output = new StringBuilder (cityNames[city1]);
          //Utilize iterator for .next() ability. Check prices and only append to output stringbuilder if price is <= user request. 
          while (true) {
            Iterator <WeightedDirectedEdge> e = G.adj(city1).iterator();
            while (e.hasNext()) {
              WeightedDirectedEdge edge = e.next();
              if (edge.to() == city2) {
                price += edge.price();
                output.append(" " + edge.price() + " " + cityNames[city2]);
              }
            }
            //Stop when no more paths
            if (!path.hasNext()) break;
            city1 = city2;
            city2 = path.next();
          }
          //Print possible paths <= cost. 
          if (price <= cost) {
            String print ="Cost: $" + price + " " + output;
            if (!route.contains(print)) {
              System.out.println(print);
              route.add(print);
            }
          }
        }
      }
    }
  }
  //Recursive helper method for finding all paths disregarding price. Takes in two vertices, the ArrayList of ArrayLists to update, and a LinkedHashSet to hold the paths. 
  private void allPathsHelper(int source, int destination, ArrayList<ArrayList<Integer>> paths, LinkedHashSet<Integer> pathSet) {
    //Add the source vertex first.  
    pathSet.add(source);
    //Ignore when vertices are the same. 
    if (source == destination) {
      paths.add(new ArrayList<Integer>(pathSet));
      pathSet.remove(source);
      return;
    }
    //Iterate graph and try adding edge to path. Work until all paths are filled into the ArrayList
    ArrayList<Integer> edges = new ArrayList<>();
    Iterator<WeightedDirectedEdge> e = G.adj(source).iterator();
    while (e.hasNext()) {
      edges.add(e.next().to());
    }
    for (int s: edges) {
      if (!pathSet.contains(s)) {
        allPathsHelper(s, destination, paths, pathSet);
      }
    }
    //Call remove to backtrack after recursive calls on the callstack. 
    pathSet.remove(source);
  }
  //Method to quit (and save if needed). 
  public void saveThenQuit() throws IOException {
      System.out.println("Please enter graph filename to save as. ");
      String filename = scan.nextLine();
      if (G == null) {
        System.out.println("No graph data detected. Exiting system without saving. ");  
        return;
      } else if (!saveNow) {
        scan.close();
        System.out.println("No route changes were made, therefore the file wasn't updated. Exiting System. ");
        System.exit(0);
      } else { //Needs to save, therefore will overwrite the file, given the user supplies a new name. 
        FileWriter filewriter = new FileWriter(filename, false);
        filewriter.write((cityNames.length) + "\n");
        //Backwards of printGraph - writing citynames, and then edge and distance and price data.  
        for (int i = 0; i < cityNames.length; i++) {
          filewriter.write(cityNames[i] + "\n");
        }
        for (int j = 0; j < G.v; j++) {
          for (WeightedDirectedEdge e: G.adj(j)) {
            int from = e.from() + 1;
            int to = e.to() + 1;
            if (from < to) filewriter.write(from + " " + to + " " + e.weight() + " " + e.price() + ".00" + "\n");
          }
        }
        filewriter.close();
        scan.close();
        System.out.println("File has successfully been updated. Exiting system. ");
        System.exit(0);
      }

    }

    //Method to add a route. Asks user to supply the vertices, and two weights for the distance and price. Will add a new edge if route already exists. 
    public void addRoute(){
        if(G == null){
          System.out.println("Please import a graph first (option 1).");
          System.out.print("Please press ENTER to continue ...");
          scan.nextLine();
        } else {
          for(int i=0; i<cityNames.length; i++){
            System.out.println(i+1 + ": " + cityNames[i]);
          }
          System.out.print("Please enter source city (1-" + cityNames.length + "): ");
          int source = Integer.parseInt(scan.nextLine());
          System.out.print("Please enter destination city (1-" + cityNames.length + "): ");
          int destination = Integer.parseInt(scan.nextLine());
          source--;
          destination--;
          for (int i = 0; i < G.v; i++) {
            for (WeightedDirectedEdge e: G.adj(i)) {
              if (e.from() == source && e.to() == destination) {
                System.out.print("Route already exists with some distance and price. Continuing will override old edge with new information. ");
              }
            }
          }
          System.out.print("Please enter the distance in miles of this trip: ");
          int miles = Integer.parseInt(scan.nextLine());
          System.out.print("Please enter the cost of this trip: ");
          int price = Integer.parseInt(scan.nextLine());
          G.addEdge(new WeightedDirectedEdge(source, destination, miles, price));
          G.addEdge(new WeightedDirectedEdge(destination, source, miles, price));
          System.out.println("Successfully added a new route from " + cityNames[source] + " to " + cityNames[destination] + " for a distance of " + miles + " miles costing $" + price);  
          saveNow = true;
          System.out.print("Please press ENTER to continue ...");
          scan.nextLine();
        }
    }

    //Removes a route, given the user supplies both vertices. Will remove top to bottom from the file if there are two different edges for the same two vertices (if add above does this).
    public void removeRoute() {
        if(G == null){
          System.out.println("Please import a graph first (option 1).");
          System.out.print("Please press ENTER to continue ...");
          scan.nextLine();
        } else {
          for(int i=0; i<cityNames.length; i++){
            System.out.println(i+1 + ": " + cityNames[i]);
          }
          System.out.print("Please enter source city (1-" + cityNames.length + "): ");
          int source = Integer.parseInt(scan.nextLine());
          System.out.print("Please enter destination city (1-" + cityNames.length + "): ");
          int destination = Integer.parseInt(scan.nextLine());
          source--;
          destination--;
          //Try removing, and if successful, remove edge from the other vertex. 
          if (G.removeEdge(source,destination)) {
            G.removeEdge(destination,source);
            System.out.println("Successfully removed the route from " + cityNames[source] + " to " + cityNames[destination]);  
            saveNow = true;
          } else {
            System.out.println("Unsuccessfully removed route because it does not exist. ");  
          }

          System.out.print("Please press ENTER to continue ...");
          scan.nextLine();
        }
    } 

    //Method to find the shortest path by number of destinations or hops. Uses breadth-first-search as defined in the Digraph class. 
    private void shortestHops() {
      if(G == null){
        System.out.println("Please import a graph first (option 1).");
        System.out.print("Please press ENTER to continue ...");
        scan.nextLine();
      } else {
        for(int i=0; i<cityNames.length; i++){
          System.out.println(i+1 + ": " + cityNames[i]);
        }
        System.out.print("Please enter source city (1-" + cityNames.length + "): ");
        int source = Integer.parseInt(scan.nextLine());
        System.out.print("Please enter destination city (1-" + cityNames.length + "): ");
        int destination = Integer.parseInt(scan.nextLine());
        source--;
        destination--;
        G.bfs(source);
        if(!G.marked[destination]){
          System.out.println("There is no route from " + cityNames[source]
                              + " to " + cityNames[destination]);
        } else {
            //Stack holds path for popping. 
            Stack<Integer> path = new Stack<>();
            for (int x = destination; x != source; x = G.edgeTo[x]){
                path.push(x);
            }
            System.out.print("The shortest route from " + cityNames[source] +
                              " to " + cityNames[destination] + " has " +
                              G.distTo[destination] + " hop(s): ");
            //Construct the shortest path from the edgeTo array and prints shortest distance by hops from the distTo array.
            int prevVertex = source;
            System.out.print(cityNames[source] + " ");
            while(!path.empty()){
              int v = path.pop();
              System.out.print(cityNames[v] + " ");
              prevVertex = v;
            }
            System.out.println();
        }
        System.out.print("Please press ENTER to continue ...");
        scan.nextLine();
      }

    }
     //Method to find the shortest path by number of miles between them or distance. Uses Dijkstra's algorithm as defined in the Digraph class. 
    private void shortestDistance() {
      if(G == null){
        System.out.println("Please import a graph first (option 1).");
        System.out.print("Please press ENTER to continue ...");
        scan.nextLine();
      } else {
        for(int i=0; i<cityNames.length; i++){
          System.out.println(i+1 + ": " + cityNames[i]);
        }
        System.out.print("Please enter source city (1-" + cityNames.length + "): ");
        int source = Integer.parseInt(scan.nextLine());
        System.out.print("Please enter destination city (1-" + cityNames.length + "): ");
        int destination = Integer.parseInt(scan.nextLine());
        source--;
        destination--;
        G.dijkstras(source, destination);
        if(!G.marked[destination]){
          System.out.println("There is no route from " + cityNames[source]
                              + " to " + cityNames[destination]);
        } else {
            //Construct the shortest path from the edgeTo array and prints shortest distance by distance weight (in miles) from the distTo array.
          Stack<Integer> path = new Stack<>();
          for (int x = destination; x != source; x = G.edgeTo[x]){
              path.push(x);
          }
          System.out.print("The shortest route from " + cityNames[source] +
                             " to " + cityNames[destination] + " has " +
                             G.distTo[destination] + " miles: ");

          int prevVertex = source;
          System.out.print(cityNames[source] + " ");
          while(!path.empty()){
            int v = path.pop();
            System.out.print(G.distTo[v] - G.distTo[prevVertex] + " "
                             + cityNames[v] + " ");
            prevVertex = v;
          }
          System.out.println();

        }
        System.out.print("Please press ENTER to continue ...");
        scan.nextLine();
      }
  }

  
     //Method to find the shortest path by price only. Uses a variation of Djikstra's algorithm as defined in the Digraph class called dijkstrasPrice(). 
    private void shortestCost() {
      if(G == null){
        System.out.println("Please import a graph first (option 1).");
        System.out.print("Please press ENTER to continue ...");
        scan.nextLine();
      } else {
        for(int i=0; i<cityNames.length; i++){
          System.out.println(i+1 + ": " + cityNames[i]);
        }
        System.out.print("Please enter source city (1-" + cityNames.length + "): ");
        int source = Integer.parseInt(scan.nextLine());
        System.out.print("Please enter destination city (1-" + cityNames.length + "): ");
        int destination = Integer.parseInt(scan.nextLine());
        source--;
        destination--;
        G.dijkstrasPrice(source, destination);
        if(!G.marked[destination]){
          System.out.println("There is no route from " + cityNames[source]
                              + " to " + cityNames[destination]);
        } else {
          Stack<Integer> path = new Stack<>();
          for (int x = destination; x != source; x = G.edgeTo[x]){
              path.push(x);
          }
          System.out.print("The shortest route from " + cityNames[source] +
                             " to " + cityNames[destination] + " costs $: " +
                             G.priceTo[destination]);
        //Construct the shortest path from the edgeTo array and prints shortest distance by price weight (in USD) from the priceTo array.
          int prevVertex = source;
          System.out.print(cityNames[source] + " ");
          while(!path.empty()){
            int v = path.pop();
            System.out.print(G.priceTo[v] - G.priceTo[prevVertex] + " "
                             + cityNames[v] + " ");
            prevVertex = v;
          }
          System.out.println();

        }
        System.out.print("Please press ENTER to continue ...");
        scan.nextLine();
      }
  }

  //Prints the minimum-spanning tree using Prim's algorithm. See Prim's Algorithm below WeightedDirectedEdge class. Dependencies include:
  /*
    QueueMST.java
    UF.java
    IndexMinPQ.java

  */
	public void printMST(){
		PrimMST p = new PrimMST(G);
    Iterator<WeightedDirectedEdge> e = p.edges().iterator();
    while(e.hasNext()){
        WeightedDirectedEdge edge = e.next();
        System.out.println(cityNames[edge.to()] + ", " + cityNames[edge.from()] + ": " + edge.weight() + " miles.");
    }
	}




  /**
  *  The <tt>Digraph</tt> class represents an directed graph of vertices
  *  named 0 through v-1. It supports the following operations: add an edge to
  *  the graph, iterate over all of edges leaving a vertex.Self-loops are
  *  permitted.
  */
  private class Digraph {
    private final int v;
    private int e;
    private LinkedList<WeightedDirectedEdge>[] adj;
    private boolean[] marked;  // marked[v] = is there an s-v path
    private int[] edgeTo;      // edgeTo[v] = previous edge on shortest s-v path
    private int[] distTo;      // distTo[v] = number of edges shortest s-v path
    private int [] priceTo; 

    /**
    * Create an empty digraph with v vertices.
    */
    public Digraph(int v) {
      if (v < 0) throw new RuntimeException("Number of vertices must be nonnegative");
      this.v = v;
      this.e = 0;
      @SuppressWarnings("unchecked")
      LinkedList<WeightedDirectedEdge>[] temp =
      (LinkedList<WeightedDirectedEdge>[]) new LinkedList[v];
      adj = temp;
      for (int i = 0; i < v; i++)
        adj[i] = new LinkedList<WeightedDirectedEdge>();
    }

    /**
    * Add the edge e to this digraph.
    */
    public void addEdge(WeightedDirectedEdge edge) {
      int from = edge.from();
      adj[from].add(edge);
      e++;
    }

    //Removes an edge given the two vertices. Removes only once, is needed to be called twice to remove from both ends. 
    public boolean removeEdge(int v1, int v2) {
        for (WeightedDirectedEdge w: adj(v1)) {
          if (w.from() == v1 && w.to() == v2) {
            adj[v1].remove(w);
            e--;
            return true;
          }

        }
        return false; 
    }

    /**
    * Return the edges leaving vertex v as an Iterable.
    * To iterate over the edges leaving vertex v, use foreach notation:
    * <tt>for (WeightedDirectedEdge e : graph.adj(v))</tt>.
    */
    public Iterable<WeightedDirectedEdge> adj(int v) {
      return adj[v];
    }

    //Breadth-first-search algorithm that finds the short path by number of hops called by method above with the source vertex. 

    public void bfs(int source) {
      marked = new boolean[this.v];
      distTo = new int[this.e];
      edgeTo = new int[this.v];

      Queue<Integer> q = new LinkedList<Integer>();
      for (int i = 0; i < v; i++){
        distTo[i] = INFINITY;
        marked[i] = false;
      }
      distTo[source] = 0;
      marked[source] = true;
      q.add(source);

      while (!q.isEmpty()) {
        int v = q.remove();
        for (WeightedDirectedEdge w : adj(v)) {
          if (!marked[w.to()]) {
            edgeTo[w.to()] = v;
            distTo[w.to()] = distTo[v] + 1;
            marked[w.to()] = true;
            q.add(w.to());
          }
        }
      }
    }

    // Implementation of dijkstra's algorithm. Takes the source and destination vertices. 
    public void dijkstras(int source, int destination) {
      marked = new boolean[this.v];
      distTo = new int[this.v];
      edgeTo = new int[this.v];


      for (int i = 0; i < v; i++){
        distTo[i] = INFINITY;
        marked[i] = false;
      }
      distTo[source] = 0;
      marked[source] = true;
      int nMarked = 1;

      int current = source;
      while (nMarked < this.v) {
        for (WeightedDirectedEdge w : adj(current)) {
          if (distTo[current]+w.weight() < distTo[w.to()]) {
            distTo[w.to()] = distTo[current] + w.weight();
            edgeTo[w.to()] = current;
          }
        }
        int min = INFINITY;
        current = -1;

        for(int i=0; i<distTo.length; i++){
          if(marked[i])
            continue;
          if(distTo[i] < min){
            min = distTo[i];
            current = i;
          }
        }
        if (current == -1) {
          break;
        } else {
          marked[current] = true;
          nMarked++;
        }

      }
    }
    // Custom implementation of dijkstra's algorithm. Takes the source and destination vertices and bases path based on price, not distance. 

      public void dijkstrasPrice(int source, int destination) {
      marked = new boolean[this.v];
      priceTo = new int[this.v];
      edgeTo = new int[this.v];


      for (int i = 0; i < v; i++){
        priceTo[i] = INFINITY;
        marked[i] = false;
      }
      priceTo[source] = 0;
      marked[source] = true;
      int nMarked = 1;

      int current = source;
      while (nMarked < this.v) {
        for (WeightedDirectedEdge w : adj(current)) {
          if (priceTo[current]+w.price() < priceTo[w.to()]) {
            priceTo[w.to()] = priceTo[current] + w.price();
            edgeTo[w.to()] = current;
          }
        }
        int min = INFINITY;
        current = -1;

        for(int i=0; i<priceTo.length; i++){
          if(marked[i])
            continue;
          if(priceTo[i] < min){
            min = priceTo[i];
            current = i;
          }
        }
        if (current == -1) {
          break;
        } else {
          marked[current] = true;
          nMarked++;
        }

      }
    }
  }
  /**
  *  The <tt>WeightedDirectedEdge</tt> class represents a weighted edge in an directed graph.
  */

  private class WeightedDirectedEdge {
    private final int v;
    private final int w;
    private int weight;
    private int price;
    /**
    * Create a directed edge from v to w with given weight.
    */
    public WeightedDirectedEdge(int v, int w, int weight, int price) {
      this.v = v;
      this.w = w;
      this.weight = weight;
      this.price = price;
    }

    public int from(){
      return v;
    }

    public int to(){
      return w;
    }

    public int weight(){
      return weight;
    }
    public int price(){
      return price;
    }
  }
  

  
/**
 * The following code is heavily adapted from Robert Sedgewick and Kevin Wayne's Algorithms, 4th edition code. 
 * Changes were made to Queue to rename it as the default java Queue was used for the above AirlineSystem BFS method.
 * Changes were made to utilize my custom WeightedDirectedEdge instead of what was originally written as using Edge.java
 * Dependencies for the PrimMST itself:
 *  QueueMST.java
 *  UF.java
 *  IndexMinPQ.java
 * 
 * 
 *  The {@code PrimMST} class represents a data type for computing a
 *  <em>minimum spanning tree</em> in an edge-weighted graph.
 *  The edge weights can be positive, zero, or negative and need not
 *  be distinct. If the graph is not connected, it computes a <em>minimum
 *  spanning forest</em>, which is the union of minimum spanning trees
 *  in each connected component. The {@code weight()} method returns the 
 *  weight of a minimum spanning tree and the {@code edges()} method
 *  returns its edges.
 *  <p>
 *  This implementation uses <em>Prim's algorithm</em> with an indexed
 *  binary heap.
 *  The constructor takes &Theta;(<em>E</em> log <em>V</em>) time in
 *  the worst case, where <em>V</em> is the number of
 *  vertices and <em>E</em> is the number of edges.
 *  Each instance method takes &Theta;(1) time.
 *  It uses &Theta;(<em>V</em>) extra space (not including the 
 *  edge-weighted graph).
 *  <p>
 *  For additional documentation,
 *  see <a href="https://algs4.cs.princeton.edu/43mst">Section 4.3</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *  For alternate implementations, see {@link LazyPrimMST}, {@link KruskalMST},
 *  and {@link BoruvkaMST}.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
  private class PrimMST {
  private static final double FLOATING_POINT_EPSILON = 1E-12;

  private WeightedDirectedEdge[] edgeTo;        // edgeTo[v] = shortest edge from tree vertex to non-tree vertex
  private double[] distTo;      // distTo[v] = weight of shortest such edge
  private boolean[] marked;     // marked[v] = true if v on tree, false otherwise
  private IndexMinPQ<Double> pq;

  /**
   * Compute a minimum spanning tree (or forest) of an edge-weighted graph.
   * @param G the edge-weighted graph
   */
  public PrimMST(Digraph G) {
      edgeTo = new WeightedDirectedEdge[G.v];
      distTo = new double[G.v];
      marked = new boolean[G.v];
      pq = new IndexMinPQ<Double>(G.v);
      for (int v = 0; v < G.v; v++)
          distTo[v] = Double.POSITIVE_INFINITY;

      for (int v = 0; v < G.v; v++)      // run from each vertex to find
          if (!marked[v]) prim(G, v);      // minimum spanning forest
  }

  // run Prim's algorithm in graph G, starting from vertex s
  private void prim(Digraph G, int s) {
      distTo[s] = 0.0;
      pq.insert(s, distTo[s]);
      while (!pq.isEmpty()) {
          int v = pq.delMin();
          scan(G, v);
      }
  }

  // scan vertex v
  private void scan(Digraph G, int v) {
      marked[v] = true;
      for (WeightedDirectedEdge e : G.adj(v)) {
          if (v == e.to()) 
          {
            int w = e.from();
            if (marked[w]) continue;         // v-w is obsolete edge
          if (e.weight() < distTo[w]) {
              distTo[w] = e.weight();
              edgeTo[w] = e;
              if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
              else                pq.insert(w, distTo[w]);
          }
          } else {
            int w = e.to();
            if (marked[w]) continue;         // v-w is obsolete edge
          if (e.weight() < distTo[w]) {
              distTo[w] = e.weight();
              edgeTo[w] = e;
              if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
              else                pq.insert(w, distTo[w]);
          }
          }
          
      }
  }

  /**
   * Returns the edges in a minimum spanning tree (or forest).
   * @return the edges in a minimum spanning tree (or forest) as
   *    an iterable of edges
   */

  public Iterable<WeightedDirectedEdge> edges() {
      QueueMST<WeightedDirectedEdge> mst = new QueueMST<WeightedDirectedEdge>();
      for (int v = 0; v < edgeTo.length; v++) {
          WeightedDirectedEdge e = edgeTo[v];
          if (e != null) {
            mst.enqueue(e);
          }
      }
      return mst;
  }

}



/**
 *  The {@code UF} class represents a <em>union–find data type</em>
 *  (also known as the <em>disjoint-sets data type</em>).
 *  It supports the classic <em>union</em> and <em>find</em> operations,
 *  along with a <em>count</em> operation that returns the total number
 *  of sets.
 *  <p>
 *  The union–find data type models a collection of sets containing
 *  <em>n</em> elements, with each element in exactly one set.
 *  The elements are named 0 through <em>n</em>–1.
 *  Initially, there are <em>n</em> sets, with each element in its
 *  own set. The <em>canonical element</em> of a set
 *  (also known as the <em>root</em>, <em>identifier</em>,
 *  <em>leader</em>, or <em>set representative</em>)
 *  is one distinguished element in the set. Here is a summary of
 *  the operations:
 *  <ul>
 *  <li><em>find</em>(<em>p</em>) returns the canonical element
 *      of the set containing <em>p</em>. The <em>find</em> operation
 *      returns the same value for two elements if and only if
 *      they are in the same set.
 *  <li><em>union</em>(<em>p</em>, <em>q</em>) merges the set
 *      containing element <em>p</em> with the set containing
 *      element <em>q</em>. That is, if <em>p</em> and <em>q</em>
 *      are in different sets, replace these two sets
 *      with a new set that is the union of the two.
 *  <li><em>count</em>() returns the number of sets.
 *  </ul>
 *  <p>
 *  The canonical element of a set can change only when the set
 *  itself changes during a call to <em>union</em>&mdash;it cannot
 *  change during a call to either <em>find</em> or <em>count</em>.
 *  <p>
 *  This implementation uses <em>weighted quick union by rank</em>
 *  with <em>path compression by halving</em>.
 *  The constructor takes &Theta;(<em>n</em>) time, where
 *  <em>n</em> is the number of elements.
 *  The <em>union</em> and <em>find</em> operations take
 *  &Theta;(log <em>n</em>) time in the worst case.
 *  The <em>count</em> operation takes &Theta;(1) time.
 *  Moreover, starting from an empty data structure with <em>n</em> sites,
 *  any intermixed sequence of <em>m</em> <em>union</em> and <em>find</em>
 *  operations takes <em>O</em>(<em>m</em> &alpha;(<em>n</em>)) time,
 *  where &alpha;(<em>n</em>) is the inverse of
 *  <a href = "https://en.wikipedia.org/wiki/Ackermann_function#Inverse">Ackermann's function</a>.
 *  <p>
 *  For alternative implementations of the same API, see
 *  {@link QuickUnionUF}, {@link QuickFindUF}, and {@link WeightedQuickUnionUF}.
 *  For additional documentation, see
 *  <a href="https://algs4.cs.princeton.edu/15uf">Section 1.5</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
private class UF {

  private int[] parent;  // parent[i] = parent of i
  private byte[] rank;   // rank[i] = rank of subtree rooted at i (never more than 31)
  private int count;     // number of components

  /**
   * Initializes an empty union-find data structure with
   * {@code n} elements {@code 0} through {@code n-1}.
   * Initially, each elements is in its own set.
   *
   * @param  n the number of elements
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public UF(int n) {
      if (n < 0) throw new IllegalArgumentException();
      count = n;
      parent = new int[n];
      rank = new byte[n];
      for (int i = 0; i < n; i++) {
          parent[i] = i;
          rank[i] = 0;
      }
  }

  /**
   * Returns the canonical element of the set containing element {@code p}.
   *
   * @param  p an element
   * @return the canonical element of the set containing {@code p}
   * @throws IllegalArgumentException unless {@code 0 <= p < n}
   */
  public int find(int p) {
      validate(p);
      while (p != parent[p]) {
          parent[p] = parent[parent[p]];    // path compression by halving
          p = parent[p];
      }
      return p;
  }

  /**
   * Returns the number of sets.
   *
   * @return the number of sets (between {@code 1} and {@code n})
   */
  public int count() {
      return count;
  }

  /**
   * Returns true if the two elements are in the same set.
   *
   * @param  p one element
   * @param  q the other element
   * @return {@code true} if {@code p} and {@code q} are in the same set;
   *         {@code false} otherwise
   * @throws IllegalArgumentException unless
   *         both {@code 0 <= p < n} and {@code 0 <= q < n}
   * @deprecated Replace with two calls to {@link #find(int)}.
   */
  @Deprecated
  public boolean connected(int p, int q) {
      return find(p) == find(q);
  }

  /**
   * Merges the set containing element {@code p} with the 
   * the set containing element {@code q}.
   *
   * @param  p one element
   * @param  q the other element
   * @throws IllegalArgumentException unless
   *         both {@code 0 <= p < n} and {@code 0 <= q < n}
   */
  public void union(int p, int q) {
      int rootP = find(p);
      int rootQ = find(q);
      if (rootP == rootQ) return;

      // make root of smaller rank point to root of larger rank
      if      (rank[rootP] < rank[rootQ]) parent[rootP] = rootQ;
      else if (rank[rootP] > rank[rootQ]) parent[rootQ] = rootP;
      else {
          parent[rootQ] = rootP;
          rank[rootP]++;
      }
      count--;
  }

  // validate that p is a valid index
  private void validate(int p) {
      int n = parent.length;
      if (p < 0 || p >= n) {
          throw new IllegalArgumentException("index " + p + " is not between 0 and " + (n-1));  
      }
  }

}


/**
 *  The {@code Queue} class represents a first-in-first-out (FIFO)
 *  queue of generic items.
 *  It supports the usual <em>enqueue</em> and <em>dequeue</em>
 *  operations, along with methods for peeking at the first item,
 *  testing if the queue is empty, and iterating through
 *  the items in FIFO order.
 *  <p>
 *  This implementation uses a singly linked list with a static nested class for
 *  linked-list nodes. See {@link LinkedQueue} for the version from the
 *  textbook that uses a non-static nested class.
 *  See {@link ResizingArrayQueue} for a version that uses a resizing array.
 *  The <em>enqueue</em>, <em>dequeue</em>, <em>peek</em>, <em>size</em>, and <em>is-empty</em>
 *  operations all take constant time in the worst case.
 *  <p>
 *  For additional documentation, see <a href="https://algs4.cs.princeton.edu/13stacks">Section 1.3</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *
 *  @param <Item> the generic type of an item in this queue
 */

private class QueueMST<Item> implements Iterable<Item> {
  private Node<Item> first;    // beginning of queue
  private Node<Item> last;     // end of queue
  private int n;               // number of elements on queue

  // helper linked list class
  private class Node<Item> {
      private Item item;
      private Node<Item> next;
  }

  /**
   * Initializes an empty queue.
   */
  public QueueMST() {
      first = null;
      last  = null;
      n = 0;
  }

  /**
   * Returns true if this queue is empty.
   *
   * @return {@code true} if this queue is empty; {@code false} otherwise
   */
  public boolean isEmpty() {
      return first == null;
  }

  /**
   * Returns the number of items in this queue.
   *
   * @return the number of items in this queue
   */
  public int size() {
      return n;
  }

  /**
   * Returns the item least recently added to this queue.
   *
   * @return the item least recently added to this queue
   * @throws NoSuchElementException if this queue is empty
   */
  public Item peek() {
      if (isEmpty()) throw new NoSuchElementException("Queue underflow");
      return first.item;
  }

  /**
   * Adds the item to this queue.
   *
   * @param  item the item to add
   */
  public void enqueue(Item item) {
      Node<Item> oldlast = last;
      last = new Node<Item>();
      last.item = item;
      last.next = null;
      if (isEmpty()) first = last;
      else           oldlast.next = last;
      n++;
  }

  /**
   * Removes and returns the item on this queue that was least recently added.
   *
   * @return the item on this queue that was least recently added
   * @throws NoSuchElementException if this queue is empty
   */
  public Item dequeue() {
      if (isEmpty()) throw new NoSuchElementException("Queue underflow");
      Item item = first.item;
      first = first.next;
      n--;
      if (isEmpty()) last = null;   // to avoid loitering
      return item;
  }

  /**
   * Returns a string representation of this queue.
   *
   * @return the sequence of items in FIFO order, separated by spaces
   */
  public String toString() {
      StringBuilder s = new StringBuilder();
      for (Item item : this) {
          s.append(item);
          s.append(' ');
      }
      return s.toString();
  } 

  /**
   * Returns an iterator that iterates over the items in this queue in FIFO order.
   *
   * @return an iterator that iterates over the items in this queue in FIFO order
   */
  public Iterator<Item> iterator()  {
      return new LinkedIterator(first);  
  }

  // an iterator, doesn't implement remove() since it's optional
  private class LinkedIterator implements Iterator<Item> {
      private Node<Item> current;

      public LinkedIterator(Node<Item> first) {
          current = first;
      }

      public boolean hasNext()  { return current != null;                     }
      public void remove()      { throw new UnsupportedOperationException();  }

      public Item next() {
          if (!hasNext()) throw new NoSuchElementException();
          Item item = current.item;
          current = current.next; 
          return item;
      }
  }

}



/**
 *  The {@code IndexMinPQ} class represents an indexed priority queue of generic keys.
 *  It supports the usual <em>insert</em> and <em>delete-the-minimum</em>
 *  operations, along with <em>delete</em> and <em>change-the-key</em> 
 *  methods. In order to let the client refer to keys on the priority queue,
 *  an integer between {@code 0} and {@code maxN - 1}
 *  is associated with each key—the client uses this integer to specify
 *  which key to delete or change.
 *  It also supports methods for peeking at the minimum key,
 *  testing if the priority queue is empty, and iterating through
 *  the keys.
 *  <p>
 *  This implementation uses a binary heap along with an array to associate
 *  keys with integers in the given range.
 *  The <em>insert</em>, <em>delete-the-minimum</em>, <em>delete</em>,
 *  <em>change-key</em>, <em>decrease-key</em>, and <em>increase-key</em>
 *  operations take &Theta;(log <em>n</em>) time in the worst case,
 *  where <em>n</em> is the number of elements in the priority queue.
 *  Construction takes time proportional to the specified capacity.
 *  <p>
 *  For additional documentation, see
 *  <a href="https://algs4.cs.princeton.edu/24pq">Section 2.4</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *
 *  @param <Key> the generic type of key on this priority queue
 */
private class IndexMinPQ<Key extends Comparable<Key>> implements Iterable<Integer> {
  private int maxN;        // maximum number of elements on PQ
  private int n;           // number of elements on PQ
  private int[] pq;        // binary heap using 1-based indexing
  private int[] qp;        // inverse of pq - qp[pq[i]] = pq[qp[i]] = i
  private Key[] keys;      // keys[i] = priority of i

  /**
   * Initializes an empty indexed priority queue with indices between {@code 0}
   * and {@code maxN - 1}.
   * @param  maxN the keys on this priority queue are index from {@code 0}
   *         {@code maxN - 1}
   * @throws IllegalArgumentException if {@code maxN < 0}
   */
  @SuppressWarnings("unchecked")
  public IndexMinPQ(int maxN) {
      if (maxN < 0) throw new IllegalArgumentException();
      this.maxN = maxN;
      n = 0;
      keys = (Key[]) new Comparable[maxN + 1];    // make this of length maxN??
      pq   = new int[maxN + 1];
      qp   = new int[maxN + 1];                   // make this of length maxN??
      for (int i = 0; i <= maxN; i++)
          qp[i] = -1;
  }

  /**
   * Returns true if this priority queue is empty.
   *
   * @return {@code true} if this priority queue is empty;
   *         {@code false} otherwise
   */
  public boolean isEmpty() {
      return n == 0;
  }

  /**
   * Is {@code i} an index on this priority queue?
   *
   * @param  i an index
   * @return {@code true} if {@code i} is an index on this priority queue;
   *         {@code false} otherwise
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   */
  public boolean contains(int i) {
      validateIndex(i);
      return qp[i] != -1;
  }

  /**
   * Returns the number of keys on this priority queue.
   *
   * @return the number of keys on this priority queue
   */
  public int size() {
      return n;
  }

  /**
   * Associates key with index {@code i}.
   *
   * @param  i an index
   * @param  key the key to associate with index {@code i}
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   * @throws IllegalArgumentException if there already is an item associated
   *         with index {@code i}
   */
  public void insert(int i, Key key) {
      validateIndex(i);
      if (contains(i)) throw new IllegalArgumentException("index is already in the priority queue");
      n++;
      qp[i] = n;
      pq[n] = i;
      keys[i] = key;
      swim(n);
  }

  /**
   * Returns an index associated with a minimum key.
   *
   * @return an index associated with a minimum key
   * @throws NoSuchElementException if this priority queue is empty
   */
  public int minIndex() {
      if (n == 0) throw new NoSuchElementException("Priority queue underflow");
      return pq[1];
  }

  /**
   * Returns a minimum key.
   *
   * @return a minimum key
   * @throws NoSuchElementException if this priority queue is empty
   */
  public Key minKey() {
      if (n == 0) throw new NoSuchElementException("Priority queue underflow");
      return keys[pq[1]];
  }

  /**
   * Removes a minimum key and returns its associated index.
   * @return an index associated with a minimum key
   * @throws NoSuchElementException if this priority queue is empty
   */
  public int delMin() {
      if (n == 0) throw new NoSuchElementException("Priority queue underflow");
      int min = pq[1];
      exch(1, n--);
      sink(1);
      assert min == pq[n+1];
      qp[min] = -1;        // delete
      keys[min] = null;    // to help with garbage collection
      pq[n+1] = -1;        // not needed
      return min;
  }

  /**
   * Returns the key associated with index {@code i}.
   *
   * @param  i the index of the key to return
   * @return the key associated with index {@code i}
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   * @throws NoSuchElementException no key is associated with index {@code i}
   */
  public Key keyOf(int i) {
      validateIndex(i);
      if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
      else return keys[i];
  }

  /**
   * Change the key associated with index {@code i} to the specified value.
   *
   * @param  i the index of the key to change
   * @param  key change the key associated with index {@code i} to this key
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   * @throws NoSuchElementException no key is associated with index {@code i}
   */
  public void changeKey(int i, Key key) {
      validateIndex(i);
      if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
      keys[i] = key;
      swim(qp[i]);
      sink(qp[i]);
  }

  /**
   * Change the key associated with index {@code i} to the specified value.
   *
   * @param  i the index of the key to change
   * @param  key change the key associated with index {@code i} to this key
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   * @deprecated Replaced by {@code changeKey(int, Key)}.
   */
  @Deprecated
  public void change(int i, Key key) {
      changeKey(i, key);
  }

  /**
   * Decrease the key associated with index {@code i} to the specified value.
   *
   * @param  i the index of the key to decrease
   * @param  key decrease the key associated with index {@code i} to this key
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   * @throws IllegalArgumentException if {@code key >= keyOf(i)}
   * @throws NoSuchElementException no key is associated with index {@code i}
   */
  public void decreaseKey(int i, Key key) {
      validateIndex(i);
      if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
      if (keys[i].compareTo(key) == 0)
          throw new IllegalArgumentException("Calling decreaseKey() with a key equal to the key in the priority queue");
      if (keys[i].compareTo(key) < 0)
          throw new IllegalArgumentException("Calling decreaseKey() with a key strictly greater than the key in the priority queue");
      keys[i] = key;
      swim(qp[i]);
  }

  /**
   * Increase the key associated with index {@code i} to the specified value.
   *
   * @param  i the index of the key to increase
   * @param  key increase the key associated with index {@code i} to this key
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   * @throws IllegalArgumentException if {@code key <= keyOf(i)}
   * @throws NoSuchElementException no key is associated with index {@code i}
   */
  public void increaseKey(int i, Key key) {
      validateIndex(i);
      if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
      if (keys[i].compareTo(key) == 0)
          throw new IllegalArgumentException("Calling increaseKey() with a key equal to the key in the priority queue");
      if (keys[i].compareTo(key) > 0)
          throw new IllegalArgumentException("Calling increaseKey() with a key strictly less than the key in the priority queue");
      keys[i] = key;
      sink(qp[i]);
  }

  /**
   * Remove the key associated with index {@code i}.
   *
   * @param  i the index of the key to remove
   * @throws IllegalArgumentException unless {@code 0 <= i < maxN}
   * @throws NoSuchElementException no key is associated with index {@code i}
   */
  public void delete(int i) {
      validateIndex(i);
      if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
      int index = qp[i];
      exch(index, n--);
      swim(index);
      sink(index);
      keys[i] = null;
      qp[i] = -1;
  }

  // throw an IllegalArgumentException if i is an invalid index
  private void validateIndex(int i) {
      if (i < 0) throw new IllegalArgumentException("index is negative: " + i);
      if (i >= maxN) throw new IllegalArgumentException("index >= capacity: " + i);
  }

 /***************************************************************************
  * General helper functions.
  ***************************************************************************/
  private boolean greater(int i, int j) {
      return keys[pq[i]].compareTo(keys[pq[j]]) > 0;
  }

  private void exch(int i, int j) {
      int swap = pq[i];
      pq[i] = pq[j];
      pq[j] = swap;
      qp[pq[i]] = i;
      qp[pq[j]] = j;
  }


 /***************************************************************************
  * Heap helper functions.
  ***************************************************************************/
  private void swim(int k) {
      while (k > 1 && greater(k/2, k)) {
          exch(k, k/2);
          k = k/2;
      }
  }

  private void sink(int k) {
      while (2*k <= n) {
          int j = 2*k;
          if (j < n && greater(j, j+1)) j++;
          if (!greater(k, j)) break;
          exch(k, j);
          k = j;
      }
  }


 /***************************************************************************
  * Iterators.
  ***************************************************************************/

  /**
   * Returns an iterator that iterates over the keys on the
   * priority queue in ascending order.
   * The iterator doesn't implement {@code remove()} since it's optional.
   *
   * @return an iterator that iterates over the keys in ascending order
   */
  public Iterator<Integer> iterator() { return new HeapIterator(); }

  private class HeapIterator implements Iterator<Integer> {
      // create a new pq
      private IndexMinPQ<Key> copy;

      // add all elements to copy of heap
      // takes linear time since already in heap order so no keys move
      public HeapIterator() {
          copy = new IndexMinPQ<Key>(pq.length - 1);
          for (int i = 1; i <= n; i++)
              copy.insert(pq[i], keys[pq[i]]);
      }

      public boolean hasNext()  { return !copy.isEmpty();                     }
      public void remove()      { throw new UnsupportedOperationException();  }

      public Integer next() {
          if (!hasNext()) throw new NoSuchElementException();
          return copy.delMin();
      }
    }

  }

} //ENDS MAIN AirlineSystem CLASS