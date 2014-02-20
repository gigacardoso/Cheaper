package pdus;
/*************PDU DOWNLOAD **************
 * 
 * 	Contem a informa��o no comando Download
 * So sao enviados 3 artigos de cada vez.
 * 
 */
import java.util.ArrayList;




public class PDU_Download implements PDU {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4137172701635240437L;
	ArrayList<String> _lojas;
	ArrayList<String> _artigo;
	
	
	
	public PDU_Download(ArrayList<String> lojas, ArrayList<String> artigosToSend) {
		super();
		_lojas = lojas;
		_artigo = artigosToSend;
	}
	

	public ArrayList<String> get_lojas() {
		return _lojas;
	}


	public ArrayList<String> get_artigo() {
		return _artigo;
	}

	public void visit(ClientHandler ch) {
	}
}
