import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

//this is an identical class that will run on all three/four servers, it will be indentical and run on the same port
//as the proxy server, just on different servers to allow for ease of use when transmiting the read request
public class FileTransmitterServer {
    private DatagramSocket socket;
    private final int PORT = 26974;

    //Doug Lea in decimal ASCII equivalent
    private static SecretKey SECRET_KEY;
    private static String password = "DougLea";
    private static final String SALTVALUE = "DougLeaIsMyNetworksProfessor";



public FileTransmitterServer() throws IOException {
    //bind socket to port
    socket = new DatagramSocket(PORT);

    for(;;) {

        System.out.println("Awaiting request from proxy Server");
        DatagramPacket receivePacket  = new DatagramPacket(new byte[2048], 2048);
        socket.receive(receivePacket);
        System.out.println("received a packet from the proxy server: " + receivePacket.getAddress().getHostName());
        System.out.println("port:" + PORT);
        new ServerSlidingWindows(receivePacket, socket).run();
//        OACKPacket packet = new OACKPacket(new DatagramPacket(new byte[1024], 1024, receivePacket.getAddress(), receivePacket.getPort()), 4, 64, 387);
//        System.out.println("created OACK packet, sending now");
//        DatagramPacket holder = packet.getDatagramPacket();
//        System.out.println("address of packet to be sent: " + holder.getAddress() + ", port of packet to be sent; " + holder.getPort());
//        socket.send(packet.getDatagramPacket());
//        System.out.println("OACK packet sent");
//
//
    }


}

public static void main(String[] argz) throws IOException {
    new FileTransmitterServer();
}

private static SecretKey getKeyFromPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
    SECRET_KEY = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    return SECRET_KEY;
}

    public static byte[] encrypt(Key key, byte[] content) {
        Cipher cipher;
        byte[] encrypted = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]), new SecureRandom());
            encrypted = cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        return encrypted;
    }

    /* Decryption Method */
    public static byte[] decrypt(Key key, byte[] textCrypt)
    {
        Cipher cipher;
        byte[] decrypted = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]), new SecureRandom());
            decrypted = cipher.doFinal(textCrypt);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        return decrypted;
    }
}
