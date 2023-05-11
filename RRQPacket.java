import java.net.DatagramPacket;
import java.net.UnknownHostException;

//read request packet class

//remember this is the format of the RRQ packet
/*
         2 bytes    string   1 byte     string   1 byte
  -----------------------------------------------
  RRQ/  | 01/02 |  Filename  |   0  |    Mode    |   0  |
  WRQ    -----------------------------------------------



 */
public class RRQPacket extends TFTPPacket {
    //url to be sent(filename as specified )
    private String URL = "";
    //read request packets are of opcode type 1(check abstract TFTP class for all opcodes and associations)
    private final int RRQ = 1;
    //the mode is unnecessary for this project, so we will make it a null field
    private final String mode = "octet";
    private DatagramPacket passedPacket;
    private byte[] allRRQHeaderInfo;


    //constructor which uses default url/file name
    public RRQPacket(DatagramPacket packet, int type) throws UnknownHostException {
        super(packet, type);
        passedPacket = packet;
    }

    //constructor which allows URL/file name to be changed
    public RRQPacket(DatagramPacket packet, int type, String URL) throws UnknownHostException {
        super(packet, type);
        this.URL = URL;
        passedPacket = packet;
        byte[] data = passedPacket.getData();

        data[0] = 0;
        //set opcode(packet type)
        data[1] = (byte)this.getType();

        //we then must copy the filename passed to the packet
        byte[] bytesFromURL = URL.getBytes();
        System.out.println("bytesFromURL length: " + bytesFromURL.length);
        for(int i=0; i< bytesFromURL.length; ++i){
            data[2 + i] = bytesFromURL[i];
        }
        //insert 0 for separation
        data[2 + bytesFromURL.length ] = 0;
        //insert mode (only octet)
        byte[] modeBytes = mode.getBytes();
        System.out.println("modeBytes length: " + modeBytes.length);
        for(int i=0; i< modeBytes.length; ++i){
            //insert data at end of header
            data[2 + bytesFromURL.length + 1 + i] = modeBytes[i];
        }
        data[2 + bytesFromURL.length + 1 + modeBytes.length +1] = -1;

//        data[2 + bytesFromURL.length + 1 + modeBytes.length +1] = (byte)(port & 0xFF); // store the lower byte in the first slot
//        data[2 + bytesFromURL.length + 1 + modeBytes.length +2] = (byte)((port >> 8) & 0xFF); // store the upper byte in the second slot
//        data[2 + bytesFromURL.length + 1 + modeBytes.length +3] = -1;

        allRRQHeaderInfo = data;

    }



    //simple setters and getters
    public void setURL(String URL){
        this.URL = URL;
    }
    public String getURL(){ return this.URL; }
    public String getMode(){ return this.mode; }
    //method to obtain all info pertinant to RRQ packet to then be added to a datagram packet and sent for read request
    public byte[] getAllRRQHeaderInfo(){ return this.allRRQHeaderInfo; }
    public DatagramPacket getPassedPacket() {
        return passedPacket;
    }
    public DatagramPacket getRRQPacket(){
        //change current passed data to new header RRQ data
         this.passedPacket.setData(this.allRRQHeaderInfo);
         //return the passedPacket
         return this.passedPacket;
    }

}
