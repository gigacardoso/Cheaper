package pdus;
/************* PDU GETFILES ******************
 * 
 * contem o pedido de um(varios) ficheiro(s)  
 * 
 */
import java.util.ArrayList;



public class PDU_GetFiles implements PDU {

	private static final long serialVersionUID = 3563715167258471618L;
	ArrayList<FileArtigo> _files;
	
	public PDU_GetFiles(ArrayList<FileArtigo> files) {
		_files = files;
	}

	public ArrayList<FileArtigo> get_files() {
		return _files;
	}

	public void visit(ClientHandler ch) {
		ch.accept(this);
	}
}
