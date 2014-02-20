package pdus;
/*********** CLASSE FILEARTIGO *****************
 * 
 * classe que representa um ficheiro de um artigo
 * tem a informa�ao de que loja e artigo e que esta associado
 * 
 */

import java.io.Serializable;

public class FileArtigo implements Serializable {
	
	private static final long serialVersionUID = 558243746302837613L;
	private String _loja;
	private String _artigo;
	private String _file;
	
	public FileArtigo(String loja, String artigo, String file) {
		_loja = loja;
		_artigo = artigo;
		_file = file;
	}
	public String get_loja() {
		return _loja;
	}
	public String get_artigo() {
		return _artigo;
	}
	public String get_file() {
		return _file;
	}

}
