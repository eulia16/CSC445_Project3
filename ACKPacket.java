import java.net.DatagramPacket;
import java.net.UnknownHostException;

//ack packet
public class ACKPacket extends TFTPPacket{

    private int blockNumber;
    private byte[] ackHeader;

    private DatagramPacket packet;
    public ACKPacket(DatagramPacket passedPacket, int type, int blockNum,  int encryptValue) throws UnknownHostException {
        super(passedPacket, type);
        this.packet = passedPacket;

        ackHeader = new byte[5];
        //define ACK header information
        ackHeader[0] = 0;
        ackHeader[1] = (byte) type;
        ackHeader[2] = (byte)(blockNum & 0xFF); // store the lower byte in the first slot
        ackHeader[3] = (byte)((blockNum >> 8) & 0xFF); // store the upper byte in the second slot
        ackHeader[4] = 0;

        //int result = (bytes[3] << 8) | (bytes[2] & 0xFF);

        //added code everything else worked before
        ackHeader = XOR(ackHeader, encryptValue);


    }

    public DatagramPacket getDatagramPacket(){
        DatagramPacket returnPacket = new DatagramPacket(ackHeader, ackHeader.length, packet.getAddress(), PORT);

        return returnPacket;
    }

    public void setBlockNumber(int num){
        ackHeader[2] = (byte)(num & 0xFF); // store the lower byte in the first slot
        ackHeader[3] = (byte)((num >> 8) & 0xFF); // store the upper byte in the second slot
        this.blockNumber = num;
    }
    public  int getBlockNumber(){return this.blockNumber;}

    public byte[] XOR(byte[] values, int key) {
        byte[] returnBytes = new byte[values.length];
        for (int i = 0; i < values.length; ++i) {
            returnBytes[i] = (byte) (values[i] ^ key);
        }
        return returnBytes;

    }


}

