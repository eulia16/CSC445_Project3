import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

//this is an identical class that will run on all three/four servers, it will be indentical and run on the same port
//as the proxy server, just on different servers to allow for ease of use when transmiting the read request
public class FileTransmitterServer {
    private DatagramSocket socket;
    private final int PORT = 26974;



public FileTransmitterServer() throws IOException {
    //bind socket to port
    socket = new DatagramSocket(PORT);

    for(;;) {
        System.out.println("Awaiting request from proxy Server");
        DatagramPacket receivePacket  = new DatagramPacket(new byte[2048], 2048);
        socket.receive(receivePacket);
        System.out.println("received a packet from the proxy server: " + receivePacket.getAddress().getHostName());
        System.out.println("port:" + PORT);
          //new ServerSlidingWindows(receivePacket).run();
        OACKPacket packet = new OACKPacket(new DatagramPacket(new byte[1024], 1024, receivePacket.getAddress(), receivePacket.getPort()), 4, 64, 387);
        System.out.println("created OACK packet, sending now");
        DatagramPacket holder = packet.getDatagramPacket();
        System.out.println("address of packet to be sent: " + holder.getAddress() + ", port of packet to be sent; " + holder.getPort());
        socket.send(packet.getDatagramPacket());
        System.out.println("OACK packet sent");

    }


}

public static void main(String[] argz) throws IOException {
    new FileTransmitterServer();
}


}
