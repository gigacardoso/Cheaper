package pdus;
/*********** CLASSE DATABASE *****************
 * 
 * classe que representa a base de dados
 * trata todos os predidos dos clientes
 * 
 * Atributos:
 * 		_servers - todos os servers (key: name)
 * 		_users - todos users e pass (key: user)
 * 		_artigos - (key: loja) (key: artigo)
 * 		_cache - cache
 * 		_threads - todas as serverthreads (key: id)
 * 		_allStrores
 * 		_server - nome do server cuja base de dados esta e
 * 		_receiveFiles - lista de artigos por receber (key: id)
 * 		_sendFIle - artigo para enviar (key: id)
 * 		_files - todos os ficheiros que o servidor tem (key: nome)
 * 
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;


public class Database implements Mensagens, UDP_Handler {

	private TreeMap<String,Server> _servers; 
	private TreeMap<String,String> _users;
	private TreeMap<String,TreeMap<String,Artigo>> _artigos = new TreeMap<String,TreeMap<String,Artigo>>();
	private Cache _cache;
	private TreeMap<Integer,ServerThread> _threads = new TreeMap<Integer,ServerThread>();
	private ArrayList<String> _allStores;
	private String _server;
	private TreeMap<Integer,ArrayList<FileArtigo>> _receiveFiles = new TreeMap<Integer,ArrayList<FileArtigo>>();
	private TreeMap<Integer,FileArtigo> _sendFile = new TreeMap<Integer,FileArtigo>();
	private TreeMap<String,FileArtigo> _files = new TreeMap<String,FileArtigo>();

	public Database(TreeMap<String, Server> servers,
			TreeMap<String, String> users,ArrayList<String> allStores,String server) {
		_servers = servers;
		_users = users;
		_allStores = allStores;
		_server = server;
		_cache = new Cache(server, this);
		_cache.start();
	}

	public ArrayList<FileArtigo> get_receiveFiles(int id) {
		return _receiveFiles.get(id);
	}

	public FileArtigo get_sendFile(int id) {
		return _sendFile.get(id);
	}

	public void insertThread(int id, ServerThread s) {
		_threads.put(id,s);
	}

	public void FilesReceived(int id) {
		SaveDatabase(_server, "Artigos");
		_receiveFiles.remove(id);		
	}

	public void FilesSended(int id) {
		_sendFile.put(id,null);
	}

	public void AddFile(FileArtigo file) {
		_files.put(file.get_loja()+file.get_artigo(),file);
		SaveDatabase(_server, "Files");
	}
	
	public void Imprime() {
		System.out.println("\n#################  DADOS  ###################\n");
		Set<String> s = _artigos.keySet();
		for(String loja: s){
			TreeMap<String,Artigo> elements = _artigos.get(loja);
			Set<String> t = elements.keySet();
			for(String c: t){
				System.out.println(loja+"-"+c+"--"+ elements.get(c).get_preco());
			}
		}
		System.out.println("\n#################  Files  ###################\n");
		s = _files.keySet();
		for(String lojaArtigo: s){
			FileArtigo e = _files.get(lojaArtigo);
			System.out.println(e.get_loja()+"-"+e.get_artigo()+"--"+e.get_file());
		}
		System.out.println("_________________________________________________");
	}

	public void Initialize() {
		SaveDatabase(_server,"Artigos");
		SaveDatabase(_server, "Files");
	}

	public void RemoveThread(int id) {
		_threads.remove(id);
	}

	/***************************************
	 * 									   *
	 * 		Fun�oes Tratamento de PDUs	   *
	 * 									   *
	 ***************************************/
	
	/*accept(PDU_UserPass
	 * verifica se o par user/pass esta correcto
	 */
	public synchronized PDU accept(PDU_UserPass p, int id) {
		System.out.println(_server + ": recebido Username/Password.");
		String user = p.get_user();
		String pass = p.get_pass();
		String password = _users.get(user);

		if(password == null){//nao existe utilizador
			return new PDU_Null();
		}
		if(pass.equals(password)){// pass correcta
			_threads.get(id).set_user(user);
			return new PDU_Null();
		}
		return new PDU_Null();//pass errada
	}
	
	/*accept(PDU_Update
	 * realiza o update.
	 * devolde:
	 * 		pdu_UpdateReply se nao ha ficheiros
	 * 		pdu_GetFiles se tem ficheiros para receber
	 */
	public synchronized PDU accept(PDU_Update p, int id){
		System.out.println(_server + ": recebido comando Update.");
		String loja = p.get_loja();
		Server h = _servers.get(_server);
		TreeMap<String,Artigo> artigos = new TreeMap<String,Artigo>();
		if(h.get_lojas().contains(loja)){ //tem loja?
			ArrayList<Artigo> listaArtigos =p.get_artigo();
			ArrayList<FileArtigo> files = new ArrayList<FileArtigo>();		
			for(Artigo a: listaArtigos){
				if(a.get_preco() < 0){ 
					return new PDU_UpdateReply(false);
				}
				a.set_user(_threads.get(id).get_user());
				a.set_server(_server);
				String nome = a.get_artigo();
				String file = a.get_ficheiro();
				if( _artigos.get(loja) != null){ //ja existe algum artigo nessa loja
					Artigo art = _artigos.get(loja).get(nome);
					if(art != null){// existe o artigo a inserir
						art.set_preco(a.get_preco());
						art.set_user(_threads.get(id).get_user());
						if(file != null){ // artigo a inserir tem ficheiro?
							files.add(new FileArtigo(loja,nome,file));
							if(art.get_ficheiro()!=null){ //se o artigo tem ficheiro tambem
								File f = new File(loja+nome+art.get_ficheiro());
								System.gc();
								f.delete();
							}
							art.set_ficheiro(file);
						}
					}
					else {
						if(file != null){ // artigo a inserir tem ficheiro?
							files.add(new FileArtigo(loja,nome,file));
						}
						artigos.put(nome,a);
					}
					continue;
				}
				if(file != null){ // artigo a inserir tem ficheiro?
					files.add(new FileArtigo(loja,nome,file));
				}
				artigos.put(nome,a);
			}
			_cache.Update(p);
			TreeMap<String,Artigo> t = _artigos.get(loja);
			if(t != null){ // se ja tem essa loja
				t.putAll(artigos);// adiciona resto dos artigos nessa loja
			}
			else{
				_artigos.put(loja, artigos);//se nao coloca todos
			}
			if(files.size() > 0){ // se tem files pa recebr vai fazelo agora
				_receiveFiles.put(id, files);
				return new PDU_GetFiles(files);
			}
			else {
				SaveDatabase(_server,"Artigos");
				return new PDU_UpdateReply(true);
			}
		}
		return new PDU_UpdateReply(false);
	}
	
	/*accept(PDU_Quit
	 * termina a serverthread qye recebeu o quit
	 */
	public synchronized PDU accept(PDU_Quit p, int id) {
		System.out.println(_server + ": recebido comando Quit.");
		ServerThread s = _threads.get(id);
		s.Quit();
		return new PDU_Null();
	}
	
	/*accept(PDU_Download
	 * realiza Download
	 * 	verifica se tem no disco
	 * 	depois na cache
	 * 	se ainda faltar info envia pedidos udp
	 */
	public synchronized PDU accept(PDU_Download p, int id) {
		System.out.println(_server + ": recebido comando Download.");
		ArrayList<Artigo> result = new ArrayList<Artigo>();
		ArrayList<String> lojas = p.get_lojas();
		ArrayList<String> artigos = p.get_artigo();

		if(lojas.size() == 0){
			lojas = _allStores;
		}
		int size = lojas.size();
		for(int j=0; j< artigos.size(); j++){
			String artigo = artigos.get(j);
			TreeMap<String,Artigo> precos = new TreeMap<String,Artigo>();

			for(int i=0; i<size;++i){
				precos.put(lojas.get(i),new Artigo(artigo, null, -1));
				if(_artigos.get(lojas.get(i)) != null){
					Artigo a = _artigos.get(lojas.get(i)).get(artigo);
					if(a != null){
						a.set_loja(lojas.get(i));
						precos.put(lojas.get(i),a);
					}
					else{
						precos = _cache.Download(artigo,lojas,precos);
					}
				}
				else{
					precos = _cache.Download(artigo,lojas,precos);
				}
			}

			if(AllPositive(precos,lojas)){
				result.add(LowestPrice(precos,lojas));
			}
			else{
				ArrayList<String> lojasFalta= StoresLeft(precos,lojas); 
				ArrayList<String> horizonte= GetHorizon(lojasFalta);;
				TreeMap<String,Artigo> res;
				Random random = new Random();
				int newServer;
				while(!AllPositive(precos,lojas)){
					lojasFalta = StoresLeft(precos,lojas);
					if(horizonte.size() == 0){
						break;
					}
					newServer = random.nextInt(horizonte.size());
					String server = horizonte.remove(newServer);
          if(server.equals(_server)){
            continue;
          }
					ConnectionUDPClient udp;
					try {
						System.out.println(_server + ": Download - vai pedir informacao a "+ server);
						udp = new ConnectionUDPClient(_servers.get(server).get_hostname(),_servers.get(server).get_port());
						udp.send(new PDU_UDP(_servers.get(server).get_hostname(),_servers.get(server).get_port(),id,_server,"Request","Download",new PDU_UDP_Download(lojasFalta,artigo)));

						res =_threads.get(id).get_udp().WaitingForDownload(id);
						if(res != null){
							System.out.println(_server + ": Download - "+ server +" devolveu informacao.");
							_cache.DownloadSaveData(res);				
							precos = Join(precos, res, lojasFalta);
						}
					} catch (Exception e) {
						System.out.println(_server +": impossivel enviar mensagem a " + server);
					}
				}
				result.add(LowestPrice(precos,lojas));
			}
		}

		return new PDU_DownloadReply(result);
	}

	/*accept(PDU_Synch
	 * realiza synch
	 * guarda info no disco
	 * e envia a todos os outros servers.
	 * devolve o nome do user que fez o erro, ou null se nao o conhece
	 */
	public synchronized PDU accept(PDU_Synch p, int id) {
		System.out.println(_server + ": recebido comando Synch.");
		String loja = p.get_loja();
		String user = null;
		String username = null;
		TreeMap<String,Artigo> artigos = new TreeMap<String,Artigo>();
		Server h = _servers.get(_server);
		if(h.get_lojas().contains(loja)){// se a loja pertence ao servidor grava info em disco
			String artigo= p.get_artigo();
			if(_artigos.get(loja) != null){
				Artigo a = _artigos.get(loja).get(artigo);
				if(a != null){ // se ja existe artigo
					if(a.get_preco() == p.get_precoAntigo()){
						username= a.get_user();
					}
					if(p.get_ficheiro()!=null){
						if(a.get_ficheiro() != null){
							File f = new File(_server+loja+artigo+a.get_ficheiro());
							System.gc();
							f.delete();
						}
						a.set_ficheiro(p.get_ficheiro());
					}
					a.set_preco(p.get_precoNovo());
					a.set_user(_threads.get(id).get_user());
				}
				else {
					a =  new Artigo(artigo,p.get_ficheiro(),p.get_precoNovo());
					a.set_loja(loja);
					a.set_server(_server);
					a.set_user(_threads.get(id).get_user());
					artigos.put(artigo,a);
				}
			}
			else {
				Artigo a = new Artigo(artigo,p.get_ficheiro(),p.get_precoNovo());
				a.set_loja(loja);
				a.set_server(_server);
				a.set_user(_threads.get(id).get_user());
				artigos.put(artigo,a);
			}
			TreeMap<String,Artigo> t = _artigos.get(loja);
			if(t != null){
				t.putAll(artigos);
			}
			else{
				_artigos.put(loja, artigos);
			}
		}
		else {
			username = _cache.Synch(p,_server,_threads.get(id).get_user());
		}
		int size = SendToAllServers(new PDU_UDP_Synch(_threads.get(id).get_user(),p.get_loja(),p.get_artigo(),p.get_ficheiro(),p.get_precoAntigo(),p.get_precoNovo()), id);
		if(size != 0 && username == null){
			user = ReceiveFromAllServers(id);
			if(user != null && username == null){
				username = user;
			}
		}
		SaveDatabase(_server, "Artigos");
		if(p.get_ficheiro() != null){
			return new PDU_SynchReply(username, ServersInfo(loja), new FileArtigo(p.get_loja(),p.get_artigo(),p.get_ficheiro()));
		}
		else {
			return new PDU_SynchReply(username, null, null);
		}
	}
	
	/*accept(PDU_GetFiles
	 * insere o ficheiro para enviar na treemap sendFile
	 */
	public PDU accept(PDU_GetFiles p, int id) {
		_sendFile.put(id, p.get_files().get(0));
		return new PDU_Null();
	}

	/*accept(PDU_SendFile
	 * recebe ficheiro
	 */
	public PDU accept(PDU_SendFile p, int id) {
		System.out.println(_server + ": Vai receber ficheiro - "+ p.get_file());
		_threads.get(id).ReceiveFile(p.get_file());
		AddFile(p.get_file());
		SaveDatabase(_server, "Artigos");
		return new PDU_Null();
	}

	/***************************************
	 * 									   *
	 * 	 Funcoes Auxiliares de Download	   *
	 * 									   *
	 ***************************************/
	
	/* Join
	 * junta as TreeMaps, devolvendo um com os precos encontrados diferentes de -1
	 */
	private TreeMap<String, Artigo> Join(TreeMap<String, Artigo> precos,TreeMap<String, Artigo> res, ArrayList<String> lojasFalta) {
		for(String s: lojasFalta){
			if(precos.get(s).get_preco() > res.get(s).get_preco() || res.get(s).get_preco() != -1){
				precos.put(s,res.get(s));
			}
		}
		return precos;
	}

	/* GetHorizon
	 * devolde o Horizonte de Procura(servidores que cobrem as lojas do pedido Download)
	 */
	private ArrayList<String> GetHorizon(ArrayList<String> lojasFalta) {
		Set<String> servers = _servers.keySet();
		ArrayList<String> horizon = new ArrayList<String>();
		for(String s: servers){
			Server h = _servers.get(s);
			for(String loja: lojasFalta){
				if(h.get_lojas().contains(loja)){
					horizon.add(s);
					break;
				}
			}
		}	
		return horizon;
	}

	/* StoresLeft
	 * devolve a lista de lojas que ainda falta achar os precos
	 */
	private ArrayList<String> StoresLeft(TreeMap<String, Artigo> precos, ArrayList<String> lojas) {
		ArrayList<String> a = new ArrayList<String>();
		for(String s: lojas){
			if(precos.get(s).get_preco() == -1){
				a.add(s);
			}
		}
		return a;
	}

	/* LowestPrice
	 * devolve o artigo com o menos preco dentro das lojas todas
	 */
	private Artigo LowestPrice(TreeMap<String,Artigo> precos,ArrayList<String> lojas) {
		Artigo a = null;
		for(String s: lojas){
			if(a == null || precos.get(s).get_preco() < a.get_preco()  ){
				a = precos.get(s);
			}
		}
		return a;

	}

	/* AllPositive
	 * verifica se ja sabe os precos todos 
	 */
	private boolean AllPositive(TreeMap<String,Artigo> precos, ArrayList<String> lojas) {
		for(String s: lojas){
			if(precos.get(s).get_preco() < 0){
				return false;
			}
		}
		return true;
	}

	/***************************************
	 * 									   *
	 * 	   Fun�oes Auxiliares do Synch	   *
	 * 									   *
	 ***************************************/
	
	/* ServersInfo
	 * devolve lista de servidores que cobrem a loja
	 */
	private ArrayList<Server> ServersInfo(String loja) {
		Set<String> servers =  _servers.keySet();
		ArrayList<Server> a = new ArrayList<Server>();
		for(String server : servers){
			Server h = _servers.get(server);
			if(h.get_lojas().contains(loja)){
				a.add(h);
			}				
		}
		return a;
	}

	/* SendToAllServers
	 *  envia a pdu UDP a todos os outros servidores.
	 */
	private int SendToAllServers(PDU_UDP_Synch pdu,int id) {
		System.out.println(_server + ": Synch - vai enviar informacao a todos os outros servidores.");
		Set<String> servers =  _servers.keySet();
		int numServers = servers.size() - 1;
		for(String server : servers) {

			if(server.equals(_server))
				continue;
			try {
				System.out.println(_server + ": Synch - vai enviar informacao ao server " + server);
				Server s = _servers.get(server);
				ConnectionUDPClient udp = new ConnectionUDPClient(s.get_hostname(), s.get_port());
				udp.send(new PDU_UDP(_servers.get(server).get_hostname(),_servers.get(server).get_port(),id,_server,"Request","Synch", pdu));
				udp.close();
			} catch (Exception e) {
				numServers--;
				System.out.println(_server +": impossivel enviar mensagem a " + server);
			}
		}
		return numServers;
	}

	/* ReceiveFromAllServers
	 * devolve o nome devolvido pelos outros servidores contactados
	 * por UDP, ou null se nenhum o conhecia
	 */
	private String ReceiveFromAllServers(int id) {
		String user = _threads.get(id).get_udp().WaitingForSynch(id);
		return user;
	}

	/* SaveDatabase
	 * guarda a base de dados.
	 * artigos num ficheiro : NomedoServer+Artigos.db
	 * ficheiros num ficheiro: NomedoServer+Files.db
	 */
	public void SaveDatabase(String serverName, String comando){
		try {
			if(comando.equals("Artigos")){			
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
						new File("Artigos" + serverName + ".db"))));
				out.writeObject(_artigos);
				out.flush();
				out.close();
			}
			else {
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
						new File("Files" + serverName + ".db"))));
				out.writeObject(_files);
				out.flush();
				out.close();
			}
		}
		catch(IOException ioe) {
			System.out.println(_server + ": Erro ao gravar base de dados.");
		}
	}

	/* LoadDataBase
	 * Carrega a base de dados. Artigos e Ficheiros
	 */
	public void loadDatabase(String serverName){
		boolean carregou = true;
		try {
			File file = new File("Artigos" + serverName + ".db");
			if(file.exists()) {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
						new FileInputStream("Artigos" + serverName + ".db")));

				_artigos = (TreeMap<String,TreeMap<String,Artigo>>) in.readObject();
				in.close();
			}
			else {
				carregou = false;
				file.createNewFile();
				System.out.println(_server +": Base de dados vazia! Criado novo ficheiro chamado Artigos" + serverName + ".db");
			}
			File files = new File("Files" + serverName + ".db");
			if(files.exists()) {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
						new FileInputStream("Files" + serverName + ".db")));

				_files= (TreeMap<String,FileArtigo>) in.readObject();
				in.close();
			}
			else {
				carregou = false;
				files.createNewFile();
				System.out.println(_server +": Base de dados vazia! Criado novo ficheiro chamado Files" + serverName + ".db");
			}
		}
		catch(ClassNotFoundException cnfe) {
			System.out.println(_server + ": Erro ao carregar base de dados.");
		}
		catch(IOException ioe) {
			System.out.println(_server + ": Erro ao carregar base de dados.");
		}
		if(carregou){
			System.out.println(_server +": Base de dados carregada.");
		}
	}

	/***************************************
	 * 									   *
	 * 	  Fun�oes Tratamento de PDUs UDP   *
	 * 									   *
	 ***************************************/
	
	public void accept(PDU_UDP p) {
		p.get_pdu().visit(this, p);
	}

	/*accept(PDU_UDP_Download
	 * trata do pedido udp Download
	 */
	public void accept(PDU_UDP_Download p, PDU_UDP pdu) {
		System.out.println(_server +": recebido pedido de informacao Download [UDP].");
		ArrayList<String> lojas = p.get_lojas();
		String artigo = p.get_artigo();
		if(lojas.size() == 0){
			lojas = _allStores;
		}
		int size = lojas.size();
		TreeMap<String,Artigo> precos = new TreeMap<String,Artigo>();

		for(int i=0; i<size;++i){
			precos.put(lojas.get(i),new Artigo(artigo, null, -1));
			if(_artigos.get(lojas.get(i)) != null){
				Artigo a = _artigos.get(lojas.get(i)).get(artigo);
				if(a != null){
					a.set_loja(lojas.get(i));
					a.set_server(_server);
					precos.put(lojas.get(i),a);
				}
			}
		}
		ConnectionUDPClient udp;
		try {
			udp = new ConnectionUDPClient(_servers.get(pdu.get_from()).get_hostname(),_servers.get(pdu.get_from()).get_port());
			udp.send(new PDU_UDP(_servers.get(_server).get_hostname(),_servers.get(_server).get_port(),pdu.get_id(),pdu.get_from(),"Reply","Download",new PDU_UDP_DownloadReply(precos)));
		} catch (Exception e) {
			System.out.println(_server +": impossivel enviar mensagem a " + pdu.get_from());
		}
	}

	/*accept(PDU_UDP_Synch
	 * trata do pedido udp Synch
	 */
	public void accept(PDU_UDP_Synch p, PDU_UDP pdu) {
		System.out.println(_server +": recebido pedido de informacao Synch [UDP].");
		String loja = p.get_loja();
		String username = null;
		TreeMap<String,Artigo> artigos = new TreeMap<String,Artigo>();
		Server h = _servers.get(_server);
		if(h.get_lojas().contains(loja)){// se a loja pertence ao servidor grava info em disco
			String artigo= p.get_artigo();
			if(_artigos.get(loja) != null){
				Artigo a = _artigos.get(loja).get(artigo);
				if(a != null){ // se ja existe artigo
					if(a.get_preco() == p.get_precoAntigo()){
						username= a.get_user();
						System.out.println("o nome e - "+ username);
					}
					if(p.get_ficheiro()!=null){
						if(a.get_ficheiro() != null){
							File f = new File(loja+artigo+a.get_ficheiro());
							System.gc();
							f.delete();
						}
						a.set_ficheiro(p.get_ficheiro());
					}
					a.set_preco(p.get_precoNovo());
					a.set_user(p.get_user());
				}
				else {
					a =  new Artigo(artigo,p.get_ficheiro(),p.get_precoNovo());
					a.set_server(_server);
					a.set_user(p.get_user());
					artigos.put(artigo,a);
				}
			}
			else {
				Artigo a = new Artigo(artigo,p.get_ficheiro(),p.get_precoNovo());
				a.set_server(_server);
				a.set_user(p.get_user());
				artigos.put(artigo,a);
			}
			TreeMap<String,Artigo> t = _artigos.get(loja);
			if(t != null){
				t.putAll(artigos);
			}
			else{
				_artigos.put(loja, artigos);
			}
		}
		else {
			username = _cache.SynchUDP(p,_server);
			System.out.println("o nome e da cache - "+ username);
		}
		ConnectionUDPClient udp;
		try {
			System.out.println("antes de ser enviado - "+ username);
			udp = new ConnectionUDPClient(_servers.get(pdu.get_from()).get_hostname(),_servers.get(pdu.get_from()).get_port());
			udp.send(new PDU_UDP(_servers.get(_server).get_hostname(),_servers.get(_server).get_port(),pdu.get_id(),pdu.get_from(),"Reply","Synch",new PDU_UDP_SynchReply(username)));
		} catch (Exception e) {
			System.out.println(_server +": impossivel enviar mensagem a " + pdu.get_from());
		}
		SaveDatabase(_server, "Artigos");
		SaveDatabase(_server, "Files");
	}

}
