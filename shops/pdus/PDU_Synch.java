package pdus;


/*************PDU SYNCH  **************
 * 
 * 	contem a informa�ap de um comando Synch
 * 	
 */
public class PDU_Synch implements PDU {

	private static final long serialVersionUID = 5160625973084816685L;
	String _loja;
	String _artigo;
	String _ficheiro;
	int _precoAntigo;
	int _precoNovo;

	public PDU_Synch(String loja, String artigo, String ficheiro,
			int precoAntigo, int precoNovo) {
		_loja = loja;
		_artigo = artigo;
		_ficheiro = ficheiro;
		_precoAntigo = precoAntigo;
		_precoNovo = precoNovo;
	}
	
	public String get_loja() {
		return _loja;
	}

	public String get_artigo() {
		return _artigo;
	}

	public String get_ficheiro() {
		return _ficheiro;
	}

	public int get_precoAntigo() {
		return _precoAntigo;
	}

	public int get_precoNovo() {
		return _precoNovo;
	}

	public PDU visit(Database db, int id) {
		return db.accept(this, id);
	}
}
