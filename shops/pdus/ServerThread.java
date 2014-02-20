package pdus;
/************** CLASSE SERVER THREAD ********************
 * 
 * thread criada por cada cliente que cria ligacao.
 * recebe as pdus e comunica com a base de dados para as tratar.
 * 
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;


public class ServerThread extends Thread {

	private ConnectionTCP _tcp = null;
	private ConnectionUDPServer _udp = null;
	private Database _db;
	private int _id;
	private boolean _quit = false;
	private String _user = null;
	private String _server;

	public ServerThread(Socket socket,ConnectionUDPServer udpConnetion, Database db, int id, String server) {
		try {
			_tcp = new ConnectionTCP(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		_udp = udpConnetion;
		_db = db;
		_id = id;
		_db.insertThread(_id, this);
		_server = server;
	}

	public String get_user() {
		return _user;
	}

	public ConnectionUDPServer get_udp() {
		return _udp;
	}

	public void set_user(String user) {
		_user = user;
	}

	public void Quit(){
		_quit = true;
	}

	public void run() {
		try{
			PDU fromClient;
			PDU toClient;
			fromClient =(PDU_UserPass) _tcp.recv();
			toClient = fromClient.visit(_db, _id);
			_tcp.send(toClient);

			while ((fromClient = (PDU) _tcp.recv()) != null) {
				if(_user == null){ //user/password incorrectos
					_tcp.send(new PDU_UserPassInvalid());
					break;
				}
				toClient = fromClient.visit(_db, _id);
				if(_quit){
					break;
				}
				_tcp.send(toClient);
				_tcp.reset();
				ArrayList<FileArtigo> files = _db.get_receiveFiles(_id);
				if(files != null){ // tem ficheiros para receber (depois UPDATE)
					while(files.size()>0){
						FileArtigo file = files.remove(0);
						ReceiveFile(file);
						_db.AddFile(file);
					}
					_db.FilesReceived(_id);
					_tcp.send(new PDU_UpdateReply(true));
				}
				FileArtigo toSend = _db.get_sendFile(_id);
				if(toSend != null){// tem ficheiro para enviar(depois DOWNLOAD)
					SendFile(toSend);
					_db.FilesSended(_id);
				}
			}
			_db.RemoveThread(_id);
			_tcp.close();
		} catch (IOException e) {
			System.out.println(_server + ": Client desconectou-se");
			_db.RemoveThread(_id);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println(_server + ": Erro ao ler do Socket.");
		}		
	}

	void ReceiveFile(FileArtigo file) {
		System.out.println(_server + ": vai receber ficheiro - " + file.get_file());
		_tcp.ReceiveFile(file,_server);
		System.out.println(_server + ": recebido ficheiro - " + file.get_file());		
	}

	private void SendFile(FileArtigo file){
		boolean success = true;
		System.out.println(_server + ": vai enviar ficheiro - "+ file.get_file());
		try {
			_tcp.SendFile(file,_server);
		} catch (FileNotFoundException e) {
			System.out.println(_server + ": Erro, ficheiro nao encontrado");
			success = false;
		}
		if(success){
			System.out.println(_server + ": ficheiro enviado");
		}
	}

}
