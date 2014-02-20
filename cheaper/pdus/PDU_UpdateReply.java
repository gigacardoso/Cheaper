package pdus;


/*************PDU UPDATE REPLY **************
 * 
 * 	diz se o update teve sucesso ou nao
 * 
 */
public class PDU_UpdateReply implements PDU{

	private static final long serialVersionUID = 1880555477107622803L;
	boolean _success;

	public PDU_UpdateReply(boolean success) {
		_success = success;
	}
	

	public boolean get_success() {
		return _success;
	}

	public void visit(ClientHandler ch) {
		ch.accept(this);
	}
	

}
