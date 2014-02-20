package pdus;


/***************** INTERFACE ****************
 *  para implementar o padrao visitor
 */
public interface Mensagens {
	
	public PDU accept(PDU_UserPass p, int id);
	public PDU accept(PDU_Update p, int id);
	public PDU accept(PDU_Quit p, int id);
	public PDU accept(PDU_GetFiles p, int id);
	public PDU accept(PDU_Download p,int id);
	public PDU accept(PDU_Synch p, int id);
	public PDU accept(PDU_SendFile p, int id);
	
}
