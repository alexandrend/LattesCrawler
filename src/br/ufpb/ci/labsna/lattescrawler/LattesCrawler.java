package br.ufpb.ci.labsna.lattescrawler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Alexandre Nóbrega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 */
public class LattesCrawler {
	
	private Map<String,Integer> visited;
	
	private List<String> seeds;
	
	private Map <String,Lattes> cvs;
	
	public LattesCrawler() {
		
		visited = (Map<String,Integer>) Collections.synchronizedMap(new HashMap<String,Integer>());
		seeds = (List<String>) Collections.synchronizedList(new LinkedList<String>());
		cvs = (Map<String,Lattes>) Collections.synchronizedMap(new HashMap<String,Lattes>());
		
	}
	
	//Curriculos já recuperados.
	public void addLattes(Lattes l) {
		cvs.put(l.getLattesID(),l);
	}

	public void setVisited(String lattesID) {
		visited.put(lattesID, new Integer(0));
	}

	public boolean visited(String id) {
		return visited.get(id)!=null;
	}
		
	public Map<String,Lattes> crawl(int maxNivel) throws IOException {
		
		ExecutorService pool = Executors.newFixedThreadPool(200);
		
		int nivel = 0;
		
		while( !seeds.isEmpty() && nivel <= maxNivel) {
			
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
				pool.execute(new Crawler(this, next));
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
		return cvs;
		
	}
	
	public void addSeed(String seed) {
		seeds.add(seed);
		setVisited(seed);
	}
	
	public static void main(String args[]) throws IOException{ 

			Set <String> seed = new HashSet<String>();
		
			LattesCrawler crawler = new LattesCrawler();
			
			//System.err.println( "FILE " + args[0]);
			
			BufferedReader r = new BufferedReader(new FileReader(args[0]));
			String line;
			
			while( (line = r.readLine())!= null) {
				crawler.addSeed(line.trim());
				
				seed.add(line.trim());
				
			}
			
			r.close();
			
		    crawler.addSeed("@"); //Marca de fim de nível
		    
		    
		    Map<String,Lattes> lattes = crawler.crawl(Integer.parseInt(args[1]));
					 
		    
			System.out.println( "graph");
			System.out.println( "[");
			System.out.println("\tdirected 0");
			
			
			//Imprimir a definição dos nós
			Iterator <String>it = lattes.keySet().iterator();
			while( it.hasNext() ) {
				
				Lattes l = lattes.get(it.next());
				
				if(l.getName() != null ) {
				
					System.out.println( "\tnode");
					System.out.println( "\t[");
					System.out.println( "\t\t id " + l.getLattesID());
					System.out.println( "\t\t label " + "\"" + l.getName() + "\"");
				
					if(l.getNivel() != null )
						System.out.println( "\t\t bolsa " + "\"" + l.getNivel() + "\"");
					
					//System.out.println( "\t\t periodicos " + l.getArtigosP());
					
					if(seed.contains(l.getLattesID()))
						System.out.println( "\t\t seed 1");
					else	
						System.out.println( "\t\t seed 0");
					
						
					
				
					System.out.println( "\t]");
				}
				
			}
			
			//Imprimir a definição dos arcos
			it = lattes.keySet().iterator();
			
			while( it.hasNext()) {
				
				Lattes l = lattes.get(it.next());
				Dictionary <String,Integer> d = l.getConnections();
				
				Enumeration<String> conn = d.keys();
				
				while(conn.hasMoreElements()) {
					Lattes dest = lattes.get(conn.nextElement());
					
					if( dest != null && !dest.getLattesID().equals(l.getLattesID()) && dest.getName()!=null) {
						
						int p = d.get(dest.getLattesID()).intValue();
						
						System.out.println( "\tedge");
						System.out.println( "\t[");
						System.out.println( "\t\t source " + l.getLattesID());
						System.out.println( "\t\t target " + dest.getLattesID());
						System.out.println( "\t\t value " + p);
						System.out.println( "\t]");
					}
			
				}
			}	
			System.out.println("]");
	}

}
