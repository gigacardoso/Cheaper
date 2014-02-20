package pdus;
/*************INTERFACE PDU - Cliente ***********
 * 
 * 	interface para implementar o padr�o visitor
 * 
 */
import java.io.Serializable;





public interface PDU extends Serializable {

	public void visit(ClientHandler ch);
}
