package pdus;


/*************PDU USERPASSINVALID **************
 * 
 * 	diz que o par user/pass estavam incorrectos
 * 
 */
public class PDU_UserPassInvalid implements PDU {
	
	private static final long serialVersionUID = 2823563740493112617L;

	public PDU_UserPassInvalid() {
	}

	public void visit(ClientHandler ch) {
		ch.accept(this);
	}

}
