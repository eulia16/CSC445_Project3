import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class ServerSlidingWindows implements Runnable{
    private DatagramPacket packet;
    private int port;
    private DatagramSocket transferSocket;
    private int WINDOW_SIZE = 64;

    private String fileName;
    public ServerSlidingWindows(DatagramPacket receivePacket) throws IOException {
        this.packet = receivePacket;
        int counter =0;

        //get name of file for determinging the size of the datapackets and whatnot
        int currentIndex=2, URLBytesIndex=0;
        while(receivePacket.getData()[currentIndex] != 0){
            currentIndex++; URLBytesIndex++;
        }

        // Create a new byte array of the desired size
        byte[] urlBytes = new byte[URLBytesIndex];

        // Copy the relevant bytes into the new byte array
        System.arraycopy(receivePacket.getData(), 2, urlBytes, 0, URLBytesIndex);
        //increment past zero seperator
        currentIndex++;
        System.out.println("URL path: " + new String(urlBytes));
        this.fileName = new String(urlBytes);

        while(receivePacket.getData()[counter] != -1){
            counter++;
        }
        this.port = receivePacket.getData()[((receivePacket.getData()[5]) << 8 | (receivePacket.getData()[4] & 0xFF))];
        transferSocket = new DatagramSocket(port);
        //after binding the new port to allow for data transfer, we will send an OACK back to the client

        int totalNumPackets = getNumPackets();

        transferSocket.send(new OACKPacket(new DatagramPacket(new byte[1024], 1024), 6, WINDOW_SIZE, totalNumPackets).getDatagramPacket());

    }

        public int getNumPackets() throws IOException {
            //we must determine how many packets
            Queue<DatagramPacket> packetsToSend = new LinkedList<>();
            //grab current file
            File fileToSend = new File(fileName);
            System.out.println("size of file: " + fileToSend.length());

            //get number of packets
            int totalPackets = (int)Math.ceil(fileToSend.length()/(double)512);

            return totalPackets;
        }



    @Override
    public void run() {
        System.out.println("Created new thread that will initialize new sliding windows protocol");

    }




}
