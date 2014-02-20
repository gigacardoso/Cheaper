package pdus;
/************** PDU UDP DOWNLOAD REPLY *******************
 * 
 * Resposta a um pedido udp de Download
 * tem a informa�ao do artigo, ou -1 no preco 
 * se nao tinha informa�ao
 */
import java.util.TreeMap;



public class PDU_UDP_DownloadReply extends PDU_UDP {
	
	private static final long serialVersionUID = 2720159001292747468L;
	TreeMap<String, Artigo> _precos;

	public PDU_UDP_DownloadReply(TreeMap<String, Artigo> precos){
		_precos = precos;
	}

	public TreeMap<String, Artigo> get_precos() {
		return _precos;
	}

	public void visit(Database db) {
		db.accept(this);
	}
}
