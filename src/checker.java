import java.io.*;
import java.util.*;

public class checker {
	private ArrayList<String> file = new ArrayList<String>();
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
		runViolationCheck();
	}
	
	private void runViolationCheck(){
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
}
