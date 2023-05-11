import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RequestHandler implements Runnable{
   private DatagramPacket request;
   private int portToUse;
   private DatagramSocket socket;
   RRQPacket readRequestPacketToServer;

   HashMap<String, byte[]> redundantDataFromServers = new HashMap<>();
   HashMap<String, DatagramPacket> retainingOACKForSlidingWindowsData = new HashMap<>();

   HashMap<String, Long> lastReceivedPacketFrom = new HashMap<String, Long>();
   String[] allowedServersToConnectTo = {"pi.cs.oswego.edu", "moxie.cs.oswego.edu"};//,"wolf.cs.oswego.edu", "lambda.cs.oswego.edu"};

    public RequestHandler(DatagramPacket request, int portToUse) throws SocketException, UnknownHostException {
        this.request = request;
        this.portToUse = portToUse;
        System.out.println("A new request has been made");
        //attempt to establish a connection with each server, alert if one is unable to be connected to
        //connectedServers = new HashMap<>();
        this.socket = new DatagramSocket(portToUse);
        //establish a connection to each server available
        //for(String s : allowedServersToConnectTo){
          //connectedServers.put(s, new DatagramSocket(portToUse));
          //set the socket timeout at 1 second, if it exceeds this, we will stop listening for packets from that server
          //connectedServers.get(s).setSoTimeout(1000);
        //}

        //we then will create an initial RRQ packet to send to the first server to let them know we need however much
        //data divided by the size of the file were attempting to download, this is just the original request
        //we have as the datagram packet
        

    }


    @Override
    public void run() {

        //we will perform the sliding windows protocol between the servers connected, we first must send
        //an RRQ to all of them and wait for responses to ensure full connectivity
        System.out.println("A request has been made, new request handler instantiated");
        try {
            sendRequestsToAllServers();
            //beginRoundRobinDataTransmission();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void beginRoundRobinDataTransmission(){
        //for each
        for(String s : redundantDataFromServers.keySet()){
            int currentIndex=2, URLBytesIndex=0;
            while(request.getData()[currentIndex] != 0){
                currentIndex++; URLBytesIndex++;
            }

            // Create a new byte array of the desired size
            byte[] urlBytes = new byte[URLBytesIndex];

            // Copy the relevant bytes into the new byte array
            System.arraycopy(request.getData(), 2, urlBytes, 0, URLBytesIndex);
            //increment past zero seperator
            currentIndex++;
            System.out.println("URL path: " + new String(urlBytes));

            //call a new sliding windows
            new SlidingWindows(retainingOACKForSlidingWindowsData.get(s), socket, new String(urlBytes)).run();
        }


    }

    //essentially multicast to servers
    public void sendRequestsToAllServers() throws IOException {
        //***AN OACK MUST BE SENT BACK FROM THE SERVERS AS A RESPONSE TO THE RRQ

        //we must add the port in the requeuest data
        int counter =0;
        while(request.getData()[counter] != -1){
            counter++;
        }
        request.getData()[counter] = (byte)(portToUse & 0xFF); // store the lower byte in the first slot
        request.getData()[counter] = (byte)((portToUse >> 8) & 0xFF); // store the upper byte in the second slot


//        request.setPort(portToUse);
        for(String s : allowedServersToConnectTo) {
            //set the address to the first server we want, this will be each server
            request.setAddress(InetAddress.getByName(s));

            //send the request
            socket.send(request);
            System.out.println("sent request");
            //if not received in 1 second, server will not be considered for data transmission
            socket.setSoTimeout(3000);
            //receive the ack
            DatagramPacket receiveACK = new DatagramPacket(new byte[1024], 1024);
            System.out.println("Awaiting OACK packet");
            socket.receive(receiveACK);
            //get the address of the server that sent an ACK
            String receivedACKFrom = new String(receiveACK.getAddress().getHostName());
            System.out.println("received packet from: " + receivedACKFrom);
            //if the packet is an ACK and the string matches the string for this loop, add it to the hashmap
            if(receiveACK.getData()[1] == 4 && receivedACKFrom.equalsIgnoreCase(s)){
                System.out.println("received OACK packet from: " + receiveACK.getAddress().getHostName());
                //the ACK must have the size of the file/the number of packets in the header info
                //set file size data,by multiplying the total num of packets w the data packet size
                redundantDataFromServers.put(s, new byte[((receiveACK.getData()[5]) << 8 | (receiveACK.getData()[4] & 0xFF))
                        * ((receiveACK.getData()[8]) << 8 | (receiveACK.getData()[7] & 0xFF))]);
                //key OACK in memory for sliding windows to know abt its data
                retainingOACKForSlidingWindowsData.put(s, receiveACK);
            }
            System.out.println("Something wrong happened");


        }

    }
}
