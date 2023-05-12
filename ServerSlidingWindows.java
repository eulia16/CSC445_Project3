import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ServerSlidingWindows implements Runnable{
    private DatagramPacket packet;
    private int port;
    private int totalNumberOfPackets;
    private DatagramSocket transferSocket;
    private int WINDOW_SIZE = 64;
    private DatagramSocket socketToSendOACK;

    private String fileName;
    public ServerSlidingWindows(DatagramPacket receivePacket, DatagramSocket socketToSendOACK) throws IOException {
        this.packet = receivePacket;
        this.socketToSendOACK = socketToSendOACK;
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

        this.port = ((receivePacket.getData()[counter+2]) << 8 | (receivePacket.getData()[counter+1] & 0xFF));
        transferSocket = new DatagramSocket(port);
        //after binding the new port to allow for data transfer, we will send an OACK back to the client

        int totalNumPackets = getNumPackets();

        socketToSendOACK.send(new OACKPacket(new DatagramPacket(new byte[1024], 1024, receivePacket.getAddress(), receivePacket.getPort()), 6, WINDOW_SIZE, totalNumPackets).getDatagramPacket());



    }

        public int getNumPackets() throws IOException {
            //we must determine how many packets
            Queue<DatagramPacket> packetsToSend = new LinkedList<>();
            //grab current file
            File fileToSend = new File(fileName + ".jpeg");
            System.out.println("size of file: " + fileToSend.length());

            //get number of packets
            int totalPackets = (int)Math.ceil(fileToSend.length()/(double)512);

            return totalPackets;
        }



    @Override
    public void run() {
        System.out.println("Created new thread that will initialize new sliding windows protocol");
//initialize sliding windows
        try {
            slidingWindows();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    //all functionality of sliding windows will occur in this method
    public void slidingWindows() throws IOException {
//        //call method to have datapackets of the image loaded into a list/structure of datapackets
        Queue<DatagramPacket> imagePackets = getImageTFTPPackets();
        //now that we have the number of packets necessary to send to the client in the sliding windows
        //protocol
        int windowSizeHolder = 64;
        int toACK=0;
        //while there are still packets to send(imagePackets isnt empty)
        //byte to hold ack data in datagram
        DatagramPacket tempPacket = null;
        int[] receivedAcks = new int[imagePackets.size()];
        int count=0;
        //begin time
        long beginTime = System.nanoTime();
        while(!imagePackets.isEmpty()){
            toACK=0;
            //send window size number of datapackets
            for(int i = 0; i<windowSizeHolder; ++i){
                try {
                    if(!imagePackets.isEmpty()) {
                        //pop next packet to send
                        tempPacket = imagePackets.remove();
                        byte[] temp =  tempPacket.getData();
                        //send packet
                        transferSocket.send(tempPacket);
                        //set time out
                        transferSocket.setSoTimeout(100);
                        //increment the number of acks to receive
                        toACK++;
                    }
                }
                catch(SocketTimeoutException to){
                    imagePackets.add(tempPacket);
                    toACK--;
                }
            }
            //Receive acks for the size of the window
            for(int i = 0; i < toACK; i++){
                try{
                    byte[] ack = new byte[5];
                    DatagramPacket ACK = new DatagramPacket(ack, ack.length);
                    transferSocket.receive(ACK);
                    byte[] holder = ACK.getData();
                    //added code everything else worked before

                    transferSocket.setSoTimeout(5000);
                    receivedAcks[((holder[3] << 8) | (holder[2] & 0xFF))] = 1;
                    count++;
                }catch(SocketTimeoutException to){
                    System.out.println("There was a timeout");
                }
                if(count == totalNumberOfPackets) {
                    //allow socket to listen forever as it is being reset
                    transferSocket.setSoTimeout(0);
                    break;
                }
            }
        }

        ServerSocket serverSocket = new ServerSocket(8000);

        // Wait for a client to connect
        System.out.println("Waiting for connection...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Connection established!");

        // Open the file to be transferred
        File file = new File(fileName+ ".jpeg");
        FileInputStream fileInputStream = new FileInputStream(file);

        // Get the output stream of the client socket
        OutputStream outputStream = clientSocket.getOutputStream();

        // Transfer the file contents to the client
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();

        // Close the streams and sockets
        fileInputStream.close();
        outputStream.close();
        clientSocket.close();
        serverSocket.close();
        System.out.println("File transfer complete!");
        //end time
       System.out.println("finished Sliding windows");
        //just print it to the screen I'll add the values to a graph


    }



    //determine whether or not this method has to return a list of byte[] or datagrams, rn i think
    //it should be changed to bytes but im not sure, i maybe able to edit this method
    //so the header info for a data packet is already appended onto the front by instantiating
    //a datapacket object every time, wait to see if this is a good way to to do that
    public Queue<DatagramPacket> getImageTFTPPackets() throws IOException {
        //we must determine how many packets
        Queue<DatagramPacket> packetsToSend = new LinkedList<>();
        //grab current file

        File fileToSend = new File(this.fileName + ".jpeg");
        System.out.println("size of file: " + fileToSend.length());
        //get number of packets
        int totalPackets = (int)Math.ceil(fileToSend.length()/(double)512);

        //set total number of packets
        this.totalNumberOfPackets = totalPackets;

//        int counter =0;
//        while(packet.getData()[counter] != -1){
//            counter++;
//        }
//
//        int PORT_TO_USE = ((packet.getData()[counter +2] << 8) | (packet.getData()[counter +1] & 0xFF));
//        this.socket = new DatagramSocket(PORT_TO_USE);


        InputStream dis = new FileInputStream(fileToSend);
        //need to change this so the last packet gets a byte array smaller than 512 length, so the
        //client knows its the last packet
        byte[] fileInfo = new byte[(int) fileToSend.length()];
        dis.read(fileInfo);
        //while((bytesRead = dis.read(buffer)) != -1){
        for(int i=0; i<totalPackets; ++i){
            byte[] temp;
            //if at last packet, only copy data that matters
            if(i == totalPackets-1) {
                int remainingData = fileInfo.length - (i * 512);
                byte[] remainingDataByte = Arrays.copyOfRange(fileInfo, (i * remainingData), (i * remainingData) + remainingData);


                System.out.println("remaining data length: " + remainingDataByte.length);
                DatagramPacket lastPacket = new DatagramPacket(remainingDataByte, remainingDataByte.length ,packet.getAddress(), port);
                DATAPacket dataPacket = new DATAPacket(lastPacket, 3, packetsToSend.size());
                DatagramPacket lastDataPacket = dataPacket.getDatagramPacket();
                packetsToSend.add(lastDataPacket);
                break;
            }
            temp = Arrays.copyOfRange(fileInfo, (i * 512), (i * 512) + 512);




            //make temp datagram
            DatagramPacket datagram = new DatagramPacket(temp, temp.length, packet.getAddress(), port);
            //get block number(length of array list holding all packets)
            int blockNum = packetsToSend.size();
            //instantiate new datapacket object, w/ current block number
            DATAPacket dataPacket = new DATAPacket(datagram, 3, blockNum);
            //get datagram packet to add to list
            DatagramPacket newDatagram = dataPacket.getDatagramPacket();
            //for testing putposes we will grab the block number to ensure accuracy
            byte[] receiveByte;
            receiveByte = newDatagram.getData();
            int newNumber = (receiveByte[3] << 8) | (receiveByte[2] & 0xFF);
            System.out.println("block number from the data in the packet: " + newNumber );
            //add packet to packets to send array list
            packetsToSend.add(newDatagram);

        }



        return packetsToSend;

    }




}
