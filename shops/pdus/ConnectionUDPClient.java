package pdus;
/*********** CLASSE CONNECTION UDP CLIENT *****************
 * 
 * classe que representa uma conec��o udp
 * para enviar uma pdu
 *   
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;



public class ConnectionUDPClient {
	private DatagramSocket _socket;
	private InetAddress _address;
	private int _port;

	public ConnectionUDPClient(String hostname, int port) throws Exception {
		_socket = new DatagramSocket();
		_address = InetAddress.getByName(hostname);
		_port = port;
	}

	public ConnectionUDPClient(InetAddress address, int port) throws Exception {
		_socket = new DatagramSocket();
		_address = address;
		_port = port;
	}

	public void send(PDU_UDP pdu){
		try {     
			ByteArrayOutputStream byteStream = new
			ByteArrayOutputStream(5000);
			ObjectOutputStream os = new ObjectOutputStream(new
					BufferedOutputStream(byteStream));
			os.flush();
			os.writeObject(pdu);
			os.flush();
			//retrieves byte array
			byte[] sendBuf = byteStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(
					sendBuf, sendBuf.length, _address, _port);
			//int byteCount = packet.getLength();
			_socket.send(packet);
			os.close();
		}
		catch (UnknownHostException e)
		{
			System.err.println("Exception:  " + e);
			e.printStackTrace();    }
		catch (IOException e)    { e.printStackTrace();
		}
	}

	public void close() {
		_socket.close();
	}
}