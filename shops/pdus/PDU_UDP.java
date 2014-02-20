package pdus;
/************** PDU UDP ******************* 
 * 
 * contem info pa ser enviada por udp
 * Atributos:
 * 		_hostname - hostname de servidor que vai receber 
 * 		_port - porto do servidor que vai receber 
 * 		_id - id da server thread que enviou
 * 		_from - Nome server que enviou
 * 		_tipo - Reply ou Rquest
 * 		_comando - Download ou Synch
 * 		_pdu - info a ser enviada
 */

import java.io.Serializable;




public class PDU_UDP implements Serializable {

	private static final long serialVersionUID = 5627567566594975600L;
	private String _hostname;
	private int _port;
	private int _id;
	private String _tipo;
	private String _comando;
	private String _from;
	private PDU_UDP _pdu;

	
	public PDU_UDP(String hostname, int port, int id, String from, String tipo,
			String comando, PDU_UDP pdu) {
		_hostname = hostname;
		_port = port;
		_id = id;
		_from = from;
		_tipo = tipo;
		_comando = comando;
		_pdu = pdu;
	}

	
	public PDU_UDP() {
	}


	public String get_tipo() {
		return _tipo;
	}


	public String get_comando() {
		return _comando;
	}


	public PDU_UDP get_pdu() {
		return _pdu;
	}


	public int get_id() {
		return _id;
	}

	public String get_from() {
		return _from;
	}


	public String get_hostname() {
		return _hostname;
	}

	public int get_port() {
		return _port;
	}

	public void visit(Database db){
		db.accept(this);
	}


	public void visit(Database database, PDU_UDP p) {
	}

}
