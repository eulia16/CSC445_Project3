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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
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
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            e.printStackTrace();
        }
        return encrypted;

//        try {
//            /* Declare a byte array. */
//            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//            IvParameterSpec ivspec = new IvParameterSpec(iv);
//            /* Create factory for secret keys. */
//            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//            /* PBEKeySpec class implements KeySpec interface. */
//            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
//            /* Create cipher instance. */
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
//            // Reruns encrypted value
//            return Base64.getEncoder()
//                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
//        } catch (InvalidAlgorithmParameterException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchPaddingException e) {
//            throw new RuntimeException(e);
//        } catch (IllegalBlockSizeException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        } catch (InvalidKeySpecException e) {
//            throw new RuntimeException(e);
//        } catch (BadPaddingException e) {
//            throw new RuntimeException(e);
//        } catch (InvalidKeyException e) {
//            throw new RuntimeException(e);
//        }
    }

    /* Decryption Method */
    public static byte[] decrypt(Key key, byte[] textCrypt)
    {
        Cipher cipher;
        byte[] decrypted = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = cipher.doFinal(textCrypt);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return decrypted;
//        try
//        {
//            /* Declare a byte array. */
//            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//            IvParameterSpec ivspec = new IvParameterSpec(iv);
//            /* Create factory for secret keys. */
//            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//            /* PBEKeySpec class implements KeySpec interface. */
//            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
//            /* Reruns decrypted value. */
//            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
//        }
//        catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e)
//        {
//            System.out.println("Error occured during decryption: " + e.toString());
//        }
//        return null;
    }

}
