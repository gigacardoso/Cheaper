package pdus;
/************** PDU UDP SYNCH REPLY*******************
 *  
 * contem o user que fez o update, ou null se nao tiver
 */

public class PDU_UDP_SynchReply extends PDU_UDP {

	private static final long serialVersionUID = 4390141543370904666L;
	private String _user;

	public PDU_UDP_SynchReply(String user) {
		_user = user;
	}

	public String get_user() {
		return _user;
	}
}
