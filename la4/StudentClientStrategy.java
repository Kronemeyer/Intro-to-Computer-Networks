import java.util.*;

public class StudentClientStrategy implements ClientStrategy{
    ArrayList<String> file;
    int nextNeeded=0;

    public StudentClientStrategy(){
        reset();
    }

    public void reset(){
        file = new ArrayList<String>();
    }

    public List<String> getFile(){
        return file;
    }

    public List<Message> sendRcv(List<Message> serverMsgs){
        for(Message m : serverMsgs){
            while(file.size() < m.num+1) file.add(null);
            file.set(m.num,m.msg);
            System.out.println(m.num+","+m.msg);
        }

        while(nextNeeded <file.size() && file.get(nextNeeded)!=null)
            ++nextNeeded;

        List<Message> ack = new ArrayList<Message>();
        Message m=new Message(nextNeeded,"ACK");
        ack.add(m);
        
        return ack;


    }

}