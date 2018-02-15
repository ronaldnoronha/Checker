import java.io.*;
import java.util.*;

public class checker {
	private ArrayList<String> file = new ArrayList<String>();
	private ArrayList<Transaction> transactionList = new ArrayList<Transaction>();
	public checker(String filename){	
		try{
			File fw = new File(filename);
			Scanner in = new Scanner(fw);
			
			while (in.hasNextLine()){
				file.add(in.nextLine());
			}
			in.close();
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		createTransactions();
		//printTransactions();
		transactionSummary();
		runViolationCheck2();
	}
	private void runViolationCheck2(){
		for (int i =0;i<transactionList.size();i++){
			if (transactionList.get(i).isRead() && transactionList.get(i).isOK()){
				int reqRead = transactionList.get(i).getStartLine();
				int retRead = transactionList.get(i).getEndLine();
				int reqWrite = 0;
				for (int j=i;j>=0;j--){
					if(transactionList.get(j).isWrite() && transactionList.get(j).getProcess()==transactionList.get(i).getProcess() && transactionList.get(j).getKey()==transactionList.get(i).getKey() && transactionList.get(j).isOK()){
						reqWrite = transactionList.get(j).getStartLine();
						break;
					}
				}
				if (!checkValidValue(i,retRead,reqWrite)) System.out.println("Violation with retRead at "+retRead);
				// if violation run getViolationId()
				//System.out.println("Transaction: "+i+" reqRead: "+reqRead+" retRead "+retRead+" reqWrite "+reqWrite);				
			}
		}
	}
	private boolean checkValidValue(int transactionId, int retRead, int reqWrite){
		if (reqWrite==0 && transactionList.get(transactionId).getValue().equals("nil")) return true;
		for (int i=0;i<transactionList.size();i++){
			if (transactionList.get(i).isWrite() && transactionList.get(i).isOK()){
				if (transactionList.get(i).timelineOverlaps(reqWrite,retRead)){
					if (transactionList.get(i).getValue().equals(transactionList.get(transactionId).getValue())) return true;
				}
			}
		}
		return false;
	}
	private void transactionSummary(){
		System.out.println("Total transactions: "+transactionList.size());
		int sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isRead()) sum++;
		}
		System.out.println("Total reads: "+sum);
		sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isRead() && transactionList.get(i).getOperationAcknowledgement().equals(":ok")) sum++;
		}
		System.out.println("Ok reads: "+sum);
		sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isRead() && transactionList.get(i).getOperationAcknowledgement().equals(":fail")) sum++;
		}
		System.out.println("fail reads: "+sum);
		sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isRead() && transactionList.get(i).getOperationAcknowledgement().equals(":info")) sum++;
		}
		System.out.println("info reads: "+sum);
		sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isWrite()) sum++;
		}
		System.out.println("Total writes: "+sum);
		sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isWrite() && transactionList.get(i).getOperationAcknowledgement().equals(":ok")) sum++;
		}
		System.out.println("Ok writes: "+sum);
		sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isWrite() && transactionList.get(i).getOperationAcknowledgement().equals(":fail")) sum++;
		}
		System.out.println("fail writes: "+sum);
		sum = 0;
		for (int i=0;i<transactionList.size();i++){
			if(transactionList.get(i).isWrite() && transactionList.get(i).getOperationAcknowledgement().equals(":info")) sum++;
		}
		System.out.println("info writes: "+sum);
	}
	private void printTransactions(){
		for (int i=0;i<transactionList.size();i++){
			transactionList.get(i).print();
		}
	}
	private void createTransactions(){
		String[] line;
		for (int i =0;i<file.size();i++){
			line = file.get(i).split("\t");
			if (line[1].equals(":invoke")){
				transactionList.add(new Transaction(i,line[2],Integer.parseInt(line[0]),line[3]));
				int j = i+1;
				int transactionId = transactionList.size()-1;
				int pID = transactionList.get(transactionId).getProcess();
				String type = transactionList.get(transactionId).getOperationType();
				int key = transactionList.get(transactionId).getKey();
				while (j<file.size() && transactionList.get(transactionList.size()-1).isOpen()){
					line = file.get(j).split("\t");
					if (!line[1].equals(":invoke")&&line[0].equals(Integer.toString(pID))){
						if (getKey(line[3]).equals(Integer.toString(key))){
							transactionList.get(transactionId).setOperationAcknowledgement(line[1]);
							transactionList.get(transactionId).setValue(getValue(line[3]));
							transactionList.get(transactionId).setEndLine(j);
						}
					}
					j++;
				}
			}	
		}
	}
	private void runViolationCheck(){
		/// organise a list of transactions	
		String[] line;
		boolean valid;
		for (int i=0;i<file.size();i++){
			line = file.get(i).split("\t");
			if (isReadOp(line)){
				String key = getKey(line[3]);
				String value = getValue(line[3]);
				int startLineNumber = getStartLineNumber(line[0],key,i);
				if (startLineNumber == 0 && value.equals("nil")){
					// for the case the pID never wrote anything and the read value was also nil
					valid = true;					
				}else{
					valid = valueInBetween(key,value,startLineNumber,i);					
				}
				
				if (valid){
					//System.out.println("All good at line "+(i+1)+" Process "+line[0]);
				} else {
					System.out.println("Violation at line "+(i+1)+" Process "+line[0]);
				}
			}
		}
	}

	private boolean isReadOp(String[] line){
		if (line[1].equals(":ok") && line[2].equals(":read")){
			return true;
		}
		return false;
	}
	private boolean isWriteOp(String[] line){
		if (line[1].equals(":ok") && line[2].equals(":write")){
			return true;
		}
		return false;
	}
	private boolean valueInBetween(String key, String value, int startLineNr, int endLineNr){
		String[] line;
		for (int i = startLineNr;i<=endLineNr;i++){
			line = file.get(i).split("\t");
			if (isWriteOp(line)){
				if (getKey(line[3]).equals(key) && getValue(line[3]).equals(value)){
					return true;
				}	
			}
		}
		return false;
	}
	
	public static String getKey(String line){
		String key = line.substring(line.indexOf('[')+1,line.indexOf(' '));
		return key;
	}
	public static String getValue(String line){
		String value = line.substring(line.indexOf(' ')+1,line.indexOf(']'));
		return value;
	}
	private int getStartLineNumber(String pID, String key, int counter){ 
		int lineNr=0;
		String[] line;
		for (int i=0;i<counter;i++){
			line = file.get(i).split("\t");
			if (isWriteOp(line)){
				if (line[0].equals(pID) && getKey(line[3]).equals(key)){
					lineNr = i;
				}
			}
		}
		return lineNr;
	}
	public void publishStats(){
		for (int i=0;i<transactionList.size();i++){
			
		}
	}
}
