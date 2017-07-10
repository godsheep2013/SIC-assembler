/*
�t�ε{��
�Ŭf�� 101213038  
ToolKit:notepad++
*/
import java.util.*;
import java.io.*;
public class SIC { 
    public static void main(String[] abc) throws Exception{
		//�إ�OPcode Table�BSymbol Table
		Hashtable<String, String> OPTAB= new Hashtable<String, String>();
		String[] mnemon = {"ADD","ADDF","ADDR","AND","CLEAR","COMP","COMPF","COMPR","DIV","DIVF","DIVR","FIX","FLOAT","HIO","J","JEQ",
    		"JGT","JLT","JSUB","LDA","LDB","LDCH","LDF","LDL","LDS","LDT","LDX","LPS","MUL","MULF","MULR","NORM","OR","RD","RMO","RSUB","SHIFTL",
    		"SHIFTR","SIO","SSK","STA","STB","STCH","STF","STI","STL","STS","STSW","STT","STX","SUB","SUBF","SUBR","SVC","TD","TIO","TIX","TIXR","WD"};
		String[] operationcode = {"18","58","90","40","B4","28","88","A0","24","64","9C","C4","C0","F4","3C","30","34","38","48","00","68","50","70","08",
    		"6C","74","04","D0","20","60","98","C8","44","D8","AC","4C","A4","A8","F0","EC","0C","78","54","80","D4","14","7C","E8","84","10","1C","5C",
    		"94","B0","E0","F8","2C","B8","DC"};
		for(int i = 0; i < mnemon.length; i++)  {
    		OPTAB.put(mnemon[i], operationcode[i]);
    	}
		Hashtable<String, String> SYMTAB= new Hashtable<String, String>();
		//�ܼƫŧi
		boolean error = false;
		String errorLog = "";
		int WhichLine = 0;
		int LOCCTR = 0;
		String START_LOCCTR = "";
		String END_LOCCTR = "";
		String ProgName ="";
		int hadStart = 0;
		String TempFile = "";
		boolean RESB_NEXT = false;
		int RESB_POSITION = 0;
		boolean BYTE_NEXT = false;
		int BYTE_POSITION = 0;
		//Ū��
		FileReader fr = new FileReader("test.asm");
    	BufferedReader br = new BufferedReader(fr);
		/*********************************/
		/************ PASS 1 *************/
		/*********************************/
		while(br.ready()){
			String line = br.readLine();
			WhichLine++;
			System.out.println("[��"+WhichLine+"��]Ū�쪺���e�O�G"+line);
			String[] token=line.trim().split("\\s+");
			int HasNote=0,i=0;
			String NoCommentLine="";//�h�����Ѥ���
			while((i<token.length)&&(HasNote==0)){
				if(token[i].indexOf(".") != -1){ //�J����ѡA�]�w���Ѫ�FLAG �M�᭫�s���o��TOKEN����ѫe
					HasNote = 1;
					token[i] = token[i].substring(0,token[i].indexOf("."));
				}
				NoCommentLine += token[i] + " ";
				i++;
			}
			System.out.println(" NoCommentLine = "+NoCommentLine);
			
			//���otoken��
			String[] token1 = NoCommentLine.trim().split("\\s+");
			i=0;
			int tokenNum=0;
			int CorrectIndexed = 0;
			while((i<token1.length)){
				if(token1[i].equals(",")){ //���ާP�_
					CorrectIndexed = 1;
				}
				i++;
				tokenNum++;
			}
			String label="-",mnemonic="-",operand="-",operater="-";
			//++++++++++PSEUDO INSTRUCTION SET++++++++++++
			/**  START  **/
			if((NoCommentLine.toUpperCase().indexOf("START") != -1)){
				if(hadStart==1){//�p�G�w�gSTART�L�F ���Ϳ��~�T��
					error = true;
					errorLog += "��"+WhichLine+"��:Duplicate START   \r\n";
				}
				if(tokenNum==3 && token[1].equals("START")){
					hadStart = 1;
					ProgName = token[0];
					LOCCTR = Integer.parseInt(token[2], 16)-3;//16-->10
					START_LOCCTR = token[2];
					System.out.println("start from this line. locctr="+START_LOCCTR+" ProgName="+ProgName);
					operater = token[1];
					TempFile += WhichLine+" "+token[2]+" "+label+" "+mnemonic+" "+operand+" "+operater+"  \r\n";
					continue;
				}else{
					error = true;
					errorLog += "��"+WhichLine+"��:START ERROR   \r\n";
				}
			}
			/**  WORD  **/
			if((NoCommentLine.toUpperCase().indexOf("WORD") != -1)){
				if(tokenNum==2 && token[0].equals("WORD")){//WORD 4063
					operater = token[0];
					operand = Integer.toHexString(Integer.parseInt(token[1]));
					LOCCTR+=3;
				}
				else if(tokenNum==3 && token[1].equals("WORD")){//MAXLEN WORD 4096
					label = token[0];
					operater = token[1];
					operand = Integer.toHexString(Integer.parseInt(token[2]));
					LOCCTR+=3;
				}else{
					error = true;
					errorLog += "��"+WhichLine+"��:WORD ERROR   \r\n";
					
				}
			}
			/**  RESW  **/
			if((NoCommentLine.toUpperCase().indexOf("RESW") != -1)){
				if(tokenNum==3 && token[1].equals("RESW") ){//��LABEL
					label = token[0];
					operater = token[1];
					operand = token[2];
					LOCCTR += Integer.parseInt(String.valueOf(token[2]))*3;
				}
				else if(tokenNum==2 && token[0].equals("RESW") ){//�S��LABEL
					operater = token[0];
					operand = token[1];
					LOCCTR += Integer.parseInt(String.valueOf(token[1]))*3;
				}
				else{
					error = true;
					errorLog += "��"+WhichLine+"��:RESW ERROR   \r\n";
				}
			}
			/**  RESB  **/
			if(RESB_NEXT){
				LOCCTR += RESB_POSITION;
				RESB_NEXT = false;
			}
			if((NoCommentLine.toUpperCase().indexOf("RESB") != -1)){
				if(tokenNum==3 && token[1].equals("RESB")){//BUFFER RESB 4096
					label = token[0];
					operater = token[1];
					operand =  token[2];
					LOCCTR += 3;
					RESB_POSITION = Integer.parseInt(token[2])-3;
					RESB_NEXT = true;
				}
				else if(tokenNum==2 && token[0].equals("RESB")){//RESB 4096
					operater = token[0];
					operand =  token[1];
					LOCCTR += 3;
					RESB_POSITION = Integer.parseInt(token[2])-3;
					RESB_NEXT = true;
				}else{
					error = true;
					errorLog += "��"+WhichLine+"��:RESB ERROR   \r\n";
				}
			}
			/**  BYTE  **/
			if(BYTE_NEXT){
				LOCCTR += BYTE_POSITION;
				BYTE_NEXT = false;
			}
			if((NoCommentLine.toUpperCase().indexOf("BYTE") != -1)){
				if(tokenNum==3 && token[1].equals("BYTE")){//EOF BYTE C'EOF'
					label = token[0];
					operater = token[1];
					operand = token[2];
					String[] tmp = token[2].split("'");
					if (tmp[0].equalsIgnoreCase("C")) {
						if(tmp[1].length()>3 || tmp[1].indexOf(" ")!=-1){//C ������פ��i�W�L3�A���i���Ů�
							error = true;
							errorLog += "��"+WhichLine+"��:ERROR input of BYTE C   \r\n";
						}
    	                LOCCTR += 3;
						BYTE_NEXT = true;
						BYTE_POSITION = tmp[1].length()-3;//locctr�����[�Wlength
    	             } 
					 else if (tmp[0].equalsIgnoreCase("X")) {
						 //X ������פ��i�W�L6 ���i���Ů� �ӥB�@�w�n�O���ƭӦr��
						 if((tmp[1].length()>6) || (tmp[1].indexOf(" ")!=-1) || (tmp[1].length()%2!=0)){
							error = true;
							errorLog += "��"+WhichLine+"��:ERROR input of BYTE X   \r\n";
						}
    	                LOCCTR += 3;
						BYTE_NEXT = true;
						BYTE_POSITION = Integer.parseInt(String.valueOf(tmp[1].length()/2))-3;//locctr�[�Wlength/2
    	             }else{
						error = true;
						errorLog += "��"+WhichLine+"��:ERROR syntax of BYTE   \r\n";
					 }
				}
			}
			/**  END  **/
			if((NoCommentLine.toUpperCase().indexOf("END ") != -1)){//�i�[label
				if((tokenNum==2)){//END FIRST
					operater = token[0];
					operand = token[1];
					END_LOCCTR =  Integer.toString(LOCCTR+3);
					if(SYMTAB.get(operand)==null){
						error = true;
						errorLog += "��"+WhichLine+"��:End error   \r\n";
						break;
					}
					else{
						TempFile += WhichLine+" "+Integer.toHexString(LOCCTR+3).toUpperCase()+" "+label+" "+mnemonic+" "+operand+" "+operater+"  \r\n";
						break;
					}
				}else{
					error = true;
					errorLog += "��"+WhichLine+"��:END ���~   \r\n";
				}
			}
			
			//**********************���ީw���B�z**********************
			if(NoCommentLine.indexOf(',') != -1){
				int commonNum = 0;
				for(int ii=0;ii<NoCommentLine.length();ii++){//�p�⦳�X�ӳr�I
					char check=NoCommentLine.charAt(ii);
					if(check==',')
						commonNum++;
				}
				if(commonNum>1){
					error = true;
					errorLog += "��"+WhichLine+"��:indexed addressing error   \r\n";
					continue;
				}
				//�p�G����r���O",X" --> ERROR
				String test = NoCommentLine.replaceAll("\\s+", "").toUpperCase();
				if(!test.substring(test.length()-2).equals(",X")){
					error = true;
					errorLog += "��"+WhichLine+"��:indexed addressing error   \r\n";
				}
				else if(tokenNum==5){//LABEL LDCH BUFFER , X
					label = token[0];
					mnemonic = token[1];
					operand = token[2]+token[3]+token[4];
					LOCCTR+=3;
				}
				else if(tokenNum==4){ 
					if(CorrectIndexed==1){//STCH BUFFER , X
						mnemonic = token[0];
						operand = token[1]+token[2]+token[3];
						LOCCTR+=3;
					}
					else if(CorrectIndexed==0){//��ܬO XXXX STCH BUFFER, X �άOXXXX  STCH BUFFER ,X
						error = true;
						errorLog += "��"+WhichLine+"��:error input   \r\n";
					}
				}
				else if(tokenNum==3  && token[1].charAt(token[1].length()-1)==','){//STCH BUFFER, X �άO  
					mnemonic = token[0];
					operand = token[1]+token[2];
					LOCCTR+=3;
					
				}
				else if(tokenNum==3  && token[2].equals(",X")){//STCH BUFFER ,X
					mnemonic = token[0];
					operand = token[1]+token[2];
					LOCCTR+=3;
				}else if(tokenNum==3 && token[2].charAt(token[2].length()-2)==','){//Lebal mun op && LDA LDA LENGTH,X
					label = token[0];
					mnemonic = token[1];
					operand = token[2];
					LOCCTR+=3;
				}
				else if(tokenNum==2){//STCH BUFFER,X
					mnemonic = token[0];
					operand = token[1];
					LOCCTR+=3;
				}else{
					error = true;//�Y����L���p�N�O����
					errorLog += "��"+WhichLine+"��:error input   \r\n";
				}
			}
			else{//���O����
			//RSUB
				if((NoCommentLine.toUpperCase().indexOf("RSUB") != -1)){
					if((tokenNum==2)&&(token[1].toUpperCase().equals("RSUB"))){//label RSUB
						label=token[0];
						mnemonic=token[1];
						LOCCTR+=3;
					}
					else if(tokenNum==1 && token[0].toUpperCase().equals("RSUB")){//RSUB
						mnemonic=token[0];
						LOCCTR+=3;
					}else{
						error = true;
						errorLog += "��"+WhichLine+"��:Syntax error   \r\n";
					}
				}
				else{
					if(mnemonic.equals("-") && operater.equals("-")){//���OPSEUDO OPERATER �]���O���� �ӥB�٨S����
						if(tokenNum == 3){ //LABEL MNEMONIC OPERAND
							label = token[0];
							mnemonic = token[1];
							operand = token[2];
							LOCCTR+=3;
						}
						else if(tokenNum == 2){//MNEMONIC OPERAND
							mnemonic = token[0];
							operand = token[1];
							LOCCTR+=3;
						}
					}
				}
			}
			//************** ��mnemonic�ǤJOPTAB��� ***************
			if(!mnemonic.equals("-")){
				if(OPTAB.get(mnemonic)!=null){
					//optable����
				}else{
					error = true;
					errorLog += "��"+WhichLine+"��:Undefined mnemonic   \r\n";
				}
			}
			//************** ��LABEL�MLOCCTR�ǤJSYMTAB ***************
			if(!label.equals("-")){
				if(SYMTAB.get(label)!=null){
					error = true;
					errorLog += "��"+WhichLine+"��:Duplicate Symbol   \r\n";
				}else{
					String tmp16 = Integer.toHexString(LOCCTR);
					tmp16 = tmp16.toUpperCase();
					SYMTAB.put(label,tmp16);
				}
				
				if(OPTAB.get(label)!=null){ //�p�Glabel�ϥ�opcode�r��
					error = true;
					errorLog += "��"+WhichLine+"��:label�ϥΫO�d�r��  \r\n";
				}
			}
			
			
			System.out.println("token:"+tokenNum+"  locctr:"+LOCCTR);
			System.out.println("label="+label+"  mnemonic="+mnemonic+"  operand="+operand+"  operater="+operater);
			
			
			String LOCCTR2 = Integer.toHexString(LOCCTR);
			LOCCTR2 = LOCCTR2.toUpperCase();
			if(label.equals("-") && mnemonic.equals("-") && operand.equals("-") && operater.equals("-")){
				//�Ŧ椣�L
			}else{
				TempFile += WhichLine+" "+LOCCTR2+" "+label+" "+mnemonic+" "+operand+" "+operater+"  \r\n";
			}
		}//end of while
		System.out.println(TempFile);
		System.out.println("==================");
		if(error){
			System.out.println(errorLog);
			System.exit(0);
		}
		
		/*********************************/
		/********** ���ͤ����� ***********/
		/*********************************/
		FileWriter fw = new FileWriter("SICtmp.txt");
		fw.write(TempFile);
		fw.write(errorLog);
		fw.close();
    	fr.close();
		
		/*********************************/
		/************ PASS 2 *************/
		/*********************************/
		FileReader fr2 = new FileReader("SICtmp.txt");
    	BufferedReader br2 = new BufferedReader(fr2);
		error = false;
		errorLog = "";
		/*              
		               0         1      2        3          4          5
		�����ɮ榡: WhichLine  LOCCTR  label   mnemonic   operand   operater
		*/
		ArrayList<String> CodeList = new ArrayList<String>();//object code buffer
		ArrayList<String> textLoc = new ArrayList<String>(); //�ΨӰO��t record���_�l��l
		String Line="-",label="-",mnemonic="-",operand="-",operater="-",LOCCTR2="-";
		int bufferIndex=0;
		String EndOperand ="";
		while(br2.ready()){
			String ObjCode = ""; 
			String line2 = br2.readLine();
			String[] t2 = line2.trim().split("\\s+");
			Line = t2[0];
			LOCCTR2 = t2[1];
			label = t2[2];
			mnemonic = t2[3];
			operand = t2[4];
			operater = t2[5];
			if(operater.equals("START"))
				continue;
			if(operater.equals("END")){
				EndOperand = operand;
			}
			if(!mnemonic.equals("-") && OPTAB.get(mnemonic)!=null && operand.indexOf(",X")<0){ //mnemonic��즳�F�� �ӥBOPTAB��search��
				ObjCode+=OPTAB.get(mnemonic);//����OPCODE�e��X��iObjCode
				if(!operand.equals("-")){//��operand���
					if(SYMTAB.get(operand)!=null){
						ObjCode+=SYMTAB.get(operand);
					}
					else{
						ObjCode+="0000";
						error = true;
						errorLog+="��"+Line+"��: undefined Symbol\r\n";
					}
				}
				else{//�S��operand��� RSUB
					ObjCode+="0000";
				}
			}
			if(operater.equals("BYTE")){
				String[] tmp = operand.split("'");
				if (tmp[0].equalsIgnoreCase("C")) { //�p�����
					char[] c = tmp[1].toCharArray();//��@�Ӥ@�Ӧr
					String s = "";
					for(int i=0;i<c.length;i++){
						s += String.valueOf(Integer.toHexString(c[i]).toUpperCase());//��FOR�j���v��ASCII�˶i�h
					}
					ObjCode+=s;
				} 
				else if(tmp[0].equalsIgnoreCase("X")) { //��������
    	            ObjCode+=tmp[1];
    	        }
			}
			if(operater.equalsIgnoreCase("WORD")){
				//int i = Integer.parseInt(operand);
				ObjCode+=Zero(operand,6);
			}
			if(operand.indexOf(",X")!=-1){//����
				String[] indexSplit = operand.split(",");
				ObjCode+=OPTAB.get(mnemonic);
				long tmplong = Integer.parseInt(SYMTAB.get(indexSplit[0]), 16);//16��10
				ObjCode+=Long.toHexString((tmplong+32768)).toUpperCase();//X (1000)2 =(8000)16   8000����10�i��=32768
			}
			if(CodeList.size() ==  0){ //�Ĥ@��
    			textLoc.add(LOCCTR2);
    			CodeList.add(ObjCode);
    		} else { 
    		    String tmpppp = CodeList.get(bufferIndex); 
    		    if(ObjCode!=null){ 
    		        String tmpppp2 = tmpppp+ObjCode;//����
    		        if(tmpppp2.length() > 60 || operater.equals("RESB") || operater.equals("RESW")){ //�[�i�h�|�W�L�j�p����
						if(CodeList.get(bufferIndex).equals("")){
							
						}else{
							bufferIndex ++; //���U�@���
							textLoc.add(LOCCTR2);
							CodeList.add(ObjCode);
						}
    		        }else{ //���|�W�L�j�p�A��P�@��
        		    	CodeList.remove(bufferIndex); //��[�_�Ӫ��A��^���
        		    	CodeList.add(tmpppp2);
    	   		    }
    		    }
    		}
		}//end of pass2 scan
		
		if(error){
			System.out.println(errorLog);
			System.exit(0);
		}
		
		/*********************************/
		/*********** �LRECORD ************/
		/*********************************/
		
		//����H record
		int aa=Integer.valueOf(END_LOCCTR); //10�i�쪺END
		int bb=Integer.parseInt(START_LOCCTR,16);//10�i�쪺START
		String ProgLength = Integer.toHexString(aa-bb);
		System.out.println("H "+Space(ProgName,6)+" "+Zero(START_LOCCTR,6)+" "+Zero(ProgLength,6).toUpperCase());
		
		//����T record
		for(int i = 0;i < CodeList.size();i++){
    	    System.out.println("T "+
    	            Zero(textLoc.get(i).toUpperCase(),6) +" "+    //�L T2-7��쪺�_�l��}
    	    		Zero(Integer.toHexString(CodeList.get(i).length()/2).toUpperCase(),2)+" "+//���������ت��X����
    				CodeList.get(i).toUpperCase());
    	}
		
		String lunch = SYMTAB.get(EndOperand);
    	//����E record
		System.out.println("E "+Zero(lunch.toUpperCase(),6));
    	
		
	}//end of main
	
	
    public static String Space(String s, int i){ //��ťհ��k�䪽��6
		StringBuffer tmp = new StringBuffer();
		tmp.append(s);
		int X = i - (s==null?0:s.length());
		if(X>0){
			for(int j=0;j<X;j++){
			tmp.append(" ");
			}
		}
      return tmp.toString();
    }
	
	
    public static String Zero(String s, int i){ //��0��쥪�䪽��6
      StringBuffer tmp = new StringBuffer();
      int X = i - (s==null?0:s.length());
      if(X>0){
        for(int j=0;j<X;j++){
          tmp.append("0");
        }
      }
      tmp.append(s);
      return tmp.toString();
    }
}