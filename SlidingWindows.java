import javax.xml.crypto.Data;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindows implements  Runnable{
    private DatagramPacket packetFromServer;
    private DatagramSocket socket;
    private int TOTAL_NUM_PACKETS;
    private int WINDOW_SIZE;
    private String fileName;

    public SlidingWindows(DatagramPacket packetFromServer, DatagramSocket socket, String fileName){

        this.packetFromServer = packetFromServer;
        this.socket = socket;
        this.fileName = fileName;
        //get the number of packets
        this.TOTAL_NUM_PACKETS = ((packetFromServer.getData()[8]) << 8 | (packetFromServer.getData()[7] & 0xFF));
        //get the window size
        this.WINDOW_SIZE = ((packetFromServer.getData()[5]) << 8 | (packetFromServer.getData()[4] & 0xFF));
    }


    @Override
    public void run() {

        System.out.println("A new sliding windows thread has been created");
        try {
            slidingWindows();
        } catch (IOException e) {
            System.out.println("Error downloading file, potential server crash, exiting this thread...");
            return;
            //throw new RuntimeException(e);
        }

    }


    public void slidingWindows() throws IOException {
        //begin receiving packets for sliding windows
        byte[] pictureData = new byte[(TOTAL_NUM_PACKETS+1) * 512];
        int numDrops = 0;
        int count=0;

        int[] packetNumbersReceived = new int[TOTAL_NUM_PACKETS];
        Queue<Integer> toAck = new LinkedList<>();
        int nextAckToReceive =0;

        //continue to receive data and send acks for however many packets there are
        while( count < TOTAL_NUM_PACKETS - 1){
            //receive the datagram
            byte[] receiveBuffer = new byte[516];
            //receive datagram using buffer
            DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);


            for(int i = 0; i< WINDOW_SIZE && count < TOTAL_NUM_PACKETS; ++i){
                try{
                    if(count < TOTAL_NUM_PACKETS ) {
                        socket.receive(receiveDatagram);
                        receiveBuffer = receiveDatagram.getData();


                        //ensure you received the next ack you expected
                        //System.out.println("Next Ack Expected: " + nextAckToReceive + ", next ack actually received: " + (int) ((receiveBuffer[3] << 8) | (receiveBuffer[2] & 0xFF)));
                        if(nextAckToReceive == (int) ((receiveBuffer[3] << 8) | (receiveBuffer[2] & 0xFF))) {

                            socket.setSoTimeout(100);
                            count++;
                            packetNumbersReceived[(int) (receiveBuffer[3] << 8) | (receiveBuffer[2] & 0xFF)] = 1;
                            toAck.add((int) (receiveBuffer[3] << 8) | (receiveBuffer[2] & 0xFF));
                            nextAckToReceive++;


                            System.arraycopy(receiveBuffer, 4, pictureData, 512 * (int) ((receiveBuffer[3] << 8) | (receiveBuffer[2] & 0xFF)), 512);
                        }
                        else{
                            //slide window back if not receiving next expected packet
                            i--;
                            throw new SocketTimeoutException();
                        }
                    }
                    else
                        break;
                }
                catch(SocketTimeoutException e){
                    System.out.println(e);
                    numDrops++;
                    if(numDrops > 20){
                        System.exit(0);
                    }
                }
            }

            while(!toAck.isEmpty()) {
                //create datagram holder
                DatagramPacket ackData = new DatagramPacket(new byte[5], 5, packetFromServer.getAddress(), packetFromServer.getPort());
                int temp = toAck.remove();
                //create ack packet object
                ACKPacket ack = new ACKPacket(ackData, 4, temp);
                //set ack's block number from the data held
                socket.send(ack.getDatagramPacket());
            }

        }


        //persist a new file with the name of the server it was downloaded from, as well as the name of the file
        File file = new File(packetFromServer.getAddress().getHostName() + fileName + ".jpg");
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bos.write(pictureData);
        bos.close();

        socket.close();

    }

}
