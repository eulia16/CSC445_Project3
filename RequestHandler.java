import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RequestHandler implements Runnable{
   DatagramPacket request;
   RRQPacket readRequestPacketToServer;
   //HashMap<String, DatagramSocket> connectedServers;
    HashMap<String, Long> lastReceivedPacketFrom = new HashMap<String, Long>();
   String[] allowedServersToConnectTo = {"rho.cs.oswego.edu", "moxie.cs.oswego.edu","wolf.cs.oswego.edu", "lambda.cs.oswego.edu"};

    public RequestHandler(DatagramPacket request, int portToUse) throws SocketException, UnknownHostException {
        this.request = request;
        System.out.println("A new request has been made");
        //attempt to establish a connection with each server, alert if one is unable to be connected to
        //connectedServers = new HashMap<>();
        DatagramSocket socket = new DatagramSocket(portToUse);
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

    }
}
