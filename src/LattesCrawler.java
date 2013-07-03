import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


/**
 * @author Alexandre N—brega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 */
class Crawler implements Runnable {

	private LattesCrawler lc;
	
	private String lattesID;
	
	private Set <Lattes>lattes;

	
	public Crawler(LattesCrawler lc, String lattesID, Set <Lattes> lattes) {
		this.lc = lc;
		this.lattesID = lattesID;
		this.lattes = lattes;
	}
	
	
	public void run(){
		
		try {
			
			URL cvlattes = new URL("http://lattes.cnpq.br/" + lattesID);

			BufferedReader in = new BufferedReader( new InputStreamReader(cvlattes.openStream()));
  
			String inputLine;
        
			Lattes l = new Lattes(lattesID);
        
			while ((inputLine = in.readLine()) != null) {
    
				if( inputLine.contains("\"nome\"")) {
				
					l.setName( inputLine.substring(inputLine.indexOf(">") + 1, inputLine.lastIndexOf("<")).trim());	
				
				} else if( inputLine.contains("class=\"texto\">Bolsista de Produtividade em Pesquisa")){
				
					l.setPQ(inputLine.substring(inputLine.lastIndexOf("l")+1, inputLine.lastIndexOf("<")).trim());			
			
				} else if( inputLine.contains("class=\"texto\">Bolsista de Produtividade Desen.")){
			
					l.setDT(inputLine.substring(inputLine.lastIndexOf("l")+1, inputLine.lastIndexOf("<")).trim());
				}
				
				else {
        	
					StringTokenizer st = new StringTokenizer(inputLine);
			
					while( st.hasMoreTokens() ) {
						String t = st.nextToken();
			
						if( t.contains("http://lattes.cnpq.br")) {
					
							StringBuffer id = new StringBuffer();
					
							for( int i = 0; i < t.length(); i++ )
								if( Character.isDigit(t.charAt(i)))
									id.append(t.charAt(i));

							if( id.length() == 16 ) {							
								l.addConnection(id.toString());
							
								if(!lc.visited(id.toString()))
									lc.addSeed(id.toString());
							}
						}
					}	
				}
			}
            
			in.close();
        
			lattes.add(l);
        
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}

/**
 * @author Alexandre N—brega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 */
public class LattesCrawler {
	
	private Map<String,Integer> visited;
	
	private List<String> seeds;
	
	public LattesCrawler() {
		
		visited = (Map<String,Integer>) Collections.synchronizedMap(new HashMap<String,Integer>());
		seeds = (List<String>) Collections.synchronizedList(new LinkedList<String>());
		
	}
	
	public boolean visited(String id) {
		return visited.get(id)!=null;
	}
		
	public Set<Lattes> crawl(int maxNivel ) throws IOException {
		
		Set <Lattes>s = Collections.synchronizedSet(new HashSet<Lattes>());
		
		ExecutorService pool = Executors.newFixedThreadPool(200);
		int nivel = 0;
		
		while( !seeds.isEmpty() && nivel < maxNivel) {
			String next = seeds.remove(0);
			
			if(next.equals("@")) { 
				
				System.err.println("AGUARDANDO FIM DO NIVEL " + nivel);
				pool.shutdown();
				
				while(!pool.isTerminated() ) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.err.println( "FIM DO NIVEL " + nivel);
				System.err.println("SEEDS " + seeds.size());
				
				nivel++;
				seeds.add("@");
				pool = Executors.newFixedThreadPool(200);
	
			} else {
				pool.execute(new Crawler(this, next, s));
			}
		}
		
		pool.shutdown();
		
		while(!pool.isTerminated() ) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return s;
		
	}
	
	public void addSeed(String seed) {
		visited.put(seed, new Integer(0));
		seeds.add(seed);
	}
	
	public static void main(String args[]) throws IOException{ 

			LattesCrawler crawler = new LattesCrawler();
			
			BufferedReader r = new BufferedReader(new FileReader(args[0]));
			String line;
			
			while( (line = r.readLine())!= null)
				crawler.addSeed(line.trim());
			
			r.close();
			
		    crawler.addSeed("@"); //Marca de fim de n’vel
		    
		    
		    Set<Lattes> s = crawler.crawl(Integer.parseInt(args[1]));
			
			
			System.out.println( "graph");
			System.out.println( "[");
			System.out.println("\tdirected 0");
			
			
			Iterator <Lattes>it = s.iterator();
			while( it.hasNext() ) {
				
				Lattes l = (Lattes) it.next();
				
				System.out.println( "\tnode");
				System.out.println( "\t[");
				System.out.println( "\t\tid " + l.getLattesID());
				System.out.println( "\t\tlabel " + "\"" + l.getName() + "\"");
				
				if(l.getNivel() != null )
					System.out.println( "\t\tnivel " + "\"" + l.getNivel() + "\"");
				
				//System.out.println( "\t\tinst " +  "\"" + instituicao + "\"" );
				//System.out.println( "\t\tarea " +  "\"" + area + "\"");
				//System.out.println( "\t\tgrandearea " + "\"" + grandeArea + "\"");
				System.out.println( "\t]");
				
			}
			
			it = s.iterator();
			while( it.hasNext()) {
				
				Lattes l = (Lattes) it.next();
				Dictionary <String,Integer> d = l.getConnections();
				
				Enumeration<String> conn = d.keys();
				Enumeration <Integer> w = d.elements();
				
				while(conn.hasMoreElements()) {
					String dest = conn.nextElement();
					int p = w.nextElement().intValue();
					
					System.out.println( "\tedge");
					System.out.println( "\t[");
					System.out.println( "\t\t source " + l.getLattesID());
					System.out.println( "\t\t target " + dest);
					System.out.println( "\t\tvalue " + p);
					System.out.println( "\t]");
			
				}
			}
			
			System.out.println("]");
	}


}
