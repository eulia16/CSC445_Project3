//this is an identical class that will run on all thre/four servers, it will be indentical and run on the same port
//as the proxy server, just on different servers to allow for ease of use when transmiting the read request
import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.*;
//don't forget we have ports 26970-26979

//this class will serve as the proxy server, waiting for a connection, receiving a URL, and then hitting an
//http endpoint that lives there
public class FileTransmitterServer {
    private DatagramSocket socket;
    private InetAddress address;
    private int PORT = 26974;

    private boolean dropPackets = false;
    private int windowSize = 30;
    private int ENCRYPT_VALUE=0;

    private InetAddress clientAddress;
    //cache to keep already searched urls' file's in memory
    private HashMap<String, File> cache = new HashMap<>();

    private String currentRequestedURl = "";
    private final int packetSize = 512;
    private int totalNumberOfPackets =0;


    //constructor, base server assumes false booleans and assumed port 26971
    public FileTransmitterServer(int port, boolean dropPackets){
        this.PORT = port;
        this.dropPackets = dropPackets;
    }
    public FileTransmitterServer(boolean dropPackets){
        this.dropPackets = dropPackets;
    }



    //listen method
    public void listen() throws IOException, URISyntaxException, InterruptedException {
        socket = makeConnection();
        byte[] packetBytes = new byte[1024];
        String urlString;

        //the server will continue to listen for a URL until it received one
        while(true) {
            System.out.println("Awaiting URL From Client...");
            DatagramPacket urlPacket = new DatagramPacket(packetBytes, packetBytes.length);
            //receive packet
            socket.receive(urlPacket);
            //grab data
            packetBytes = urlPacket.getData();

            clientAddress = urlPacket.getAddress();


            int currentIndex=2, URLBytesIndex=0;
            while(packetBytes[currentIndex] != 0){
                currentIndex++; URLBytesIndex++;
            }

            // Create a new byte array of the desired size
            byte[] urlBytes = new byte[URLBytesIndex];

            // Copy the relevant bytes into the new byte array
            System.arraycopy(packetBytes, 2, urlBytes, 0, URLBytesIndex);
            //increment past zero seperator
            currentIndex++;
            System.out.println("URL path: " + new String(urlBytes));
            urlString = new String(urlBytes);
            currentRequestedURl = urlString;
            //now that we have the fileName/URL, we skip the next byte because its a seperator/0
            //the value after that 0 should be mode, and were only doing octet mode, but we can
            //continue to grab the mode for security purposes
            byte[] mode = new byte[5];
            int currentModeByte=0;
            //this is not only searching for a 0 terminating character because the last byte is acting
            //like a stop for the mode, but also if the client wants to drop 1 percent of packets or not,
            //where 0 is do not drop 1 percent, and 1 is drop 1 percent of packets
            while(packetBytes[currentIndex] != 0 && packetBytes[currentIndex] != 1){
                mode[currentModeByte] = packetBytes[currentIndex];
                currentIndex++;currentModeByte++;
            }
            //if value if the next byte is 1, the client wants 1 percent of packets to be dropped
            if(packetBytes[currentIndex] == 1)
                dropPackets = true;
            currentIndex++;
            //we then will grab the encryption key for XORing w the key
            ENCRYPT_VALUE = packetBytes[currentIndex];

            //can add some control flow s/a if mode!=octet, quit program
            String modeToString = new String(mode);

            if(new String(mode).equalsIgnoreCase("octet")){
                //next step is to create a class that returns a file and then this class will store
                //the file that is currently in memory and cache it in a hashmap that stores the URL
                //sent in the RRQ packet as the key, and the bufferedImage as the value

                //as per the cache, if the URL already has been searched, it will proceed to send
                //rather than hitting the endpoint for the image it already has
                if(cache.containsKey(urlString)){
                    //begin sending data sequence packets
                    System.out.println("file already cached, beginning sending sequence");
                    File tempFile = cache.get(urlString);
                    if(sendOACK()) {
                        System.out.println("sent OACK and received ACK from client, beginning sliding windows protocol");


                        //starting sliding windows, if the user did not want 1 percent of packets to be simulated/dropped
                        if(!dropPackets)
                            slidingWindows();
                        else//else do the sliding windows w/ 1 percent drop
                            slidingWindowsWithDrop();


                    }
                    else{
                        System.out.println("Error occured during OACK transmission, exiting protocol...");
                        //just for now
                        socket.close();

                        System.exit(0);

                    }

                }
                //else hit the endpoint to get the file
                else {
                    String destinationFile = generateRandomFileName();
                    PageGrabber pageGrabber = new PageGrabber(destinationFile);
                    System.out.println("URL to search: " + urlString);
                    File returnedImage = pageGrabber.obtainImageFromURL(urlString);
                    //enter file in cache
                    cache.put(urlString, returnedImage);
                    System.out.println("Successfully grabbed image");
                    File tempFile = cache.get(urlString);
                    System.out.println("if file exists: " + tempFile.exists() + ", name: " + tempFile.getName());
                    //now that we have created the caching functionality and grabbing the file either from the URL
                    //or from the cache, we will now start to design the data, error, and ack packets

                    //we now send an OACK and receive an ACK back from the client, once this occurs
                    //we will begin the sliding windows stuff

                    if(sendOACK()) {
                        System.out.println("sent OACK and received ACK from client, beginning sliding windows protocol");


                        //starting sliding windows, if the user did not want 1 percent of packets to be simulated/dropped
                        if(!dropPackets)
                            slidingWindows();
                        else//else do the sliding windows w/ 1 percent drop
                            slidingWindowsWithDrop();


                    }
                    else{
                        System.out.println("Error occured during OACK transmission, exiting protocol...");
                        //just for now
                        socket.close();

                        System.exit(0);

                    }

                }

            }
            else {
                System.out.println("Program only supports octet mode, quitting now");
                //just for now
                socket.close();

                System.exit(0);
            }


        }

    }


    public void slidingWindowsWithDrop() throws IOException, InterruptedException {

        //we still need to grab the packets we want to send of the image
        Queue<DatagramPacket> imagePackets = getImageTFTPPackets();

        //to make this randomly drop 1 percent of packets, we will create a random value for each packet
        //that is about to be sent, and if the packet block number matches one of the numebrs generated in
        //the list of random numbers generated, then 'drop' the packet, and resend it(timeout)
        //we will make a function to do this and call it to retrieve the list

        List<Integer> randomPacketNumbersToDrop = generateRandomIntegersToDrop();

        System.out.println("random packets: " + randomPacketNumbersToDrop);
        //otherwise this functionality is the same as the normal sliding windows
        System.out.println("number of packets to send to client outside function: " + imagePackets.size());

        int windowSizeHolder = windowSize;
        int toACK=0;
        //while there are still packets to send(imagePackets isnt empty)
        DatagramPacket tempPacket = null;
        int[] receivedAcks = new int[imagePackets.size()];
        int count=0;


        //start timer
        long beginTime = System.nanoTime();

        while(!imagePackets.isEmpty()){
            toACK=0;


            //send window size number of datapackets
            for(int i = 0; i<windowSizeHolder; ++i){

                try {
                    if (!imagePackets.isEmpty()) {
                        //pop next packet to send

                        tempPacket = imagePackets.remove();

                        byte[] temp = tempPacket.getData();
                        temp = XOR(temp, ENCRYPT_VALUE);

                        //before sending the packet, check to see if it is a value in the random drop packets frame
                        //we then will be able to add it to the back of the queue and resend later
                        if (randomPacketNumbersToDrop.contains(((temp[3] << 8) | (temp[2] & 0xFF)))) {
                            randomPacketNumbersToDrop.remove((Object) (((temp[3] << 8) | (temp[2] & 0xFF))));
                            //simulate packet drop
                            System.out.println("simulating drop, waiting 100 ms");
                            Thread.sleep(100);
                            socket.send(tempPacket);
                            socket.setSoTimeout(100);
                            toACK++;
                            continue;

                        }

                        System.out.println("Currently sending packet: " + ((temp[3] << 8) | (temp[2] & 0xFF)));
                        //send packet
                        socket.send(tempPacket);
                        //set time out
                        socket.setSoTimeout(100);

                        //increment the number of acks to receive
                        toACK++;

                    }
                } catch (SocketTimeoutException to) {
                    tempPacket = imagePackets.remove();
                    socket.send(tempPacket);
                }


            }

            for(int i = 0; i < toACK; i++){
                try{
                    byte[]ack = new byte[5];
                    DatagramPacket ACK = new DatagramPacket(ack, ack.length);
                    socket.receive(ACK);
                    byte[] holder = ACK.getData();
                    holder = XOR(holder, ENCRYPT_VALUE);
                    socket.setSoTimeout(100);
                    System.out.println("count: " + count);
                    receivedAcks[((holder[3] << 8) | (holder[2] & 0xFF))] = 1;
                    count++;


                }catch(SocketTimeoutException to){
                }
                if(count == totalNumberOfPackets) {
                    //allow socket to listen forever as it is being reset
                    socket.setSoTimeout(0);
                    break;
                }

            }


        }

        double timeInSecs = ((double) (System.nanoTime() - beginTime))/1E9;
        System.out.println("Time: " + timeInSecs);
        //just print it to the screen I'll add the values to a graph
        File fileToSend = cache.get(currentRequestedURl);
        long totalSize = fileToSend.length();
        double throughput = ((totalSize * 8)/1E6)/timeInSecs;
        System.out.println("all bytes: " + totalSize + ", throughput: " + throughput + " Mb/s");
        socket.close();
    }



    //all functionality of sliding windows will occur in this method
    public void slidingWindows() throws IOException {
        //call method to have datapackets of the image loaded into a list/structure of datapackets
        Queue<DatagramPacket> imagePackets = getImageTFTPPackets();
        //now that we have the number of packets necessary to send to the client in the sliding windows
        //protocol
        int windowSizeHolder = windowSize;
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
                        socket.send(tempPacket);
                        //set time out
                        socket.setSoTimeout(100);
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
                    socket.receive(ACK);
                    byte[] holder = ACK.getData();
                    //added code everything else worked before
                    holder = XOR(holder, ENCRYPT_VALUE);

                    socket.setSoTimeout(5000);
                    receivedAcks[((holder[3] << 8) | (holder[2] & 0xFF))] = 1;
                    count++;
                }catch(SocketTimeoutException to){
                    System.out.println("There was a timeout");
                }
                if(count == totalNumberOfPackets) {
                    //allow socket to listen forever as it is being reset
                    socket.setSoTimeout(0);
                    break;
                }
            }
        }
        //end time
        double timeInSecs = ((double) (System.nanoTime() - beginTime))/1E9;
        System.out.println("Time: " + timeInSecs);
        //just print it to the screen I'll add the values to a graph
        File fileToSend = cache.get(currentRequestedURl);
        long totalSize = fileToSend.length();
        double throughput = ((totalSize * 8)/1E6)/timeInSecs;
        System.out.println("all bytes: " + totalSize + ", throughput: " + throughput + " Mb/s");

    }



    public boolean sendOACK() throws IOException {
        byte[] tempByte = new byte[512];
        File fileToSend = cache.get(currentRequestedURl);
        //create OACK packet to send to client
        int totalPackets = (int) Math.ceil(fileToSend.length()/(double) 512);
        //create OACK packet to send to client
        OACKPacket oackPacket = new OACKPacket(new DatagramPacket(tempByte, tempByte.length, clientAddress, PORT), 06, windowSize, totalPackets);
        System.out.println("Sending OACK packet");
        socket.send(oackPacket.getDatagramPacket());

        //then receive individual ack for OACK confirmation, before sending actual data
        byte[] receiveByte = new byte[512];
        DatagramPacket receiveACK = new DatagramPacket(receiveByte, receiveByte.length);
        System.out.println("receiving ACK packet");
        socket.receive(receiveACK);
        receiveByte = receiveACK.getData();
        receiveByte = XOR(receiveByte, ENCRYPT_VALUE);
        int type = receiveByte[1];
        int result = (receiveByte[3] << 8) | (receiveByte[2] & 0xFF);
        System.out.println("type: " + type + ", block number: " + result);

        //if the data from the receiveByte is equal to the received packet, something went wrong, return false
        return type == 4;
    }


    //determine whether or not this method has to return a list of byte[] or datagrams, rn i think
    //it should be changed to bytes but im not sure, i maybe able to edit this method
    //so the header info for a data packet is already appended onto the front by instantiating
    //a datapacket object every time, wait to see if this is a good way to to do that
    public Queue<DatagramPacket> getImageTFTPPackets() throws IOException {
        //we must determine how many packets
        Queue<DatagramPacket> packetsToSend = new LinkedList<>();
        //grab current file
        File fileToSend = cache.get(currentRequestedURl);
        System.out.println("size of file: " + fileToSend.length());

        //get number of packets
        int totalPackets = (int)Math.ceil(fileToSend.length()/(double)512);

        //set total number of packets
        totalNumberOfPackets = totalPackets;

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

                //added encryption code, everything worked before this
                remainingDataByte = XOR(remainingDataByte, ENCRYPT_VALUE);

                System.out.println("remaining data length: " + remainingDataByte.length);
                DatagramPacket lastPacket = new DatagramPacket(remainingDataByte, remainingDataByte.length ,clientAddress, PORT);
                DATAPacket dataPacket = new DATAPacket(lastPacket, 3, packetsToSend.size(), ENCRYPT_VALUE);
                DatagramPacket lastDataPacket = dataPacket.getDatagramPacket();
                packetsToSend.add(lastDataPacket);
                break;
            }
            temp = Arrays.copyOfRange(fileInfo, (i * 512), (i * 512) + 512);


            //added encryption code, everything worked before this
            temp = XOR(temp, ENCRYPT_VALUE);


            //make temp datagram
            DatagramPacket datagram = new DatagramPacket(temp, temp.length, clientAddress, PORT);
            //get block number(length of array list holding all packets)
            int blockNum = packetsToSend.size();
            //instantiate new datapacket object, w/ current block number
            DATAPacket dataPacket = new DATAPacket(datagram, 3, blockNum, ENCRYPT_VALUE);
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


    public DatagramSocket makeConnection() throws SocketException {
        return new DatagramSocket(PORT);
    }

    public String generateRandomFileName() {
        int size = 1_000_000_000;
        Random random = new Random();
        int value = random.nextInt(size);
        return String.valueOf(value);
    }



    public List<Integer> generateRandomIntegersToDrop(){
        List<Integer> integersToDrop = new ArrayList<>();
        Random random = new Random();
        //for each '100' generate one random number to drop
        for(int i =0; i < totalNumberOfPackets / 100; ++i){
            int randomNumber = random.nextInt(((totalNumberOfPackets-1) - 0));
            integersToDrop.add(randomNumber);
        }

        return integersToDrop;
    }

    public byte[] XOR(byte[] values, int key) {
        byte[] returnBytes = new byte[values.length];
        for (int i = 0; i < values.length; ++i) {
            returnBytes[i] = (byte) (values[i] ^ key);
        }
        return returnBytes;

    }

}



