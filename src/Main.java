import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * Main Class - GHS Simulator
 *
 */
public class Main {

	private final String SEPARATOR = " "; 
	private int noofnodes;
	private int distanceMatrix[][];
	private MessageBus messageBus = new MessageBus();
	private List<Node> nodeList;
	private List<Thread> threadList;
	public static volatile boolean completed = false;
	
	
	public static void main(String[] args) {
		Main simulator = new Main();
		//read and process input from input file
		simulator.processInput();
		simulator.printInfo();
		simulator.init();
		simulator.loop();
		simulator.processOutput();
		simulator.printStats();
		
		//shutdown the simulator
		simulator.shutdown();
	}
	
	public void processInput(){
		FileReader inputFileReader;
		try {
			inputFileReader = new FileReader("dat/input.txt");
			BufferedReader br = new BufferedReader(inputFileReader);
			noofnodes = Integer.parseInt(br.readLine());
			distanceMatrix = new int[noofnodes][noofnodes];
			
			for(int i=0 ; i<noofnodes ; i++){
				String inputLine = br.readLine();
				String ints[] = inputLine.split(SEPARATOR);
				for(int j=0 ; j < noofnodes ; j++){
					distanceMatrix[i][j] = Integer.parseInt(ints[j]);
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Not able to read from file."+e.getMessage());
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("Not able to read from file."+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Not able to read from file."+e.getMessage());
			e.printStackTrace();
		}		
		
	}
	
	public void processOutput(){
		
		FileWriter outputFileWriter;
		String outPath = "dat/output.txt";
		try {
			int bitmap[][] = new int[noofnodes][noofnodes];
			File outputFile = new File(outPath);
			if(outputFile.exists()){
				outputFile = new File(outPath+System.currentTimeMillis());
			}
			outputFileWriter = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(outputFileWriter);
			
			for(int i=0;i<nodeList.size();i++){
				Node node = nodeList.get(i);
				for(Edge edge:node.getAdjsEdges()){
					if(edge.getStatus() == Status.BRANCH && bitmap[edge.getVI()][edge.getUI()]!=1){
						System.out.println(edge);
						bw.write((edge.getUI()+1)+" --> "+(edge.getVI()+1)+" with weight w = "+edge.getW()+"\n");
						bitmap[edge.getUI()][edge.getVI()] = 1;
					}
				}
			}

			bw.close();
			
		} catch (IOException e) {
			System.out.println("Not able to read from file."+e.getMessage());
			e.printStackTrace();
		}		
		
	}
	
	
	public void init(){
		
		nodeList = new ArrayList<Node>();
		threadList = new ArrayList<Thread>();
		for(int i=0;i<noofnodes;i++){
			Node node = new Node(i,messageBus);
			//initially each node belongs to a single fragment
			Fragment frag = new Fragment();
			frag.setRoot(node);
			frag.setLevel(0);
			frag.setName(i);
                        node.setNoofnodes(noofnodes);
			node.setDistanceMatrix(distanceMatrix[i]);
			node.setFragment(frag);
			Thread nodeThread = new Thread(node);
			nodeList.add(node);
			threadList.add(nodeThread);
			//nodeThread.start();
		}
                ListIterator<Node> litr = nodeList.listIterator();
                while(litr.hasNext()) 
                {
                    Node node = litr.next();
                    node.initAdsEdges();
                }
                
                ListIterator<Thread> itr = threadList.listIterator();
                while(itr.hasNext()) 
                {
                    Thread nodeThread = itr.next();
                    nodeThread.start();
                } 
	}
	
	public void loop(){
		while(!completed);
		System.out.println("MST computation complete.");
	}
	
	public void shutdown(){
		System.exit(1);
	}
	
	public void printStats(){

		for(int i=0;i<noofnodes;i++){
			System.out.println(nodeList.get(i));
		}
		for(int i=0;i<noofnodes;i++){
			nodeList.get(i).nodeStats();
		}
		messageBus.printMessageBus();
	}
	
	public void printInfo(){
		
		for(int i = 0; i< noofnodes;i++){
			for(int j=0;j<noofnodes;j++){
				System.out.print(distanceMatrix[i][j]);
                                System.out.print("\t");
			}
                        System.out.println();
		}
		
	}
	
}
