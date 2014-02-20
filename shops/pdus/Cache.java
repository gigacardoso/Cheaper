package pdus;
/*********** CLASSE CACHE *****************
 * 
 * classe que representa a cache
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;


public class Cache extends Thread {

	private String _server;
	private int _ttl;
	private TreeMap<String,TreeMap<String,CacheElement>> _cache= new TreeMap<String,TreeMap<String,CacheElement>>();
	private Database _db;

	public Cache(String server, Database db) {
		_server = server;
		_db = db;
		try {
			File timers = new File("Timers.dat");
			Scanner fileScanner;
			fileScanner = new Scanner(timers);
			while(fileScanner.hasNext()) {
				if("TTLCache".equals(fileScanner.next())) {
					_ttl = Integer.parseInt(fileScanner.next());
					break;
				}
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println(_server + ": Ficheiro Timers.dat nao existe.");
			System.out.println(_server + ": TTL Cache usado: 50");
			_ttl = 50;
		}
	}
	
	/* Update
	 * elimina artigo inserido na base de dados da cache se este ca estiver
	 */
	public synchronized void Update(PDU_Update p){
		for(Artigo a: p.get_artigo()){
			String nome = a.get_artigo();
			String loja = a.get_loja();

			if( _cache.get(loja) != null){ //ja existe algum artigo nessa loja
				CacheElement c = _cache.get(loja).get(nome);
				if(c != null){// existe o artigo a inserir
					Remove(c);
				}
				continue;
			}
		}
	}
	
	/* Download
	 * verifica se tem o preco do artigo na cache.
	 */
	public synchronized TreeMap<String, Artigo> Download(String artigo, ArrayList<String> lojas, TreeMap<String, Artigo> precos) {
		int size = lojas.size();
		for(int i=0; i<size;++i){
			if(_cache.get(lojas.get(i)) != null){
				if(_cache.get(lojas.get(i)).get(artigo) != null){
				Artigo a = _cache.get(lojas.get(i)).get(artigo).get_artigo();
				if(a != null){
					a.set_loja(lojas.get(i));
					precos.put(lojas.get(i),a);
				}
				}
			}		
		}
		return precos;
	}
	
	/*DownloadSaveData
	 * guarda a informa�ao recebida por download.
	 */
	public synchronized void DownloadSaveData(TreeMap<String, Artigo> artigos){
		Set<String> set = artigos.keySet();
		for(String loja: set){
			Artigo a =artigos.get(loja);
			if(a.get_preco() == -1){// artigo nao conhecido
				continue;
			}
			if(_cache.get(loja) != null){
				_cache.get(loja).put(a.get_artigo(), new CacheElement(_ttl,a));
			}
			else {
				TreeMap<String,CacheElement> elementos = new TreeMap<String,CacheElement>();
				elementos.put(a.get_artigo(), new CacheElement(_ttl,a));
				_cache.put(loja,elementos);
			}
		}
	}
	
	/*Synch
	 * efectua synch na cache
	 */
	public synchronized String Synch(PDU_Synch p, String server, String username){
		TreeMap<String,CacheElement> elementos = new TreeMap<String,CacheElement>();
		String user =null;
		String loja= p.get_loja();
		String artigo= p.get_artigo();
		if(_cache.get(loja) != null){
			CacheElement c = _cache.get(loja).get(artigo);
			if(c != null){ // se ja existe artigo
				Artigo a = c.get_artigo();
				if(a.get_preco() == p.get_precoAntigo()){
					user= a.get_user();
				}
				a.set_preco(p.get_precoNovo());
				a.set_user(username);
				_cache.get(loja).get(artigo).set_ttl(_ttl);
			}
			else {
				Artigo a =  new Artigo(artigo,null,p.get_precoNovo());
				a.set_loja(loja);
				a.set_server(server);
				a.set_user(username);
				elementos.put(artigo,new CacheElement(_ttl,a));
			}
		}
		else {
			Artigo a = new Artigo(artigo,p.get_ficheiro(),p.get_precoNovo());
			a.set_loja(loja);
			a.set_server(server);
			a.set_user(username);
			elementos.put(artigo,new CacheElement(_ttl,a));
		}
		TreeMap<String,CacheElement> t = _cache.get(loja);
		if(t != null){
			t.putAll(elementos);
		}
		else{
			_cache.put(loja, elementos);
		}
		return user;
	}

	/*Decrementa
	 * decrementa os ttl dos artigos na cache
	 */
	public synchronized void Decrementa(){
		//System.out.println("\n#################  CACHE  ###################\n");
		Set<String> s = _cache.keySet();
		for(String loja: s){
			Set<String> elements = _cache.get(loja).keySet();
			TreeMap<String,CacheElement> artigos = _cache.get(loja);
			Iterator<String> i = elements.iterator();
			while(i.hasNext()){
				CacheElement elem = artigos.get(i.next());
				int ttl = elem.get_ttl();
				//System.out.println(loja+"-"+c+"-"+ttl+"------------"+ artigos.get(c).get_artigo().get_preco());
				if(--ttl == 0){
					System.out.println(_server + ": artigo "+ elem.get_artigo().get_artigo() +" loja " + loja+ " removido pois TTL terminou.");
					i.remove();
					//Remove(artigos.get(c));
				}else{
					elem.set_ttl(ttl);
					//artigos.get(c).set_ttl(ttl);
				}
			}
		}
	}

	private synchronized void Remove(CacheElement c) {
		System.out.println(c.get_artigo().get_loja());
		_cache.get(c.get_artigo().get_loja()).remove(c.get_artigo().get_artigo());
	}

	public void run(){
		while(true){
			try {
				Cache.sleep(1000);
				Decrementa();
				//_db.Imprime();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class CacheElement{

		private int _ttl;
		private Artigo _artigo;

		private CacheElement(int ttl, Artigo artigo) {
			_ttl = ttl;
			_artigo = artigo;
		}

		public int get_ttl() {
			return _ttl;
		}

		public void set_ttl(int ttl) {
			_ttl = ttl;
		}

		public Artigo get_artigo() {
			return _artigo;
		}
	}

	/*SynchUDP
	 * realiza o synch caso o receba por UDP;
	 */
	public synchronized String SynchUDP(PDU_UDP_Synch p, String server) {
		TreeMap<String,CacheElement> elementos = new TreeMap<String,CacheElement>();
		String user =null;
		String loja= p.get_loja();
		String artigo= p.get_artigo();
		if(_cache.get(loja) != null){
			CacheElement c = _cache.get(loja).get(artigo);
			if(c != null){ // se ja existe artigo
				Artigo a = c.get_artigo();
				if(a.get_preco() == p.get_precoAntigo()){
					user= a.get_user();
				}
				a.set_preco(p.get_precoNovo());
				a.set_user(p.get_user());
				_cache.get(loja).get(artigo).set_ttl(_ttl);
			}
			else {
				Artigo a =  new Artigo(artigo,null,p.get_precoNovo());
				a.set_loja(loja);
				a.set_server(server);
				a.set_user(p.get_user());
				elementos.put(artigo,new CacheElement(_ttl,a));
			}
		}
		else {
			Artigo a = new Artigo(artigo,p.get_ficheiro(),p.get_precoNovo());
			a.set_loja(loja);
			a.set_server(server);
			a.set_user(p.get_user());
			elementos.put(artigo,new CacheElement(_ttl,a));
		}
		TreeMap<String,CacheElement> t = _cache.get(loja);
		if(t != null){
			t.putAll(elementos);
			//_artigos.put(loja, t);
		}
		else{
			_cache.put(loja, elementos);
		}
		return user;
	}

}
