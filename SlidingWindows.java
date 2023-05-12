import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindows implements  Runnable{
    private DatagramPacket packetFromServer;
    private DatagramSocket socket;
    private int TOTAL_NUM_PACKETS;
    private int WINDOW_SIZE;
    private String fileName;
    private DatagramSocket socketToSendOACK;
    public SlidingWindows(DatagramPacket packetFromServer, DatagramSocket socket, DatagramSocket socketToSendOACK, String fileName){

        this.packetFromServer = packetFromServer;
        this.socket = socket;
        this.socketToSendOACK = socketToSendOACK;
        this.fileName = fileName;
        //get the number of packets
        this.TOTAL_NUM_PACKETS = ((packetFromServer.getData()[8]) << 8 | (packetFromServer.getData()[7] & 0xFF));
        System.out.print("total num of packets: " + TOTAL_NUM_PACKETS);
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
//        System.out.println("Inside sliding windows");
//        Socket tcpSocketToServer = new Socket(packetFromServer.getAddress(), socketToSendOACK.getPort());
//
//        File tempFile = new File("downloaded"+ fileName + ".jpeg");
//
//        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
//        // Get the input stream of the server socket
//        InputStream inputStream = tcpSocketToServer.getInputStream();
//
//        // Transfer the file contents from the server
//        byte[] buffer = new byte[1024];
//        int bytesRead = 0;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            fileOutputStream.write(buffer, 0, bytesRead);
//        }
//
//        // Close the streams and socket
//        fileOutputStream.close();
//        inputStream.close();
//        tcpSocketToServer.close();
//        System.out.println("File transfer complete!");



//
//        //continue to receive data and send acks for however many packets there are
        while( count < TOTAL_NUM_PACKETS - 1){
            //receive the datagram
            byte[] receiveBuffer = new byte[516];
            //receive datagram using buffer
            DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);


            for(int i = 0; i< WINDOW_SIZE && count < TOTAL_NUM_PACKETS; ++i){
                try{
                    if(count < TOTAL_NUM_PACKETS ) {
                        socket.receive(receiveDatagram);
                        System.out.println("received packet, block number: " + (int) ((receiveBuffer[3] << 8) | (receiveBuffer[2] & 0xFF)));
                        System.out.println("address recieved packet from: " + receiveDatagram.getAddress().getHostAddress());
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
        File file = new File("new Download"+ fileName + ".jpeg");
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bos.write(pictureData);
        bos.close();

        ServerSocket  connectionWithClient = new ServerSocket(30000);

        // Wait for a client to connect
//        System.out.println("Waiting for connection...");
//        Socket clientSocket = serverSocket.accept();
//        System.out.println("Connection established!");
//
//        // Open the file to be transferred
//        File file = new File("new Download"+ fileName + ".jpeg");
//        FileInputStream fileInputStream = new FileInputStream(file);
//
//        // Get the output stream of the client socket
//        OutputStream outputStream = clientSocket.getOutputStream();
//
//        // Transfer the file contents to the client
//        byte[] buffer = new byte[1024];
//        int bytesRead = 0;
//        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//        outputStream.flush();
//
//        // Close the streams and sockets
//        fileInputStream.close();
//        outputStream.close();
//        clientSocket.close();
//        serverSocket.close();
//        System.out.println("File transfer complete!");


        socket.close();

    }

}
