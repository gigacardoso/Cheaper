package pdus;


/************* PDU Quit *******************/
public class PDU_Quit implements PDU {

	private static final long serialVersionUID = -2231849592016685125L;

	public PDU visit(Database db, int id) {
		return db.accept(this, id);
	}

}
