package br.ufpb.ci.labsna.lattescrawler;
import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Alexandre Nóbrega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 */
public class Lattes {

	//Utilizado para tratar lattes de homonimos, o que pode gerar problemas com o processamento do grafo.
	private static Map<String,Integer>cvNames = Collections.synchronizedMap(new HashMap<String,Integer> ()); 
	
	
	private String lattesID;
	private Dictionary <String,Integer>connections;
	private String name;
	private String nivel;
	private int artigosP;

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

	public void extractData(LattesCrawler lc) throws IOException, LattesNotFoundException  {
			
			Document doc = Jsoup.connect("http://lattes.cnpq.br/" + lattesID).timeout(0).get();			
			
			String title[] = doc.select(".nome").text().split("Bolsista");
			
			if( title[0].length() < 2) {
					//System.err.println( doc.text());
					throw new LattesNotFoundException( "http://lattes.cnpq.br/" + lattesID);
			}
			
			setName(title[0]);
			this.nivel = null;

			if( title.length > 1 )
				this.nivel =  "Bolsista " + title[1];

			Elements links = doc.select("a[href]");		
			for (Element link : links) {
			    String l = link.attr("abs:href");
			    if( l.startsWith("http://lattes.cnpq.br") && !l.endsWith(lattesID) && l.substring(22).length() == 16) {
			    	addConnection( l.substring(22));
			    }
			}
			
			//setArtigosP(doc.select( ".artigo-completo").size());
	
	}
	
	
	public boolean equals (Lattes o) {
		return o.getLattesID().equals(getLattesID());
	}

	public int getArtigosP() {
		return artigosP;
	}

	public void setArtigosP(int artigos_periodico) {
		this.artigosP = artigos_periodico;
	}
	
}
