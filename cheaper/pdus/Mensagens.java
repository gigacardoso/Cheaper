package pdus;


/************* interface Mensagens ******************
 * 
 * interface para implementacao do padrao Visitor
 * 
 */

public interface Mensagens {
	
	public void accept(PDU_UpdateReply p);
	public void accept(PDU_UserPassInvalid p);
	public void accept(PDU_GetFiles p);
	public void accept(PDU_DownloadReply p);
	public void accept(PDU_SynchReply p);
	
}
