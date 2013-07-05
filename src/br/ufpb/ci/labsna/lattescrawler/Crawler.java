package br.ufpb.ci.labsna.lattescrawler;

import java.util.Enumeration;


/**
 * @author Alexandre N�brega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 * 
 * Sintaxe para uso:  java LattesCrawler arquivo_de_entrada profundidade 
 * 
 * Onde arquivo_de_entrada contem um ou mais identificadores n�meros de curr�culos lattes
 * e profundidade � um n�mero inteiro indicando a dist�ncia a partir do curr�culo inicial que deve ser percorrida.
 * 
 * A sa�da ser� impressa na sa�da padr�o e representa uma descri��o em formato GML de um grafo.
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
			
			//N�o queremos sobrecarregar o site do CNPQ. :-)
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