import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Alexandre N—brega Duarte - alexandre@ci.ufpb.br - http://alexandrend.com
 */
public class Lattes {

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
		this.name = name;
		System.err.println( name);
		
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
		System.err.println(nivel);
	}
	
	public String getNivel(){
		return this.nivel;
	}


	
}
