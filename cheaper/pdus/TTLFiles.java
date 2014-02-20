package pdus;
/************* CLASSE TTL FILES ******************
 * 
 * Thread que controla o tempo de vida dos ficheiros
 * recebidos por Download
 * 
 * le o tempo do ficheiro: Timers.dat
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class TTLFiles extends Thread {

	private TreeMap<String, Integer> _files = new TreeMap<String, Integer>();
	private int _ttl;
	private boolean _quit = false;
	private boolean _switch;
	private String _username;
	
	public TTLFiles(boolean s,String user) {
		try {
			_switch = s;
			_username = user;
			File timers = new File("Timers.dat");
			Scanner fileScanner;
			fileScanner = new Scanner(timers);
			while(fileScanner.hasNext()) {
				if("TTLDownload".equals(fileScanner.next())) {
					_ttl = Integer.parseInt(fileScanner.next());
					break;
				}
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cliente: Ficheiro Timers.dat nao existe.");
			System.out.println("Cliente: TTL Download usado: 50");
			_ttl = 50;
		}
	}
	
	private void SwitchPrint(String message){
		if(_switch){
			System.out.println(_username +": "+ message); 
		}
	}

	public void Quit() {
		_quit = true;
	}

	public void Decrementa(){
		Set<String> s = _files.keySet();
		ArrayList<String> toRemove = new ArrayList<String>();
		for(String ficheiro: s){
			int ttl = _files.get(ficheiro);
			if(--ttl == 0){
				toRemove.add(ficheiro);
			}else{
				_files.put(ficheiro, ttl);
			}
		}
		for(String file: toRemove){
			SwitchPrint("ficheiro "+ file + " eleminado pois TTL terminou.");
			_files.remove(file);
			File f = new File(file);
			System.gc();
			f.delete();
		}
	}

	public void run(){
		try {
			while(true){
				if(_quit){
					break;
				}
				TTLFiles.sleep(1000);
				Decrementa();
				try {
					TTLFiles.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void Add(String file) {
		_files.put(file, _ttl);
	}
}
