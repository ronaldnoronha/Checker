
public class Transaction {
	private int startLine;
	private Integer endLine = null;
	private String operationType;
	private String operationAcknowledgement;
	private int key;
	private int process;
	private String value;
	public Transaction(int start, String type, int pID, String keyValue){
		startLine = start;
		operationType = type;
		process = pID;
		key = Integer.parseInt(keyValue.substring(keyValue.indexOf("[")+1,keyValue.indexOf(" ")));
	}
	public void print(){
		if (isOpen()){
			System.out.println(startLine+"\t"+operationType+"\t"+process);
		} else {
			System.out.println(startLine+"\t"+operationType+"\t"+process+"\t"+key+"\t"+endLine+"\t"+operationAcknowledgement+"\t"+value);
		}
		return;
	}
	public boolean isOpen(){
		if(endLine==null){
			return true;
		}
		return false;
	}
	public boolean isWrite(){
		if(operationType.equals(":write")){
			return true;
		}
		return false;
	}
	public boolean isRead(){
		if(operationType.equals(":read")){
			return true;
		}
		return false;
	}
	public boolean isOK(){
		if(operationAcknowledgement.equals(":ok")){
			return true;
		}
		return false;
	}
	
	public String getOperationAcknowledgement(){
		return operationAcknowledgement;
	}
	public int getStartLine(){
		return startLine;
	}
	public int getProcess(){
		return process;
	}
	public int getEndLine(){
		return endLine;
	}
	public int getKey(){
		return key;
	}
	public String getValue(){
		return value;
	}
	public String getOperationType(){
		return operationType;
	}
	public void setOperationAcknowledgement(String message){
		operationAcknowledgement = message;
		return;
	}
	public void setValue(String message){
		value = message;
		return;
	}
	public void setEndLine(int nr){
		endLine = nr;
		return;
	}
	public boolean timelineOverlaps(int reqWrite, int retRead){
		if ((startLine<reqWrite && endLine<reqWrite) || (startLine>retRead && endLine>retRead)){
			return false;
		}
		return true;
	}
	
	

}
