//special file read request packet to be sent to the server specifically meant to request the available files for
//download on the proxy server

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
public class FRRQ extends TFTPPacket {
    //url to be sent(filename as specified )
    private String URL = "";
    //read request packets are of opcode type 1(check abstract TFTP class for all opcodes and associations)
    private final int RRQ = 1;
    //the mode is unnecessary for this project, so we will make it a null field
    private final String mode = "octet";
    private DatagramPacket passedPacket;
    private byte[] allRRQHeaderInfo;

    //constructor which allows URL/file name to be changed
    public FRRQ(DatagramPacket packet, int type) throws UnknownHostException {
        super(packet, type);
        passedPacket = packet;
        byte[] data = passedPacket.getData();

        data[0] = 0;
        //set opcode(packet type)
        data[1] = (byte)this.getType();


        allRRQHeaderInfo = data;

    }



    public String getMode(){ return this.mode; }
    //method to obtain all info pertinant to RRQ packet to then be added to a datagram packet and sent for read request
    public byte[] getAllRRQHeaderInfo(){ return this.allRRQHeaderInfo; }
    public DatagramPacket getPassedPacket() {
        return passedPacket;
    }
    public DatagramPacket getFRRQPacket(){
        //change current passed data to new header RRQ data
        this.passedPacket.setData(this.allRRQHeaderInfo);
        //return the passedPacket
        return this.passedPacket;
    }

}
