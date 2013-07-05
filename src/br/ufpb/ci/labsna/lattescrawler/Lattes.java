package br.ufpb.ci.labsna.lattescrawler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Alexandre N—brega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 */
public class Lattes {

	//Utilizado para tratar lattes de h™monimos, o que pode gerar problemas com o processamento do grafo.
	private static Map<String,Integer>cvNames = Collections.synchronizedMap(new HashMap<String,Integer> ()); 
	
	
	private String lattesID;
	private Dictionary <String,Integer>connections;
	private String name;
	private String nivel;

	public Lattes(String lattesID) {
		this.lattesID = lattesID;
		connections = new Hashtable<String,Integer>();	
	}

	public void addConnection(String otherLattesID) {

		Integer i = (Integer)connections.get(otherLattesID);
		if( i == null ) i = new Integer(0);
		
		i = i + 1;		
		connections.put(otherLattesID,i);
	}

	public void setName(String name) {
		
		Integer i = Lattes.cvNames.get(name);
		
		if( i == null ) {
			this.name = name;
			Lattes.cvNames.put(name, new Integer(1));
		} else {
			this.name = name + " (" + i + ")";
			i = i + 1;
			Lattes.cvNames.put(name, i);
		}
			
	}
	
	public String getName(){
		return name;
	}
	
	public String getLattesID() {
		return lattesID;
	}
	
	public Dictionary <String,Integer> getConnections() {
		return connections;
	}

	public void setPQ(String nivelPQ) {
		setNivel("PQ-"+nivelPQ);
	}

	public void setDT(String nivelDT) {
		setNivel( "DT-"+nivelDT);
		
	}
	
	public void setNivel(String nivel) {
		this.nivel = nivel;
	}
	
	public String getNivel(){
		return this.nivel;
	}

	public void extractData(LattesCrawler lc) throws Exception {
		
		URL cvlattes = new URL("http://lattes.cnpq.br/" + lattesID);

		//System.err.println(cvlattes);
		
		BufferedReader in = new BufferedReader( new InputStreamReader(cvlattes.openStream()));

		String inputLine;		
    
		while ((inputLine = in.readLine()) != null) {
			
			if( inputLine.contains("\"nome\"")) {
				
				setName( inputLine.substring(inputLine.indexOf(">") + 1, inputLine.lastIndexOf("<")).trim());	
				
			} else if( inputLine.contains("class=\"texto\">Bolsista de Produtividade em Pesquisa")){
			
				setPQ(inputLine.substring(inputLine.lastIndexOf("l")+1, inputLine.lastIndexOf("<")).trim());			
		
			} else if( inputLine.contains("class=\"texto\">Bolsista de Produtividade Desen.")){
		
				setDT(inputLine.substring(inputLine.lastIndexOf("l")+1, inputLine.lastIndexOf("<")).trim());
			}
			
			//Procurar por referncias para outros curriculos
			else {
		
				StringTokenizer st = new StringTokenizer(inputLine);
		
				while( st.hasMoreTokens() ) {
					String t = st.nextToken();
		
					if( t.contains("http://lattes.cnpq.br")) {
				
						StringBuffer id = new StringBuffer();
				
						for( int i = 0; i < t.length(); i++ )
							if( Character.isDigit(t.charAt(i)))
								id.append(t.charAt(i));

						if( id.length() == 16 && !id.equals(lattesID) ) {							
							addConnection(id.toString());
						
						}
						
					}
				}	
			}
		
		}
	
	}
	
	
	public boolean equals (Lattes o) {
		return o.getLattesID().equals(getLattesID());
	}
	
}
