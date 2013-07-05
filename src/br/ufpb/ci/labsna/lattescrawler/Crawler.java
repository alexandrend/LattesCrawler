package br.ufpb.ci.labsna.lattescrawler;

import java.util.Enumeration;


/**
 * @author Alexandre Nóbrega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 * 
 * Sintaxe para uso:  java LattesCrawler arquivo_de_entrada profundidade 
 * 
 * Onde arquivo_de_entrada contem um ou mais identificadores números de currículos lattes
 * e profundidade é um número inteiro indicando a distância a partir do currículo inicial que deve ser percorrida.
 * 
 * A saída será impressa na saída padrão e representa uma descrição em formato GML de um grafo.
 * 
 */
class Crawler implements Runnable {

	private LattesCrawler lc;
	
	private String lattesID;

	
	public Crawler(LattesCrawler lc, String lattesID) {
		this.lc = lc;
		this.lattesID = lattesID;
	}
	
	public void run(){
		
		try {
			
			//Não queremos sobrecarregar o site do CNPQ. :-)
			Thread.sleep( (long) (Math.random() * 2000) );
			        
			Lattes l = new Lattes(lattesID);
			l.extractData(lc);
			
			lc.addLattes(l);
			
			
			Enumeration <String> con = l.getConnections().keys();
			while( con.hasMoreElements()) {
				String ol = con.nextElement();
				if( !lc.visited(ol)) {
					lc.addSeed(ol);				
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}