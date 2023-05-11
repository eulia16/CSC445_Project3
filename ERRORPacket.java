import java.net.DatagramPacket;
import java.net.UnknownHostException;

//error packet
public class ERRORPacket extends TFTPPacket {


    public ERRORPacket(DatagramPacket packet, int type) throws UnknownHostException {
        super(packet, type);
    }




}
