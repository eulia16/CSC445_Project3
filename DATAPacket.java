import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.UnknownHostException;

//data packet

/*data packet design
         2 bytes    2 bytes       n bytes
          ---------------------------------
   DATA  | 03    |   Block #  |    Data    |
          ---------------------------------
*
* */
public class DATAPacket extends TFTPPacket{

    //actual data to be sent
    private byte[] data;
    private byte[] headerInfo;
    private final int MAX_DATA_SIZE = 512;
    private final int type = 3;
    private int blockNumber;
    private final int MAX_BLOCKNUM_SIZE = 65536;//this is because the 2 bytes that hold the block nums max value is 2^16
    private DatagramPacket passedPacket;
    public DATAPacket(DatagramPacket packet, int type) throws UnknownHostException {
        super(packet, type);
        this.passedPacket = packet;

        //set this packets payload data
        this.data = this.passedPacket.getData();

        headerInfo = new byte[4];

        //set header info
        headerInfo[0] = 0;
        headerInfo[1] = 3;
        headerInfo[2] = (byte)(blockNumber & 0xFF); // store the lower byte in the first slot
        headerInfo[3] = (byte)((blockNumber >> 8) & 0xFF); // store the upper byte in the second slot


    }
    public DATAPacket(DatagramPacket packet, int type, int blockNum, int encryptKey) throws UnknownHostException {
        super(packet, type);
        this.passedPacket = packet;

        //set this packets payload data
        this.data = this.passedPacket.getData();

        headerInfo = new byte[4];

        //set header info
        headerInfo[0] = 0;
        headerInfo[1] = 3;
        headerInfo[2] = (byte)(blockNum & 0xFF); // store the lower byte in the first slot
        headerInfo[3] = (byte)((blockNum >> 8) & 0xFF); // store the upper byte in the second slot

        this.blockNumber = blockNum;
        headerInfo = XOR(headerInfo, encryptKey);



    }


    public void setBlockNumber(int block){this.blockNumber = block;}

    public void setData(byte[] data){ this.data = data;}

    public DatagramPacket getDatagramPacket(){
        byte[] allData = new byte[headerInfo.length + data.length];
        //copy header info into all data
        System.arraycopy(headerInfo, 0, allData, 0, headerInfo.length);
        //then load data info
        System.arraycopy(data, 0, allData, headerInfo.length, data.length);
        DatagramPacket returnPacket = new DatagramPacket(allData, allData.length, passedPacket.getAddress(), PORT);

        return returnPacket;
    }
    public int getBlockNumber(){return this.blockNumber;}
    public byte[] getHeaderInfo(){return this.headerInfo;}
    public byte[] getPacketData(){ return this.data; }

    public byte[] XOR(byte[] values, int key) {
        byte[] returnBytes = new byte[values.length];
        for (int i = 0; i < values.length; ++i) {
            returnBytes[i] = (byte) (values[i] ^ key);
        }
        return returnBytes;

    }

}
