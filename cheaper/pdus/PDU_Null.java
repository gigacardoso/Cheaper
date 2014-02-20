package pdus;


/************* PDU NULL ******************
 * 
 * 	Pdu que nao faz nada
 * 	
 */
public class PDU_Null implements PDU {

	private static final long serialVersionUID = 6899303868095645443L;

	public void visit(ClientHandler ch) {
	}

}
