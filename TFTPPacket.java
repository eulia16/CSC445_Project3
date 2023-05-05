
//this class will be the abstract class that will be inherited by the various different
//types of TFTP packets which are built on UDP, and will have about three inheriting packets
//the ERRORPacket, the OACKPacket, and the ACKPacket classes.

//as described in the TFTP Options extension link for assignment 2:
/*
*  opcode  operation
            1     Read request (RRQ)
            2     Write request (WRQ)
            3     Data (DATA)
            4     Acknowledgment (ACK)
            5     Error (ERROR)
            6     OACK (ACK w/ OPTIONS)
            *
            * Order of Headers

                                                  2 bytes
    ----------------------------------------------------------
   |  Local Medium  |  Internet  |  Datagram  |  TFTP Opcode  |
    ----------------------------------------------------------

TFTP Formats

   Type   Op #     Format without header

          2 bytes    string   1 byte     string   1 byte
          -----------------------------------------------
   RRQ/  | 01/02 |  Filename  |   0  |    Mode    |   0  |
   WRQ    -----------------------------------------------
          2 bytes    2 bytes       n bytes
          ---------------------------------
   DATA  | 03    |   Block #  |    Data    |
          ---------------------------------
          2 bytes    2 bytes
          -------------------
   ACK   | 04    |   Block #  |
          --------------------
          2 bytes  2 bytes        string    1 byte
          ----------------------------------------
   ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
          ----------------------------------------

Initial Connection Protocol for reading a file

   1. Host  A  sends  a  "RRQ"  to  host  B  with  source= A's TID,
      destination= 69.

   2. Host B sends a "DATA" (with block number= 1) to host  A  with
      source= B's TID, destination= A's TID.
* */

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.attribute.UserDefinedFileAttributeView;

//as a result i believe we should only support an error packet and an OACK packet
public abstract class TFTPPacket {
    //The opcode field contains either a 1, for Read Requests, or 2, for Write Requests
    private int opcode;//type of packet
    //data of the respective packet
    private final int SIZE_PACKET_DATA = 516;
    //different types of packets, described above in notes

    private final int OACK = 6;
    private final int ERROR = 5;

    private final int ACK = 4;
    private final int DATA = 3;
    private final int WRQ = 2;
    private final int FRRQ = 8;
    private final int RRQ = 1;
    //default port from ports given by professor
    public int PORT = 26971;
    //will be defined by default as pi, but methods will be created to change it
    public String addressName = "pi.cs.oswego.edu";
    private InetAddress ADDRESS;


    //two constructors for default creation at pi server, or defined by user
    public TFTPPacket(int type) throws UnknownHostException {
        this.ADDRESS = InetAddress.getByName(addressName);
        setType(type);
    }
    public TFTPPacket(DatagramPacket packet, int type)throws UnknownHostException{
        this.PORT = packet.getPort();
        this.ADDRESS = packet.getAddress();
        setType(type);
    }

    //logic of creation of packets, etc
    public TFTPPacket createPacket(DatagramPacket packet) throws UnknownHostException {
        TFTPPacket temp;

        byte[] data = packet.getData();
        byte type = data[1];
        if(type == RRQ)
            temp = new RRQPacket(packet, type);
        else if (type == FRRQ) {
            temp = new FRRQ(packet, type);
        }
        else
            temp = null;
//        else if(type == DATA)
//            temp = new DATAPacket(packet, type);
//        else if(type == ERROR)
//            temp = new ERRORPacket(packet, type);
//        else if(type == OACK)
//            temp = new OACKPacket(packet, type);
//        else
//            temp = new ACKPacket(packet, type, 0, 0);

        return temp;
    }

    //simple setters/getters
    public void setType(int opCode){
        this.opcode = opCode;
    }
    public int getType(){
        return this.opcode;
    }
    public void setPORT(int port){
        this.PORT = port;
    }
    public int getPort(){
        return this.PORT;
    }
    public void setAddress(String address) throws UnknownHostException {
        this.addressName = address;
        this.ADDRESS = InetAddress.getByName(this.addressName);
    }
    public void setAddress(InetAddress address) throws UnknownHostException {
        this.ADDRESS = address;
    }
    public InetAddress getAddress(){
        return this.ADDRESS;
    }

}

