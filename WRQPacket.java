import java.net.DatagramPacket;
import java.net.UnknownHostException;

//write request packet class
public class WRQPacket extends TFTPPacket{

    public WRQPacket(DatagramPacket packet, int type) throws UnknownHostException {
        super(packet, type);

    }
}
