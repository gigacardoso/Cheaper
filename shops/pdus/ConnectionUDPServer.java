package pdus;
/*********** CLASSE CONNECTION UDP SERVER *****************
 * 
 * classe que representa uma coneccao udp que recebe pdus   
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;



public class ConnectionUDPServer implements Runnable {
	private String _server;
	private DatagramSocket _socket;
	private Thread _thread;
	private Database _db;
	private TreeMap<Integer,TreeMap<String, Artigo>> _repliesD = new TreeMap<Integer,TreeMap<String, Artigo>>();
	private TreeMap<Integer,SynchReply> _repliesS = new TreeMap<Integer,SynchReply>();
	private TreeMap<Integer, Boolean> _timer =  new TreeMap<Integer, Boolean>();
	private int _TODownload;
	private int _TOSynch;

	public ConnectionUDPServer(String server, int port, Database db){

		try {
			_server = server;
			_db=db;
			_socket = new DatagramSocket(port);
			System.out.println(_server + ": [UDP] a usar a porta " + port);
			System.out.println(_server + ": [UDP] espera que o cliente envie data...");
			_thread = new Thread(this);
		} catch (SocketException e) {
			e.printStackTrace();
		}	
		try {
			File timers = new File("Timers.dat");
			Scanner fileScanner;
			fileScanner = new Scanner(timers);
			while(fileScanner.hasNext()) {
				if("TODownload".equals(fileScanner.next())) {
					_TODownload = Integer.parseInt(fileScanner.next());
				}
				if("TOSynch".equals(fileScanner.next())) {
					_TOSynch = Integer.parseInt(fileScanner.next());
				}
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println(_server + ": Ficheiro Timers.dat nao existe.");
			System.out.println(_server + ": TO Download usado: 10");
			System.out.println(_server + ": TO Synch usado: 10");
			_TODownload = 10;
			_TOSynch = 10;
		}
		_thread.start();
	}

	public void run() {
		while(true) {
			try {
				PDU_UDP pdu = this.recv();
				if(pdu.get_tipo().equals("Request")){ // se for pedido vai tratalo
					System.out.println(_server + ": Recebida mensagem UDP.");
					pdu.visit(_db);
				}
				else if(pdu.get_comando().equals("Download")){ // se for resposta poe no Treemap
					System.out.println(_server + ": Download - Recebida resposta UDP.");
					PDU_UDP_DownloadReply r = (PDU_UDP_DownloadReply) pdu.get_pdu();
					_repliesD.put(pdu.get_id(), r.get_precos());
				}else if(pdu.get_comando().equals("Synch")){
					System.out.println(_server + ": Synch - Recebida resposta UDP.");
					PDU_UDP_SynchReply r = (PDU_UDP_SynchReply) pdu.get_pdu();
					if(r.get_user() != null){
						_repliesS.put(pdu.get_id(),new SynchReply(r.get_user()));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}


		}
	}

	public PDU_UDP recv() throws Exception {
		try {
			byte[] recvBuf = new byte[5000];
			DatagramPacket packet = new DatagramPacket(recvBuf,recvBuf.length);
			_socket.receive(packet);
			ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
			PDU_UDP pdu = (PDU_UDP) is.readObject();
			is.close();
			return(pdu);
		}
		catch (IOException e) {
			System.err.println("Exception:  " + e);
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return(null);
	}
	
	/* WaitingForDownload(int id)
	 * espera ate receber uma resposta ou o timeout espirar
	 */
	public TreeMap<String, Artigo> WaitingForDownload(int id) {
		TreeMap<String, Artigo> res;
		_repliesD.put(id, null);
		_timer.put(id,true);
		Timer timer = new Timer(); 
		timer.schedule(new RemindTask(id), _TODownload*1000);
		System.out.println(_server + ": Download - activado TimeOut de " + _TODownload + " segundos." );
		while(_timer.get(id)){
			res = _repliesD.get(id);
			if(res != null){
				timer.cancel();
				_repliesD.remove(id);
				return res;
			}
		}
		System.out.println(_server + ": Download - TimeOut Expirado.");
		_repliesD.remove(id);
		return null;
	}
	
	/* WaitingForDownload(int id)
	 * espera ate receber num respostas ou o timeout espirar
	 */
	public String WaitingForSynch(int id) {
		String res = null;
		_timer.put(id,true);
		Timer timer = new Timer(); 
		timer.schedule(new RemindTask(id), _TOSynch*1000);
		System.out.println(_server + ": Synch - activado TimeOut de " + _TOSynch + " segundos." );
		while(_timer.get(id)){
			if(_repliesS.get(id) != null ){
				if(_repliesS.get(id).get_user() != null){
					timer.cancel();
					res =  _repliesS.get(id).get_user();
					break;
				}
			}
		}
		if(res != null){
			System.out.println(_server + ": Synch - TimeOut Expirado.");
		}
		_repliesS.remove(id);
		return res;
	}
	
	
	private class RemindTask extends TimerTask {
		private int _id;

		private RemindTask(int id) {
			_id =id;
		}

		public void run() {
			_timer.put(_id,false);
		}
	}
	
	private class SynchReply {

		private String _user =  null;
		
		public SynchReply(String user){
			_user=user;
		}
		public String get_user() {
			return _user;
		}
		public void set_user(String user) {
			_user = user;
		}
	}

}
