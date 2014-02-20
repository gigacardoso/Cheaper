/*********** CLASSE SHOPS *****************
 * 
 * classe que representa um server
 * cria uma thread por cada conec�ao de cliente.
 * 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

import pdus.ConnectionUDPServer;
import pdus.Database;
import pdus.Server;
import pdus.ServerThread;


public class Shops {
	private static boolean _listening = true;
	
	public void Quit(){
		_listening = false;
	}
		
	public static void main(String[] args) throws IOException {

		ServerSocket serverSocket = null;
		ConnectionUDPServer udp  = null;
		Database db;
		int id=0;
		int porto= 0;
		String shopsFile = "", usersFile = "";
		File arguments = new File("args.db"); 
		String dados;
		if(args.length > 0){
			porto =Integer.parseInt(args[0]);
			if(args.length > 1){
				shopsFile = args[1];
				if(args.length > 2){
					usersFile = args[2];
				}
				else{// le userfile do ficheiro
					if(!arguments.exists()){
						System.err.println("ERRO: Tem de inserir argumentos pelo menos na primeira vez.");
						System.exit(1);
					}
					Scanner fileScanner = new Scanner(arguments);
					while(fileScanner.hasNext()) {
						if("UsersFile".equals(fileScanner.next())) {
							usersFile = fileScanner.next();
							break;
						}
					}
					fileScanner.close();				
				}
			}else{ // le usersFile e ShopsFIle;
				if(!arguments.exists()){
					System.err.println("ERRO: Tem de inserir argumentos pelo menos na primeira vez.");
					System.exit(1);
				}
				Scanner fileScanner = new Scanner(arguments);
				while(fileScanner.hasNext()) {
					dados = fileScanner.next();
					if("ShopsFile".equals(dados)){
						shopsFile = fileScanner.next();
					}
					if("UsersFile".equals(dados)) {
						usersFile = fileScanner.next();
					}
				}
				fileScanner.close();
			}
			
		}
		else { // le tudo;
			if(!arguments.exists()){
				System.err.println("ERRO: Tem de inserir argumentos pelo menos na primeira vez.");
				System.exit(1);
			}
			Scanner fileScanner = new Scanner(arguments);
			while(fileScanner.hasNext()) {
				dados = fileScanner.next();
				if("Porto".equals(dados)) {
					porto = Integer.parseInt(fileScanner.next());
				}
				if("ShopsFile".equals(dados)) {
					shopsFile = fileScanner.next();
				}
				if("UsersFile".equals(dados)) {
					usersFile = fileScanner.next();
				}
			}
			fileScanner.close();
		}
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arguments)));
		out.println("Porto " + porto);
		out.println("ShopsFile " + shopsFile);
		out.println("UsersFile " + usersFile);
		out.close();
		
		String Server = null;
		InetAddress addr = InetAddress.getLocalHost();
		String hostnameServer = addr.getHostName();


		BufferedReader in;
		TreeMap<String,Server> servers= new TreeMap<String,Server>();
		TreeMap<String,String> users= new TreeMap<String,String>();
		ArrayList<String> allStores = new ArrayList<String>();
		try {
			in = new BufferedReader(new FileReader("mDBs.dat"));
			String s, name, hostname = new String();
			int n, port;
			while((s = in.readLine()) != null){
				n= s.indexOf(" ");
				name = s.substring(0, n);
				s = s.substring(++n);
				n= s.indexOf(" ");
				hostname = s.substring(0, n);
				s = s.substring(++n);
				port = Integer.parseInt(s);
				if(porto == port && hostnameServer.equals(hostname)){
					Server = name;
				}
				Server host = new Server(name,hostname,port);
				servers.put(name,host);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(Server == null){
			System.out.println("ERRO: nao existe nenhum servidor que corra usando a porta "+ porto);
			System.exit(1);
		}		
	
		try {
			in = new BufferedReader(new FileReader(shopsFile));
			String s, server, loja = new String();
			int n;
			while((s = in.readLine()) != null){
				ArrayList<String> array = new ArrayList<String>();
				n= s.indexOf(" ");
				server = s.substring(0, n);
				s = s.substring(++n);
				n= s.indexOf(", ");
				while( n != -1 ){
					loja = s.substring(0, n);
					if(!allStores.contains(loja)){
						allStores.add(loja);
					}
					s = s.substring((n+2));
					n= s.indexOf(", ");
					array.add(loja);
				}
				if(!allStores.contains(s)){
					allStores.add(s);
				}
				array.add(s);
				Server host = servers.get(server);
				host.set_lojas(array);
				servers.put(server, host);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try{
			in = new BufferedReader(new FileReader(usersFile));
			String s, user , pass   = new String();
			int n;
			while((s = in.readLine()) != null){
				n= s.indexOf(" ");
				user = s.substring(0, n);
				pass = s.substring(++n);
				users.put(user, pass);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		db = new Database(servers, users, allStores, Server);
		db.loadDatabase(Server);
		db.Initialize();


		try {
			serverSocket = new ServerSocket(porto);
			udp = new ConnectionUDPServer(Server, porto, db);
		} catch (IOException e) {
			System.err.println(Server +": nao consegue fazer listen na porta " + porto);
			System.exit(1);
		}

		while (_listening){
			new ServerThread(serverSocket.accept(),udp, db, ++id,Server).start();
		}

		serverSocket.close();
	}

}
