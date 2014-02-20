package pdus;


/************** PDU UDP SYNCH *******************
 * 
 * contem info do synch a ser enviada aos outros servers
 * assim como o user que o ta a realizar
 *
 */
public class PDU_UDP_Synch extends PDU_UDP {
	
	private static final long serialVersionUID = -7534308043325570627L;
	private String _user;
	private String _loja;
	private String _artigo;
	private String _ficheiro;
	private int _precoAntigo;
	private int _precoNovo;
	
	public PDU_UDP_Synch(String user, String loja, String artigo, String ficheiro, int precoAntigo, int precoNovo) {
		_user = user;
		_loja = loja;
		_artigo = artigo;
		_ficheiro = ficheiro;
		_precoAntigo = precoAntigo;
		_precoNovo = precoNovo;
	}

	public String get_user() {
		return _user;
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
	public void visit(Database db, PDU_UDP p) {
		db.accept(this, p);
	}
}
