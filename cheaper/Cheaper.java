/************* CLASSE CHEAPER ******************
 * 
 * Classe que engloba a leitura de mensagens do utilizador
 * e coloca numa pilha.
 * 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

import pdus.Artigo;
import pdus.ClientHandler;
import pdus.PDU_Download;
import pdus.PDU_Quit;
import pdus.PDU_Synch;
import pdus.PDU_Update;
import pdus.Server;


public class Cheaper{
	public static void main(String[] args) throws IOException {

		BufferedReader in_file = null;
		TreeMap<String,Server> _servers = new TreeMap<String,Server>();
		ClientHandler _clientHandler;
		boolean _switch = true;
		String username = "";
		String password = "";
		String localServer = "";
		File arguments = new File("clientArgs.db");
		if(args.length > 0){
			if(args[0].equals("/v")){
				_switch = true;
				if( args.length > 1){
					username =args[1];
					if(args.length > 2){
						password = args[2];
						if(args.length > 3){
							localServer = args[3];
						}
						else{// le userfile do ficheiro
							if(!arguments.exists()){
								System.err.println("ERRO: Tem de inserir argumentos pelo menos na primeira vez.");
								System.exit(1);
							}
							Scanner fileScanner = new Scanner(arguments);
							while(fileScanner.hasNext()) {
								if("LocalServer".equals(fileScanner.next())) {
									localServer = fileScanner.next();
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
							if("Password".equals(fileScanner.next())) {
								password = fileScanner.next();
							}
							if("LocalServer".equals(fileScanner.next())) {
								localServer = fileScanner.next();
							}
						}
						fileScanner.close();
					}
				}
				else {
					if(!arguments.exists()){
						System.err.println("ERRO: Tem de inserir argumentos pelo menos na primeira vez.");
						System.exit(1);
					}
					Scanner fileScanner = new Scanner(arguments);
					while(fileScanner.hasNext()) {
						if("Username".equals(fileScanner.next())) {
							username =fileScanner.next();
						}
						if("Password".equals(fileScanner.next())) {
							password = fileScanner.next();
						}
						if("LocalServer".equals(fileScanner.next())) {
							localServer = fileScanner.next();
						}
					}
					fileScanner.close();
				}
				
			}
			else{
				username =args[0];
				if(args.length > 1){
					password = args[1];
					if(args.length > 2){
						localServer = args[2];
					}
					else{// le userfile do ficheiro
						if(!arguments.exists()){
							System.err.println("ERRO: Tem de inserir argumentos pelo menos na primeira vez.");
							System.exit(1);
						}
						Scanner fileScanner = new Scanner(arguments);
						while(fileScanner.hasNext()) {
							if("LocalServer".equals(fileScanner.next())) {
								localServer = fileScanner.next();
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
						if("Password".equals(fileScanner.next())) {
							password = fileScanner.next();
						}
						if("LocalServer".equals(fileScanner.next())) {
							localServer = fileScanner.next();
						}
					}
					fileScanner.close();
				}
			}
		}
		else { // le tudo;
			if(!arguments.exists()){
				System.err.println("ERRO: Tem de inserir argumentos pelo menos na primeira vez.");
				System.exit(1);
			}
			Scanner fileScanner = new Scanner(arguments);
			while(fileScanner.hasNext()) {
				if("Username".equals(fileScanner.next())) {
					username =fileScanner.next();
				}
				if("Password".equals(fileScanner.next())) {
					password = fileScanner.next();
				}
				if("LocalServer".equals(fileScanner.next())) {
					localServer = fileScanner.next();
				}
			}
			fileScanner.close();
		}
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arguments)));
		out.println("Username " + username);
		out.println("Password " + password);
		out.println("LocalServer " + localServer);
		out.close();

		in_file = new BufferedReader(new FileReader("mDBs.dat"));
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
			_servers.put(name,host);
		}
		in_file.close();

		String fromUser = null;

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		Server h = _servers.get(localServer);
		hostname = h.get_hostname();
		port = h.get_port();
		_clientHandler = new ClientHandler(hostname,port,localServer,username,password,_switch);
		_clientHandler.start();

		int comand;
		String comando;
		while ((fromUser = stdIn.readLine()) != null){
			 if(_clientHandler.Reconnecting()){
			 	_clientHandler.Input(fromUser);
			}
			else{
				if(fromUser.equals("Quit")){
					_clientHandler.addPDU("Quit" ,new PDU_Quit());
				}else {
					comand= fromUser.indexOf(" ");
					if(comand == -1){
						System.out.println("Comando nao conhecido");
					}
					else{
						comando = fromUser.substring(0, comand);
						fromUser=fromUser.substring(++comand);
						if(comando.equals("Update")){
							PDU_Update p = ParseUpdate(fromUser);
							if(p!= null){
								_clientHandler.addPDU("Update", p);
							}
						}
						if(comando.equals("Download")){
							ArrayList<ArrayList<String>> result = ParseDownload(fromUser);
							if(result != null){
								ArrayList<String> lojas = result.remove(0);
								ArrayList<String> artigos = result.remove(0);

								if(artigos.size() < 3){
									_clientHandler.addPDU("Download" ,new PDU_Download(lojas,artigos));
								}
								else{
									ArrayList<String> artigosToSend;
									for(int i=0; i < artigos.size();){
										artigosToSend = new ArrayList<String>();
										for(int j= 0; i < artigos.size() && j < 3; i++, j++){
											artigosToSend.add(artigos.get(i));
										}
										_clientHandler.addPDU("Download" ,new PDU_Download(lojas,artigosToSend));
									}
								}
							}


						}
						if(comando.equals("Synch")){
							PDU_Synch p = ParseSynch(fromUser);
							if(p != null){
								_clientHandler.addPDU("Synch", p);
							}
						}
						if(!comando.equals("Synch") && !comando.equals("Download") && !comando.equals("Update")){
							System.out.println("Comando nao conhecido");
						}
					}
				}				
			}			
		}
		stdIn.close();

	}
	
	private static PDU_Update ParseUpdate(String fromUser) {
		int lj= fromUser.indexOf(": ");
		if(lj == -1 || lj == 0){
			System.out.println("Formato Errado. Comando tem de ter forma:");
			System.out.println("Update loja: artigo[-ficheiro]/preco[; artigo[-ficheiro]/preco]");
			return null;
		}
		String loja = fromUser.substring(0, lj);
		fromUser=fromUser.substring((lj+2));
		boolean mais = true;
		ArrayList<Artigo> array = new ArrayList<Artigo>();
		while(mais){
			int next= fromUser.indexOf("; ");
			String umArtigo;
			if(next != -1){
				umArtigo = fromUser.substring(0, next);
				fromUser=fromUser.substring((next+2));
			}
			else {
				umArtigo = fromUser;
				mais=false;
			}
			int ficheiro= umArtigo.indexOf("-");
			int precoi= umArtigo.indexOf("/");
			if(precoi == -1 || precoi == 0){
				System.out.println("Formato Errado. Comando tem de ter forma:");
				System.out.println("Update loja: artigo[-ficheiro]/preco[; artigo[-ficheiro]/preco]");
				return null;
			}
			String art =  null;
			String fich = null;
			if(ficheiro != -1){
				art =  umArtigo.substring(0, ficheiro);
				fich = umArtigo.substring(++ficheiro, precoi);
				File f = new File(fich);
				if(!f.exists()){
					System.out.println("Ficheiro nao existente: "+ fich);
					return null;
				}
			}
			else{
				art = umArtigo.substring(0, precoi);
			}
			try {
				int preco = Integer.parseInt(umArtigo.substring(++precoi));
				Artigo a = new Artigo(art,fich,preco);
				a.set_loja(loja);
				array.add(a);
			}
			catch(NumberFormatException e) {
				System.out.println("Preco tem de ser numero inteiro. Comando tem de ter forma:");
				System.out.println("Update loja: artigo[-ficheiro]/preco[; artigo[-ficheiro]/preco]");
				return null;
			}
		}
		return new PDU_Update(loja,array);
	}

	private static ArrayList<ArrayList<String>> ParseDownload(String fromUser) {
		ArrayList<ArrayList<String>> result = null;
		int virgula = fromUser.indexOf(", ");
		int art = fromUser.indexOf(": ");
		ArrayList<String> lojas = new ArrayList<String>();
		ArrayList<String> artigos = new ArrayList<String>();
		while(virgula != -1  && virgula < art){
			if(virgula == 0){
				System.out.println("Formato Errado. Comando tem de ter forma:");
				System.out.println("Download [Loja, ... :] Ai [, Aj...]");
				return null;
			}
			String loja =  fromUser.substring(0, virgula);
			fromUser=	fromUser.substring((virgula+2));
			virgula = fromUser.indexOf(", ");
			art = fromUser.indexOf(": ");
			lojas.add(loja);
		}					
		if(art != -1){
			if(art == 0){
				System.out.println("Formato Errado. Comando tem de ter forma:");
				System.out.println("Download [Loja, ... :] Ai [, Aj...]");
				return null;
			}
			String loja =  fromUser.substring(0, art);
			lojas.add(loja);
			fromUser= fromUser.substring((art+2));
		}
		virgula = fromUser.indexOf(", ");
		int ultimo = fromUser.indexOf(";");
		String artigo;
		while(virgula != -1){
			if(virgula == 0){
				System.out.println("Formato Errado. Comando tem de ter forma:");
				System.out.println("Download [Loja, ... :] Ai [, Aj...]");
				return null;
			}
			artigo =  fromUser.substring(0, virgula);
			fromUser=	fromUser.substring((virgula+2));
			virgula = fromUser.indexOf(", ");
			ultimo = fromUser.indexOf(";");
			artigos.add(artigo);
		}
		if( ultimo != -1){
			if(ultimo == 0){
				System.out.println("Formato Errado. Comando tem de ter forma:");
				System.out.println("Download [Loja, ... :] Ai [, Aj...]");
				return null;
			}
			artigo = fromUser.substring(0, ultimo);
			artigos.add(artigo);
		}
		else {
			if(fromUser.equals("")){
				System.out.println("Formato Errado. Comando tem de ter forma:");
				System.out.println("Download [Loja, ... :] Ai [, Aj...]");
				return null;
			}
			artigo = fromUser;
			artigos.add(artigo);
		}
		result = new ArrayList<ArrayList<String>>();
		result.add(lojas);
		result.add(artigos);
		return result;	
	}

	private static PDU_Synch ParseSynch(String fromUser) {
		int artigo = fromUser.indexOf(": ");
		int ficheiro= fromUser.indexOf("-");
		int old_preco= fromUser.indexOf("/");
		int new_preco = fromUser.indexOf("->");
		if(artigo == -1 || old_preco == -1 || new_preco == -1){
			System.out.println("Formato Errado. Comando tem de ter forma:");
			System.out.println("Synch loja: artigo[-ficheiro]/precoAntigo->precoNovo");
			return null;
		}
		String loja = fromUser.substring(0, artigo);
		String art;
		String file = null;
		int precoAntigo, precoNovo;
		if(ficheiro == -1 || ficheiro == new_preco){
			art =fromUser.substring((artigo+2), old_preco);
		}
		else {
			art = fromUser.substring((artigo+2), ficheiro);
			file = fromUser.substring(++ficheiro, old_preco);
			File f = new File(file);
			if(!f.exists()){
				System.out.println("Ficheiro nao existente: "+ file);
				return null;
			}
		}
		fromUser = fromUser.substring(++old_preco);
		new_preco = fromUser.indexOf("->");
		try {
			precoAntigo = Integer.parseInt(fromUser.substring(0,new_preco));
			precoNovo = Integer.parseInt(fromUser.substring((new_preco+2)));
		}
		catch(NumberFormatException e) {
			System.out.println("Formato Errado. Comando tem de ter forma:");
			System.out.println("Synch loja: artigo[-ficheiro]/precoAntigo->precoNovo");
			return null;
		}
		return new PDU_Synch(loja, art, file, precoAntigo, precoNovo);
	}

}
