package pdus;
/*************PDU UPDATE **************
 * 
 * 	contem a informa�ao de um update
 * 	loja e todos os artigos
 * 
 */
import java.util.ArrayList;



public class PDU_Update implements PDU {
	
	private static final long serialVersionUID = -3468106891828591865L;
	String _loja;
	ArrayList<Artigo> _artigo;

	
	public PDU_Update(String loja, ArrayList<Artigo> artigo) {
		_loja = loja;
		_artigo = artigo;
	}


	public String get_loja() {
		return _loja;
	}


	public ArrayList<Artigo> get_artigo() {
		return _artigo;
	}

	public PDU visit(Database db, int id) {
		return db.accept(this, id);
	}
}
