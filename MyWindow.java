import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.crypto.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
//security imports
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MyWindow extends JFrame {

    private ArrayList<File> downloadedFiles = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    private int currentDownloadIndex=0;

    private HashMap<String, byte[]> globalStorageOfFiles = new HashMap<>();

    private int MAX_NUM_THREADS = 15;

    //Doug Lea in decimal ASCII equivalent
    private static final String SECRET_KEY = "681111171037610197";
    private static final String SALTVALUE = "DougLeaIsMyNetworksProfessor";

    private String proxyString="rho.cs.oswego.edu";

    private InetAddress address = InetAddress.getByName(proxyString);

    private int currentRowSelected;

    private JList fileHolders;

    private ArrayList<Integer> keepTrackOfRowCountForConcurrency = new ArrayList<Integer>();

    String currentSelectedFile = null;

    private int PORT=26974;
    private JLabel projectNameLabel;
    private JLabel proxyServerLabel;
    private JPanel tablePanel;
    private JButton uploadDownloadButton;
    private JButton findFileButton;
    private JTextField fileNameTextField;
    private JButton uploadFileButton;
    private final int MAX_FILES_FROM_SERVER = 15;

    private JTable table;

    private DatagramSocket proxyServerConnectedTo;

    private JToggleButton uploadOrDownloadOption;

    private ArrayList<SwingWorker> currentThreadsJProgressBars = new ArrayList<>();

    private JLabel availableToDownload = new JLabel("Files Available" + "\n" + "For Download");

    //contrastButton = new JToggleButton(new ImageIcon(unclickedButtonImage));
    //setButtonIconCircle(contrastButton);
    //contrastButton.setPressedIcon(new ImageIcon(unclickedButtonImage));

    public static void main(String[] args) throws IOException {
        MyWindow window = new MyWindow();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }

    public MyWindow() throws IOException {
        setTitle("My Window");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));

        // Create the components
        projectNameLabel = new JLabel("Baby Torrent");

        String proxyConnectedTo = "pi.cs.oswego.edu";
        proxyServerLabel = new JLabel("Proxy Server Connected To: " + proxyConnectedTo);
        tablePanel = new JPanel();
        //tablePanel.setBackground(Color.BLACK);
        uploadDownloadButton = new JButton("Download");
        findFileButton = new JButton("Find File");
        fileNameTextField = new JTextField("File Name");
        uploadFileButton = new JButton("Download");
        proxyServerConnectedTo = new DatagramSocket(PORT);

        System.out.println("proxyServer: " + proxyServerConnectedTo.getLocalPort());
        uploadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
//                    we need to capture the file that the user wants to download, so we will craete a read request
//                    packet and capture the datagram packet from there to then allow the
                    DatagramPacket temp = new DatagramPacket(new byte[2048], 2048, address, PORT);
                    //if the
                    if(currentSelectedFile == null) {
                        JOptionPane.showMessageDialog(null, "You must select a file before attempting to download it");
                        return;
                    }
                    RRQPacket packetToSend = new RRQPacket(temp, 01, currentSelectedFile);
//                    somehow grab the name of the file that has been selected
//                    and send that as the bytes for the RRQ packet
//                    send a request to the proxyServer with a request for this machine to start downloading
                    proxyServerConnectedTo.send(packetToSend.getPassedPacket());
                    //we then append a new fileDownload guy to the current downloaded files
                    Object[] data = {currentSelectedFile, 0, "Delete"};
                    ((DefaultTableModel)table.getModel()).addRow(data);

                    //add name of file to the text field
                    fileNameTextField.setText(currentSelectedFile.toString());

                    //after sending the packet, we will need to break off and create a new class to handle the
                    //downloading and uploading, as well as popping a new file to the jlist and keeping the progress
                    //bar up to date

                    //we will use a swing worker to handle each time the button is pressed, to break off a new
                    //swing worker thread, and begin the sliding windows download
                       //currentDownloadIndex++;
                    int i = table.getRowCount() ;
                    keepTrackOfRowCountForConcurrency.add(currentDownloadIndex, table.getRowCount() -1);
                    doTheSwingThing(keepTrackOfRowCountForConcurrency.get(currentDownloadIndex));


                    System.out.println("Starting tcp socket connection");
                    Socket tcpSocketToServer = new Socket(InetAddress.getByName("rho.cs.oswego.edu"), 30000);
                    System.out.println("Establishged connection vie port 8000:");


                    File tempFile = new File("downloaded"+ currentSelectedFile + ".jpeg");

                    FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                    // Get the input stream of the server socket
                    InputStream inputStream = tcpSocketToServer.getInputStream();

                    // Transfer the file contents from the server
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }

                    // Close the streams and socket
                    fileOutputStream.close();
                    inputStream.close();
                    tcpSocketToServer.close();
                    System.out.println("File transfer complete!");





                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Set the layout manager for the frame
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add the project name label to the top left corner
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(projectNameLabel, gbc);

        // Add the proxy server label to the top right corner
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;
        add(proxyServerLabel, gbc);

        // Add the table panel to the center
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 110, 10, 10);
        TestFile testFile = new TestFile(keepTrackOfRowCountForConcurrency);

        this.table = testFile.getTable();
        add(this.table, gbc);

        //keepTrackOfRowCountForConcurrency.add(0, table.getRowCount()-1);

        //temp for now

        String week[]= { "Monday","Tuesday","Wednesday",
                "Thursday","Friday","Saturday","Sunday"};
        fileHolders= new JList(week);
        fileHolders.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                    if (!e.getValueIsAdjusting()) {
                        JList source = (JList) e.getSource();
                        if(source.getSelectedValue() == null)
                            return;
                        currentSelectedFile = source.getSelectedValue().toString();
                        currentRowSelected = source.getSelectedIndex();
                    }

            }
        });

        fileHolders.setPreferredSize(new Dimension(100,100));

        //we need to send a request for a return of available files to download from the proxy server
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = .3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 110, 10, 10);
        add(fileHolders, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
         //gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 10, 10);
        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("This will send a specific type of request packet to the proxy server and retireve the list of available files to download");
                String[] allCurrentFiles = sendFRRQToGetPackets();
                 //String week[]= { "Fuck","This","Shit"};
                fileHolders.setListData(allCurrentFiles);

                String blep = "HOLOAAAAAAAAAAAA";
                byte[] testString = blep.getBytes();
                String s = new String(testString);
                String encryptedval = encrypt(s);
                /* Call the decrypt() method and store result of decryption. */
                System.out.println("bytes of decrypted bytes: " + decrypt(encryptedval));
                String decryptedval = decrypt(encryptedval);

                System.out.println("Original value: " + testString);
                System.out.println("Encrypted value: " + encryptedval);
                System.out.println("Decrypted value: " + decryptedval);
                uploadFileButton.setEnabled(true);


            }
        });

        add(refreshButton, gbc);

        // Add the upload/download button to the bottom left
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        findFileButton.setVisible(false);
        add(findFileButton, gbc);

        // Add the find file button to the bottom center
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(fileNameTextField, gbc);

        // Add the file name text field to the bottom center
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);
        uploadFileButton.setEnabled(false);
        add(uploadFileButton, gbc);

    }

    public SwingWorker doTheSwingThing(int rowShit){
        SwingWorker<byte[], Void> worker = new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                //for each progress bar, set it as zero and then update the progress
                int progress = 0;
                Random random = new Random();
                setProgress(0);


                while (progress < 100) {


                    //Sleep for up to one second.
                    try {
                        Thread.sleep(random.nextInt(200));
                    } catch (InterruptedException ignore) {
                        System.out.println("Thread was interrupted");
                    }

                    progress++;

                    ((DefaultTableModel) table.getModel()).setValueAt(progress, rowShit, 1);

                }
                byte[] downloadedFileFromSlidingWindows = slidingWindows();
                return downloadedFileFromSlidingWindows;
            }

            //when the sliding windows is finished, we will add the file to an array list of all
            //the files that have been downloaded
            @Override
            protected void done(){
                try {
                    //get file data
                    byte[] downloadedFile = get();
                    //create file object
                    File file = new File(((DefaultTableModel) table.getModel()).getValueAt(rowShit, 0) + ".jpg");
                    //add to current files that have been downloaded
                    downloadedFiles.add(file);
                    JOptionPane.showMessageDialog(null, "File: " + (((DefaultTableModel) table.getModel()).getValueAt(rowShit, 0).toString()) + " has been downloaded, you will find it in your local file path");

                    //persist the file to a specific path
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    bos.write(downloadedFile);
                    bos.close();

                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (ExecutionException ex) {
//                  throw new RuntimeException(ex);
                    System.out.println("download was cancelled, aborting download for this file");
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        };
        //execute the swing worker
        worker.execute();
        return worker;
    }

    public String[] sendFRRQToGetPackets() {
        ArrayList<String> fileFromServer = new ArrayList<>();
        try {
            //create the request file packet
            FRRQ fileReadRequestPacket = new FRRQ(new DatagramPacket(new byte[5], 5, address, PORT), 8);
            //send it
            proxyServerConnectedTo.send(fileReadRequestPacket.getFRRQPacket());

            //we will then enter an infinite for loop, with a timeout, to listen for the packet back
            DatagramPacket receiveFiles = new DatagramPacket(new byte[3000], 3000);
            proxyServerConnectedTo.receive(receiveFiles);
            System.out.println("received files");

//            for (; ; ) {
//                proxyServerConnectedTo.receive(receiveFiles);
//                proxyServerConnectedTo.setSoTimeout(3000);
//                //if the packet has actually been received, the opcode will not be 0 and we can break out of
//                //the loop
//                if (receiveFiles.getData()[1] != 0)
//                    break;
//            }
            //we will then capture the data from the datagram packet and turn that into a list, to then populate
            //the list of files available to download from the server
            //if data packet
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[1]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[2]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[3]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[4]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[5]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[6]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[7]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[8]);
            System.out.println("recievedFiles.getData(): " + receiveFiles.getData()[9]);
                //while there the value is not -1, which will tell the client there are no more files to list

                int i = 5;
                while (receiveFiles.getData()[i] != -1) {
                    String temp = "";
                    for (; ; ) {
                        if (receiveFiles.getData()[i] == 0) {
                            //System.out.println("equalled zero, new string");
                            break;
                        }
                        else {
                            temp += (char) receiveFiles.getData()[i];
                            //System.out.println("added: " + (char) receiveFiles.getData()[i] + ", to string");
                        }
                            

                        i++;
                        //System.out.println("looped");
                    }
                    fileFromServer.add(temp);
                    i++;
                }
                String week[] = {"Fuck", "This", "Shit"};
                fileHolders.setListData(fileFromServer.toArray());


        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(null, "There was an error connecting to the proxy server");
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        //table.add(the files available);
        //return new String[]{"monday", "Tuesday", "Wednesday"};

        String[] listOFiles = new String[fileFromServer.toArray().length];
        int counter=0;
        for(Object o: fileFromServer.toArray()){
            listOFiles[counter] = (String) o;
            counter++;
        }

        return listOFiles;
    }

        //method to perform the sliding windows operation for downloading and uploading files
    public byte[] slidingWindows() throws IOException {
        Socket tcpSocketToServer = new Socket(InetAddress.getByName("rho.cs.oswego.edu"), 30000);
        tcpSocketToServer.setSoTimeout(10000);
        System.out.println("Establishged connection vie port 8000:");


        File tempFile = new File("downloaded"+ currentSelectedFile + ".jpeg");

        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        // Get the input stream of the server socket
        InputStream inputStream = tcpSocketToServer.getInputStream();

        // Transfer the file contents from the server
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(decrypt(new String(buffer)).getBytes(), 0, bytesRead);
        }

        // Close the streams and socket
        fileOutputStream.close();
        inputStream.close();
        tcpSocketToServer.close();
        System.out.println("File transfer complete!");


        return new byte[4];
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
