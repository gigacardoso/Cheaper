package pdus;


/************* PDU SENDFILE ******************
 * 
 * contem a informa�ao de um ficheiro para ser enviado 
 * 
 */
public class PDU_SendFile implements PDU {

	private static final long serialVersionUID = -7448588436941615129L;
	private FileArtigo _file;
	
	public PDU_SendFile(FileArtigo file) {
		_file = file;
	}

	public FileArtigo get_file() {
		return _file;
	}

	public PDU visit(Database db, int id) {
		return db.accept(this, id);
	}
}
