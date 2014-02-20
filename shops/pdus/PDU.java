package pdus;
/*************INTERFACE PDU - Server **************
 * 
 * 	interface para implementar o padr�o visitor
 * 
 */
import java.io.Serializable;



public interface PDU extends Serializable {

	public PDU visit(Database db, int id);
}
