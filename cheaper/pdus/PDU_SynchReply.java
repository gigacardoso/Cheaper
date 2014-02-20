package pdus;
/*************PDU SYNCH REPLY **************
 * 
 * 	contem a a resposta a um comando Synch
 * 	  
 *  Atributos:
 *  	_user - null se nao foi descoberto
 *  	_servers - informa�ao dos servidores que 
 *  				podem recebr o ficheiro
 *  	_file - ficheiro a ser enviado
 * 
 */
import java.util.ArrayList;



public class PDU_SynchReply implements PDU {
	
	
	private static final long serialVersionUID = -6031816948027753242L;
	String _user;
	ArrayList<Server> _servers;
	FileArtigo _file;
	
	public PDU_SynchReply(String user,ArrayList<Server> servers, FileArtigo file) {
		_user = user;
		_servers = servers;
		_file = file;
	}

	public FileArtigo get_file() {
		return _file;
	}

	public ArrayList<Server> get_servers() {
		return _servers;
	}

	public String get_user() {
		return _user;
	}

	public void visit(ClientHandler ch) {
		ch.accept(this);
	}
}
