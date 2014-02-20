package pdus;
/************* CLASSE ClIENTHANDLER ******************
 * 
 * Thread que trata da comunicacao do cliente com o servers
 * e trata das mensagens devolvidas pelo server
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;


public class ClientHandler extends Thread implements Mensagens{
	private ArrayList<PDU_Shell> _pdus = new ArrayList<PDU_Shell>();
	private TTLFiles _timer;
	private ConnectionTCP _tcp;
	private String _hostname;
	private int _port;
	private String _localServer;
	private String _username;
	private String _password;
	private boolean _switch;
	private String _running;
	private boolean _reconnecting = false;
	private String _input = null;

	public ClientHandler(String hostname, int port, String localServer,	String username, String password , boolean s) {
		_hostname = hostname;
		_port = port;
		_localServer = localServer;
		_username = username;
		_password = password;
		_switch = s;
		_timer =  new TTLFiles(s,username);
		_timer.start();
	}

	public void  addPDU(String type,PDU p) {
		if(type.equals("Quit")){
			if(_running != null && _running.equals("Synch")){
				System.out.println("Aguarde: Synch em curso...");
				_pdus.add(0, new PDU_Shell(type,p));
			}
			else{
				if(_running != null){
					//_timer.Quit();
					//_tcp.close();
					System.exit(1);
				}
				else{
					_pdus.add(0, new PDU_Shell(type,p));
				}
			}
		}
		else{
			_pdus.add(new PDU_Shell(type,p));
		}
	}

	public void run(){
		boolean connected = true;
		try {
			_tcp = new ConnectionTCP(_hostname,_port);
		} catch (Exception e1) {
			System.out.println(_localServer +  " inacessivel");
			Reconnect();
			connected =false;
		}
		if(connected){
			addPDU("UserPass",new PDU_UserPass(_username,_password));
		}

		while(true){
			try {
				if(!_pdus.isEmpty()){// existem pdus na pilha
					PDU_Shell toServer = _pdus.remove(0);
					PDU fromServer;
					if(toServer != null){
						_running = toServer.get_type();
						_tcp.send(toServer.get_pdu());
						SwitchPrint("enviei "+ _running+ " ao servidor");
						if(_running.equals("Quit")){
							break;
						}
						fromServer = _tcp.recv();
						fromServer.visit(this);
						_running = null;
					}
				}
				try {
					ClientHandler.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				System.out.println("Servidor Crashou");
				Reconnect();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		_timer.Quit();
		_tcp.close();
		System.exit(1);
	}
	
	public void accept(PDU_UserPassInvalid p) {
		System.out.println("Par Username/Password Invalido");
		System.exit(1);
	}

	public void accept(PDU_UpdateReply p) {
		SwitchPrint("recebida resposta ao Update");
		if(p.get_success()){
			System.out.println("sucesso na realizacao de Update");
		}
		else{
			System.out.println("insucesso na realizacao de Update");
		}	
	}

	/*
	 * accept(PDU_DOWNLOADREPLY)
	 * recebe info sobre artigo(s) se existem ficheiros estabelece
	 * conneccao tcp com o server e transfere o ficheiro 
	 */
	public void accept(PDU_DownloadReply p) {
		ArrayList<Artigo> artigos = p.get_artigo();
		ArrayList<String> result = new ArrayList<String>();
		SwitchPrint("Recebida resposta ao Download");
		for(int i =0 ; i< artigos.size() ; i++){
			Artigo a=artigos.get(i);
			if(a != null && a.get_preco() != -1){
				String artigo = a.get_artigo();
				String file = a.get_ficheiro();
				int preco = a.get_preco();
				String loja = a.get_loja();
				if(file != null){
					File ficheiro = new File(file);
					if(!ficheiro.exists()){
						String servidor = a.get_servidor();
						if(servidor.equals(_localServer)){
							try {
								ArrayList<FileArtigo> f = new ArrayList<FileArtigo>();
								f.add(new FileArtigo(loja,artigo,file));
								_tcp.send(new PDU_GetFiles(f));
								_tcp.recv();
								SwitchPrint("vai receber ficheiro - " + file);
								_tcp.ReceiveFile(file, _username, _switch);
								_timer.Add(file);
								SwitchPrint("ficheiro recebido");
							} catch (IOException e) {
								SwitchPrint("Error a ler do socket");
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
						}
						else{
							try {
								File db = new File("mDBs.dat");
								String hostname = "";
								int port = -1;
								try {
									Scanner fileScanner = new Scanner(db);
									while(fileScanner.hasNext()) {
										if(servidor.equals(fileScanner.next())) {
											hostname = fileScanner.next();
											port = Integer.parseInt(fileScanner.next());
											break;
										}
									}
									if(port == -1){
										SwitchPrint("Error nao foram encontrados dados para o servidor: "+ servidor);
										throw new IOException();
									}
									fileScanner.close();
								} catch (IOException ioe) {
									System.exit(1);
								}
								SwitchPrint("Criar Ligacao TCP com "+servidor);
								ConnectionTCP tcp = new ConnectionTCP( hostname, port);
								tcp.send(new PDU_UserPass(_username,_password));
								tcp.recv();
								ArrayList<FileArtigo> f = new ArrayList<FileArtigo>();
								f.add(new FileArtigo(loja,artigo,file));
								tcp.send(new PDU_GetFiles(f));
								tcp.recv();
								tcp.ReceiveFile(file, _username, _switch);
								_timer.Add(file);
								SwitchPrint("Terminar Ligacao TCP com "+servidor);
								tcp.close();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (Exception e) {
								SwitchPrint( servidor +  " inacessivel");
							}
						}
					}
					result.add("O menor preco de " + artigo + " foi encontrado na Loja "+loja+", e " + preco + ", vide "+ file);
				}else {
					result.add("O menor preco de " + artigo + " foi encontrado na Loja "+loja+", e " + preco);
				}
			}
			else {
				result.add("Nao existem informacoes sobre o artigo: " + a.get_artigo());
			}
		}
		for(int i=0; i<result.size(); i++){
			System.out.println(result.get(i));
		}
	}

	/*
	 * accept(PDU_SYNCHREPLY)
	 * recebe nome do user que fez o erro ou null
	 * se recebe nome e tinha ficheiro no synch,
	 * envia ficheiro 
	 */
	public void accept(PDU_SynchReply p) {
		SwitchPrint("Recebida resposta ao Synch");
		if(p.get_servers()!=null){
			ArrayList<Server> a = p.get_servers();
			for(Server s: a){
				if(s.get_name().equals(_localServer)){
					try {
						_tcp.send(new PDU_SendFile(p.get_file()));
						SwitchPrint("vai enviar ficheiro - "+ p.get_file().get_file());
						_tcp.SendFile(p.get_file());
						SwitchPrint("Ficheiro enviado.");
						_tcp.recv();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException c){
						c.printStackTrace();
					}
				}else{
					ConnectSendFile(s,p.get_file());
				}
			}
		}
		if(p.get_user() != null){
			System.out.println("Synch terminado: " + p.get_user());
		}
		else {
			System.out.println("Dificuldades no Synch");
		}
	}

	/*
	 * accept(PDU_GETFILES)
	 * recebe lista de ficheiros para enviar ao servidor
	 */
	public void accept(PDU_GetFiles p) {
		ArrayList<FileArtigo> files = p.get_files();
		while(files.size()>0){
			FileArtigo file = files.remove(0);
			SwitchPrint("vai enviar ficheiro - "+ file.get_file());
			_tcp.SendFile(file);
			SwitchPrint("Ficheiro enviado.");
		}
		PDU fromServer;
		try {
			fromServer = (PDU) _tcp.recv();
			fromServer.visit(this);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Reconnect()
	 * caso o servidor crashe trata de reconnectar o cliente com
	 * outro servidor ou o mesmo.
	 */
	private void Reconnect() {
		_reconnecting = true;
		System.out.println("Deseja tentar ligar-se a outro servidor? (sim/nao)");
		String fromUser;
		try {
			while(true){
				fromUser = getInput();
				if(fromUser.equalsIgnoreCase("nao")){
					_timer.Quit();
					System.exit(1);
				}
				if(fromUser.equalsIgnoreCase("sim")){
					break;
				}
				System.out.println("Resposta tem de ser 'sim' ou 'nao'");
			}
			TreeMap<String,Server> servers = new TreeMap<String,Server>();
			BufferedReader in_file = new BufferedReader(new FileReader("mDBs.dat"));
			String s, name, hostname = new String();
			int n, port;
			while((s = in_file.readLine()) != null){
				n= s.indexOf(" ");
				name = s.substring(0, n);
				s = s.substring(++n);
				n= s.indexOf(" ");
				hostname = s.substring(0, n);
				s = s.substring(++n);
				port = Integer.parseInt(s);
				Server host = new Server(name,hostname,port);
				servers.put(name,host);
			}
			in_file.close();
			System.out.println("Servidores: "+ servers.keySet());
			System.out.println("Quer se ligar a qual?");
			while(true){
				fromUser = getInput();
				if(servers.get(fromUser)!= null){
					Server serv = servers.get(fromUser);
					_localServer = fromUser;
					_hostname= serv.get_hostname();
					_port = serv.get_port();
					break;
				}
				System.out.println("Tem de ser um destes servidores: "+ servers.keySet());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			_tcp = new ConnectionTCP( _hostname, _port);
		} catch (Exception e) {
			System.out.println(_localServer +  " inacessivel");
			Reconnect();
		}
		_pdus.add(0, new PDU_Shell("UserPass",new PDU_UserPass(_username,_password)));
		_reconnecting = false;
		System.out.println("Reconnectado!!");
	}
	
	/*
	 * getInput()
	 * espera pela resposta do user
	 */
	private String getInput() {
		while(_input == null){
			try {
					ClientHandler.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		String input = _input;
		_input = null;
		return input;
	}

	private void SwitchPrint(String message){
		if(_switch){
			System.out.println(_username +": "+ message); 
		}
	}

	/*
	 * ConnectSendFile(Server server, FileArtigo file)
	 * cria ligacao TCP com server e envia o ficheiro file
	 */
	private void ConnectSendFile(Server server, FileArtigo file) {
		try {
			SwitchPrint("Criar Ligacao TCP com "+ server.get_name());
			ConnectionTCP tcp = new ConnectionTCP( server.get_hostname(), server.get_port());
			tcp.send(new PDU_UserPass(_username,_password));
			tcp.recv();
			tcp.send(new PDU_SendFile(file));
			SwitchPrint("vai enviar ficheiro - "+ file.get_file());
			tcp.SendFile(file);
			SwitchPrint("ficheiro recebido");
			tcp.recv();
			SwitchPrint("Terminar Ligacao TCP com "+ server.get_name());
			tcp.close();
		} catch (UnknownHostException e){
			SwitchPrint(server.get_name() +  " inacessivel");
		}  catch (ClassNotFoundException e) {
			SwitchPrint(server.get_name() +  " inacessivel");
		} catch (IOException e) {
			SwitchPrint(server.get_name() +  " inacessivel");
		}
	}

	public boolean Reconnecting() {
		return _reconnecting ;
	}

	public void Input(String fromUser) {
		_input  = fromUser;
		
	}

}
