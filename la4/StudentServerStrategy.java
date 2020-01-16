import java.util.*;
import java.lang.Math;

public class StudentServerStrategy implements ServerStrategy{
    List<String> file;
    ArrayList<Integer> sent;
    boolean avoidance;
    int cwnd=1, ssthresh=16, ACK=0, dupACK=0, lastACK=0, rtt=1, avg=0, mavg=0, ema=0, smoother=0;

    public StudentServerStrategy(){
        reset();
    }

    public void setFile(List<String> file){
        this.file = file;
    }

    public void reset(){
        sent = new ArrayList<>();

    }

    public List<Message> sendRcv(List<Message> clientMsgs){

        if (!clientMsgs.isEmpty()) {
            for(Message m: clientMsgs){
                ACK = m.num;
            }
        }
        
        // CHECK FOR DUPLICATE ACK
        if (ACK == lastACK) {
            dupACK++;
            // FAST RECOVERY
            if (dupACK==3){
                dupACK = 0;
                ssthresh = cwnd/2;
                cwnd = avg/rtt;
            } 
        } else {
            dupACK = 0;
            if (cwnd<=ssthresh && !avoidance) {
                cwnd += ACK-lastACK;
                cwnd = (int) Math.pow(2,rtt);
            } else 
            // CONGESTION AVOIDANCE
            {   
                cwnd += 1; 
                avoidance = true;
            }
        }
        
        avg += ACK-lastACK;
        rtt++;
        lastACK = ACK;

        List<Message> msgs = new ArrayList<Message>();

        // send if you can
        if (ACK+cwnd < file.size()) {
            for (int i = ACK;i<(ACK+cwnd);i++) {
                msgs.add(new Message (i,file.get(i)));
            } 
        // prevent sending past end of file
        } else {
            for (int i = ACK;i<file.size();i++) {
                msgs.add(new Message (i,file.get(i)));
            }
        }
        return msgs;
        }
    
}