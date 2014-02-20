package pdus;
/*************PDU DOWNLOAD REPLY **************
 * 
 * 	contem a resposta a um comando Download
 * 	que e um artigo(contem o preco mais baixo
 *  a loja onde foi encontrado, o servidor...)
 * 	
 */
import java.util.ArrayList;



public class PDU_DownloadReply implements PDU {
	
	private static final long serialVersionUID = 4873835651971168445L;
	private ArrayList<Artigo> _artigo;
	
	public PDU_DownloadReply(ArrayList<Artigo> result) {
		_artigo = result;
	}
		
	public ArrayList<Artigo> get_artigo() {
		return _artigo;
	}

	public void visit(ClientHandler ch) {
		ch.accept(this);
	}
}
