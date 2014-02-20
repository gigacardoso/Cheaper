package pdus;
/*********** CLASSE FILEOBJECT *****************
 * 
 * objecto que vai ser enviado pelo socket com a data
 * de um ficheiro, ou parte dela(1024)
 * 
 */

import java.io.Serializable;

public class FileObject implements Serializable {
	
	private static final long serialVersionUID = 312928353523434729L;
	private byte[] _data;
	private int _fileLength;
	private int _size;
	
	public FileObject(byte[] data,int size, int len) {
		_data = data;
		_size = size;
		_fileLength = len;
	}

	public int get_size() {
		return _size;
	}

	public byte[] get_data() {
		return _data;
	}

	public int get_fileLength() {
		return _fileLength;
	}
	
	
}
