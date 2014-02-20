package pdus;
/*********** CLASSE SERVER *****************
 * 
 * classe que representa um server
 * tem o seu nome, hostname, port e pode ter
 * a lista de lojas a que esta associado.
 *  
 */

import java.io.Serializable;
import java.util.ArrayList;

public class Server implements Serializable{
	
	private static final long serialVersionUID = 6231133773670740081L;
	private String _name;
	private String _hostname;
	private int _port;
	private ArrayList<String> _lojas = null;
	
	public Server(String name, String hostname, int port) {
		_name = name;
		_hostname = hostname;
		_port = port;
	}
		
	public String get_name() {
		return _name;
	}

	public String get_hostname() {
		return _hostname;
	}
	public int get_port() {
		return _port;
	}

	public ArrayList<String> get_lojas() {
		return _lojas;
	}

	public void set_lojas(ArrayList<String> lojas) {
		_lojas = lojas;
	}
	
}
