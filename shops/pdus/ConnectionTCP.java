package pdus;
/*********** CLASSE CONNECTION TCP *****************
 * 
 * classe que representa uma coneccao tcp
 *  faz o envio e a recepcao de todas as pdus
 *  trata do envio e da recepcao de ficheiros
 *   
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;



public class ConnectionTCP {
	
	private Socket _socket;
	private ObjectInputStream _in =  null;
	private ObjectOutputStream _out = null;
		
	public ConnectionTCP(Socket socket) throws IOException{
			_socket = socket;
			_out = new ObjectOutputStream(_socket.getOutputStream());
			_in = new ObjectInputStream(_socket.getInputStream());
	}
	
	public void send(PDU pdu) throws IOException {
		_out.writeObject(pdu);
		_out.flush();
	}
	
	public PDU recv() throws IOException, ClassNotFoundException{
		return (PDU) _in.readObject();
	}
	
	public void SendFile(FileArtigo file,String server) throws FileNotFoundException{
		File f = new File(server+file.get_loja()+file.get_artigo()+file.get_file());
		try {
			InputStream is = new FileInputStream(f);
			int length = (int) f.length();
			int offsetAnterior= 0;
			int offset = 0;
			int numRead = 0;
			int tamanho = 1000;
			byte[] bytes;
			while( offsetAnterior <  length || offsetAnterior == 0){
				if((length-offsetAnterior) < tamanho){
					bytes = new byte[(length - offsetAnterior)];
				}
				else{
					bytes = new byte[tamanho];
				}

				while (offset < bytes.length  && (numRead=is.read(bytes)) >= 0) {
					offset += numRead;
				}
				_out.writeObject(new FileObject(bytes,numRead, length));
				_out.flush();
				bytes = null;
				offsetAnterior += numRead;
				numRead = 0;
				offset = 0;
			}
			is.close();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void ReceiveFile(FileArtigo file,String server) {
		try {
			File f = new File(server+file.get_loja()+file.get_artigo()+file.get_file());
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			FileOutputStream os = new FileOutputStream(f);
			FileObject fromClient;
			int fileLength;
			int dataEscrita = 0;
			byte[] data;
			while ((fromClient = (FileObject) _in.readObject()) != null){
				data = fromClient.get_data();
				fileLength = fromClient.get_fileLength();
				int datalen = fromClient.get_size();
				os.write(data);
				dataEscrita += datalen;
				if(dataEscrita >= fileLength){
					break;
				}				
			}
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			_in.close();
			_out.close();
			_socket.close();
		} catch (IOException ioe) {
			System.out.println("Erro no close");
		}
	}

	public void reset() {
		try {
			_out.reset();
		} catch (IOException e) {
			//Do nothing
		}
	}
}
