package pdus;


/*************PDU USERPASS **************
 * 
 * 	contem username e password
 * 
 */

public class PDU_UserPass implements PDU{
	
	private static final long serialVersionUID = 5289228960570974364L;
	private String _user;
	private String _pass;	
	
	public PDU_UserPass(String user, String pass) {
		_user = user;
		_pass = pass;
		
	}

	public String get_user() {
		return _user;
	}

	public String get_pass() {
		return _pass;
	}


	public PDU visit(Database db, int id) {
		return db.accept(this, id);
	}
}
