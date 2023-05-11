import java.net.DatagramPacket;
import java.net.UnknownHostException;

//this packet will be sent in response to the RRQ by the client, it will contain an opcode of 6,
//and will send the window size, along with



public class OACKPacket extends TFTPPacket{

    private final int opCode = 6;
    //this will be able to be changed by a setter, this will just be a default value
    private  int WINDOW_SIZE = 64;
    private final int MAX_WINDOW_SIZE = 255;
    private int dataPacketSize = 512;

    private DatagramPacket packetHolder;

    private byte[] packetBytes;
    public OACKPacket(DatagramPacket packet, int type) throws UnknownHostException {
        super(packet, type);
        this.packetHolder = packet;
        //the only data(I can think of right now) that the OACK packet needs to send to the client
        //is the windowSize and the size of the data packets it will receive, as well as the opcode
        //but that's kinda implied at this point
        byte[] OACKBytes;
        //bytes for OACK packet, doesn't have to be this long
        OACKBytes = new byte[512];
        //first two bytes are 0 and 6, as specified for the type
        OACKBytes[0] = 0;
        OACKBytes[1] = (byte) type;
        //we then need to send the window size for the client to know
        OACKBytes[2] = (byte) WINDOW_SIZE;
        //set 0 separator
        OACKBytes[3] = 0;
        //then set the datapPacketsize across two bytes
        OACKBytes[4] = (byte)(dataPacketSize & 0xFF); // store the lower byte in the first slot
        OACKBytes[5] = (byte)((dataPacketSize >> 8) & 0xFF); // store the upper byte in the second slot

        this.packetBytes = OACKBytes;

    }

    public OACKPacket(DatagramPacket packet, int type, int windowSize, int totalNumPackets) throws UnknownHostException {
        super(packet, type);
        this.packetHolder = packet;
        //the only data(I can think of right now) that the OACK packet needs to send to the client
        //is the windowSize and the size of the data packets it will receive, as well as the opcode
        //but that's kinda implied at this point
        byte[] OACKBytes;
        //bytes for OACK packet, doesn't have to be this long
        OACKBytes = new byte[512];
        //first two bytes are 0 and 6, as specified for the type
        OACKBytes[0] = 0;
        OACKBytes[1] = (byte) type;
        //we then need to send the window size for the client to know
        OACKBytes[2] = (byte) windowSize;

        this.WINDOW_SIZE = windowSize;
        //set 0 separator
        OACKBytes[3] = 0;
        //then set the datapPacketsize across two bytes
        OACKBytes[4] = (byte)(dataPacketSize & 0xFF); // store the lower byte in the first slot
        OACKBytes[5] = (byte)((dataPacketSize >> 8) & 0xFF); // store the upper byte in the second slot
        OACKBytes[6] = 0;
        //set total number of packets to allow for 65,000 ish packets
        OACKBytes[7] = (byte)(totalNumPackets & 0xFF);
        OACKBytes[8] = (byte)((totalNumPackets >> 8) & 0xFF);

        this.packetBytes = OACKBytes;

    }



    public DatagramPacket getDatagramPacket(){
        DatagramPacket returnPacket = new DatagramPacket(packetBytes, packetBytes.length, packetHolder.getAddress(), PORT);

        return returnPacket;
    }

    public void setWindowSize(int windowSize) {
        if(windowSize > MAX_WINDOW_SIZE)
            this.WINDOW_SIZE = 255;
        else
            this.WINDOW_SIZE = windowSize;
    }
    public void setDataPacketSize(int dataPacketSize){this.dataPacketSize= dataPacketSize;}
    public int getWindowSize(){return this.WINDOW_SIZE;}
    public int getOpCode() {return opCode;}
    public int getDataPacketSize(){return this.dataPacketSize;};

}

