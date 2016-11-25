import java.io.*;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

class ContactPlanCreator {
  public static void main(String args[])
  throws IOException {
	  
	//final String defaultPath = "";
    FileReader fr;
    String inputFilePath, outputPath;
    int datarate;
    BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("Insert the full path of the input file for the Contact Plan Creator\n");
    inputFilePath = br2.readLine();
    //System.out.println("Insert the datarate for the Contact Plan, in B/s\n");
    //datarate = Integer.parseInt(br2.readLine());
    fr = new FileReader(inputFilePath);
	BufferedReader br = new BufferedReader(fr);
	
    SortedSet<ContactPlanLine> contactPlan = new TreeSet<>();
    ContactPlanLine riga;
    String fileLine;
    while((fileLine = br.readLine()) != null)
    {
    	int node1, node2; 
    	int start;
    	int stop;
    	StringTokenizer tokenizer = new StringTokenizer(fileLine);
    	
    	while(tokenizer.hasMoreTokens()){
    		start =(int)(Math.floor(Double.parseDouble(tokenizer.nextToken())));
    		if(tokenizer.nextToken().equalsIgnoreCase("CONN")){
    			String temp1, temp2;
    			temp1 = tokenizer.nextToken().substring(1);
    			temp2 = tokenizer.nextToken().substring(1);
    			node1 = Integer.parseInt(temp1);
    			node2 = Integer.parseInt(temp2);
    			if(tokenizer.nextToken().equalsIgnoreCase("UP")){
    				//SP mod
    				datarate = Integer.parseInt(tokenizer.nextToken());
    				stop = 0;
    				riga = new ContactPlanLine(start, stop, node1, node2, datarate);
    				contactPlan.add(riga);
    			}
    			else{
    				for (ContactPlanLine cpl : contactPlan){
    					if(cpl.connectionUp(node1, node2)){
    						cpl.setStop((int)Math.floor(start));
    						break;
    					}
    				}
    			}
    		}
    		else 
    			break;
    		    		
    	}
    	
    }
    br.close();
    fr.close();
    System.out.println("Insert the output contact plan path");
    outputPath = br2.readLine();
    PrintWriter pw = new PrintWriter(outputPath);
	BufferedWriter bw = new BufferedWriter(pw);
    for(ContactPlanLine c : contactPlan){
    	if(c.getStop() != 0){
    	bw.write(c.toStringRange());
    	bw.newLine();
    	bw.write(c.toString());
    	bw.newLine();
    	bw.write(c.toStringTwoWays());
    	bw.newLine();
    	}
    }
    bw.close();
    pw.close();
    
  }
}