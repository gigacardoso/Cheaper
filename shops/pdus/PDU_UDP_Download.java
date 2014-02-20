package pdus;
/************** PDU UDP DOWNLOAD *******************
 * 
 * contem info do download, artigo e lista de lojas
 *
 */
import java.util.ArrayList;



public class PDU_UDP_Download extends PDU_UDP {

	private static final long serialVersionUID = 477279068991032829L;
	ArrayList<String> _lojas;
	String _artigo;

	public PDU_UDP_Download(ArrayList<String> lojas, String artigo){
		_lojas = lojas;
		_artigo = artigo;
	}

	public ArrayList<String> get_lojas() {
		return _lojas;
	}

	public String get_artigo() {
		return _artigo;
	}

	public void visit(Database db, PDU_UDP p) {
		db.accept(this, p);
	}
}
