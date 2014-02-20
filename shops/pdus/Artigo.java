package pdus;
/*********** CLASSE ARTIGO *****************
 * 
 * classe que representa um artigo
 * 
 */
import java.io.Serializable;

public class Artigo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6086988627182654489L;
	private String _artigo; //nome do artigo
	private String _ficheiro; //nome do ficheiro
	private int _preco; 
	private String _servidor; //servidor em que esta guardado(null se enviado por cliente)
	private String _loja; // loja do artigo
	private String _user; // o username de quem registou/esta a registar o produto 
	
	public Artigo(String artigo, String ficheiro, int preco) {
		_artigo = artigo;
		_ficheiro = ficheiro;
		_preco = preco;
	}
	
	public String get_user() {
		return _user;
	}
	
	public String get_artigo() {
		return _artigo;
	}

	public String get_ficheiro() {
		return _ficheiro;
	}

	public int get_preco() {
		return _preco;
	}

	public String get_loja() {
		return _loja;
	}

	public String get_servidor() {
		return _servidor;
	}

	public void set_user(String user) {
		_user = user;
	}

	public void set_ficheiro(String ficheiro) {
		_ficheiro = ficheiro;
	}
	
	public void set_loja(String loja) {
		_loja = loja;
	}

	public void set_server(String server) {
		_servidor = server;
	}

	public void set_preco(int preco) {
		_preco = preco;
	}
	
}
