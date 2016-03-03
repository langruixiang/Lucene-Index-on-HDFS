package Net;

public class NetCmd {
	  public static enum CmdType {
		  UPDATEINDEX,
		  UPDATESUCESS,
		  RECEIVESEARCHWORDS,
		  SEARCHSUCCESS,
		  GETSEARCHSERVERIP,
		  DEFUT
	  }
	  
	  private String cmdParameter;
	  
	  private CmdType cmdType;
	  private String IP;
	  
	  public String CreateMessage (String sourceIP,CmdType cmdtype){
		  String messageTemp = null;
		  if(cmdtype == CmdType.UPDATEINDEX){
			  messageTemp = sourceIP + ",UPDATEINDEX";
		  }else if(cmdtype == CmdType.UPDATESUCESS){
			  messageTemp = sourceIP + ",UPDATESUCESS";			  
		  }else if(cmdtype == CmdType.RECEIVESEARCHWORDS){
			  messageTemp = sourceIP + ",RECEIVESEARCHWORDS";
		  }else if(cmdtype == CmdType.SEARCHSUCCESS){
			  messageTemp = sourceIP + ",SEARCHSUCCESS";
		  }else if(cmdtype == CmdType.GETSEARCHSERVERIP){
			  messageTemp = sourceIP + ",GETSEARCHSERVERIP";
		  }else if(cmdtype == CmdType.DEFUT){
			  messageTemp = sourceIP + ",DEFUT";
		  }
		  
		  return messageTemp;
	  }
	  
	  public String CreateMessage(String sourceIP,String cmdParameter){
		  String messageTemp = sourceIP + "," + cmdParameter;
		  return messageTemp;
	  }
	  
	  public void DecodeMessage(String message){
		  String []items = message.split(",");
		  IP = items[0];
		  
		  if(items[1].equals("UPDATEINDEX")){
			  cmdType = CmdType.UPDATEINDEX;			  
		  }else if(items[1].equals("UPDATESUCESS")){
			  cmdType = cmdType.UPDATESUCESS;
		  }else if(items[1].equals("RECEIVESEARCHWORDS")){
			  cmdType = cmdType.RECEIVESEARCHWORDS;
		  }else if(items[1].equals("SEARCHSUCCESS")){
			  cmdType = cmdType.SEARCHSUCCESS;
		  }else if(items[1].equals("GETSEARCHSERVERIP")){
			  cmdType = cmdType.GETSEARCHSERVERIP;
		  }else if(items[1].equals("DEFUT")){
			  cmdType = cmdType.DEFUT;
		  }else {
			  cmdParameter = items[1];
		  }
	  }
	  
	  public String GetCmdParameter(){
		  return cmdParameter;
	  }
	  
	  public String GetCmdSourceIP(){
		  return IP;
	  }
	  
	  public CmdType GetCmdType(){
		  return cmdType;
	  }
}
