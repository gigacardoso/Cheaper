package pdus;
/************* PDU SHELL ******************
 * 
 * contem o PDU para ser enviada e uma string 
 * a dizer que PDU �.  
 * 
 */
public class PDU_Shell {
	
	private String _type;
	private PDU _pdu;
	public PDU_Shell(String type, PDU pdu) {
		_type = type;
		_pdu = pdu;
	}
	public String get_type() {
		return _type;
	}
	public PDU get_pdu() {
		return _pdu;
	}
	
	
		
}
