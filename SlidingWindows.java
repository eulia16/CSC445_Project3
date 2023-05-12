import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindows implements  Runnable{
    private DatagramPacket packetFromServer;
    private DatagramSocket socket;
    private int TOTAL_NUM_PACKETS;
    private int WINDOW_SIZE;
    private String fileName;
    private DatagramSocket socketToSendOACK;

    private static final String SECRET_KEY = "681111171037610197";
    private static final String SALTVALUE = "DougLeaIsMyNetworksProfessor";

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
                        byte[] decrypted = decrypt(new String(receiveBuffer)).getBytes(StandardCharsets.UTF_8);
                        if(nextAckToReceive == (int) ((decrypted[3] << 8) | (decrypted[2] & 0xFF))) {

                            socket.setSoTimeout(100);
                            count++;
                            packetNumbersReceived[(int) (decrypted[3] << 8) | (decrypted[2] & 0xFF)] = 1;
                            toAck.add((int) (decrypted[3] << 8) | (decrypted[2] & 0xFF));
                            nextAckToReceive++;


                            System.arraycopy(decrypted, 4, pictureData, 512 * (int) ((decrypted[3] << 8) | (decrypted[2] & 0xFF)), 512);
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
        System.out.println("Waiting for connection...");
        Socket clientSocket = connectionWithClient.accept();
        System.out.println("Connection established!");

        // Open the file to be transferred

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
//        clientSocket.close();
//        connectionWithClient.close();
        System.out.println("File transfer complete!");


        //socket.close();

    }


    public static String encrypt(String strToEncrypt) {
        try {
            /* Declare a byte array. */
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            /* Create factory for secret keys. */
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            /* PBEKeySpec class implements KeySpec interface. */
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            // Reruns encrypted value
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /* Decryption Method */
    public static String decrypt(String strToDecrypt)
    {
        try
        {
            /* Declare a byte array. */
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            /* Create factory for secret keys. */
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            /* PBEKeySpec class implements KeySpec interface. */
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            /* Reruns decrypted value. */
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e)
        {
            System.out.println("Error occured during decryption: " + e.toString());
        }
        return null;
    }


}
