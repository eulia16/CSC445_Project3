import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class MyWindow extends JFrame {

    private ArrayList<File> downloadedFiles = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    private String proxyString="pi.cs.oswego.edu";

    private InetAddress address = InetAddress.getByName(proxyString);

    private int currentRowSelected;

    String currentSelectedFile=null;

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
                    System.out.println("This would now send an RRQ packet to the server, asking to download this file: " + currentSelectedFile);
//                    somehow grab the name of the file that has been selected
//                    and send that as the bytes for the RRQ packet
//                    send a request to the proxyServer with a request for this machine to start downloading
                    proxyServerConnectedTo.send(packetToSend.getPassedPacket());
                    //we then append a new fileDownload guy to the current downloaded files
                    Object[] data = {currentSelectedFile, 0, "Delete"};
                    ((DefaultTableModel)table.getModel()).addRow(data);

                    //add name of file to the texg field
                    fileNameTextField.setText(currentSelectedFile.toString());

                    //after sending the packet, we will need to break off and create a new class to handle the
                    //downloading and uploading, as well as popping a new file to the jlist and keeping the progress
                    //bar up to date

                    //we will use a swing worker to handle each time the button is pressed, to break off a new
                    //swing worker thread, and begin the sliding windows download
                    SwingWorker<byte[], Void> worker = new SwingWorker<byte[], Void>() {
                        @Override
                        protected byte[] doInBackground() throws Exception {
                            //for each progress bar, set it as zero and then update the progress
                            int progress = 0;
                            Random random = new Random();
                            //System.out.println("class of part in table: "+;//  .getColumnClass(1).);// .getValueAt(currentRowSelected, 1).getClass());
                            setProgress(0);
                            while (progress < 100) {

                                //Sleep for up to one second.
                                try {
                                    Thread.sleep(random.nextInt(200));
                                } catch (InterruptedException ignore) {}

                                progress++;

                                ((DefaultTableModel) table.getModel()).setValueAt(progress, table.getRowCount()-1, 1);

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
                                File file = new File( "tempName"+ ".jpg");
                                //add to current files that have been downloaded
                                downloadedFiles.add(file);
                                //persist the file to a specific path
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                bos.write(downloadedFile);
                                bos.close();

                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            } catch (ExecutionException ex) {
                                throw new RuntimeException(ex);
                            } catch (FileNotFoundException ex) {
                                throw new RuntimeException(ex);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }

                        }
                    };
                    //execute the swing worker
                    worker.execute();
                    table.getModel().addTableModelListener(new TableModelListener() {
                        @Override
                        public void tableChanged(TableModelEvent e) {

                        }
                    });

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
        TestFile testFile = new TestFile();
        this.table = testFile.getTable();
        add(this.table, gbc);


        //temp for now
        JList fileHolders;
        String week[]= { "Monday","Tuesday","Wednesday",
                "Thursday","Friday","Saturday","Sunday"};
        fileHolders= new JList(week);
        fileHolders.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()){
                    JList source = (JList)e.getSource();
                    currentSelectedFile = source.getSelectedValue().toString();
                    currentRowSelected = source.getSelectedIndex();
                }
                System.out.println("current Selected File: " + currentSelectedFile);
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
                String[] allCurrentFiles = sendRRQToGetPackets();
                 //String week[]= { "Fuck","This","Shit"};
                fileHolders.setListData(allCurrentFiles);

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
        add(uploadFileButton, gbc);


    }


    public void setButtonIconCircle(JToggleButton button){
        button.setPreferredSize(new Dimension(20, 20));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
    }

    public String[] sendRRQToGetPackets(){
        //try {
//                    //create the request file packet
//                    FRRQ fileReadRequestPacket = new FRRQ(new DatagramPacket(new byte[5], 5, address, PORT), 8);
//                    //send it
//                    proxyServerConnectedTo.send(fileReadRequestPacket.getFRRQPacket());
//                    //we will then enter an infinite for loop, with a timeout, to listen for the packet back
//                    DatagramPacket receiveFiles = new DatagramPacket(new byte[2048], 2048);
//                    for(;;){
//                        proxyServerConnectedTo.receive(receiveFiles);
//                        proxyServerConnectedTo.setSoTimeout(3000);
//                        //if the packet has actually been received, the opcode will not be 0 and we can break out of
//                        //the loop
//                        if(receiveFiles.getData()[1] != 0)
//                            break;
//                    }
            //we will then capture the data from the datagram packet and turn that into a list, to then populate
            //the list of files available to download from the server
            //if data packet
//                    if(receiveFiles.getData()[1] == 3){
//                        //while there the value is not -1, which will tell the client there are no more files to list
//                       ArrayList<String> fileFromServer = new ArrayList<>();
//                        int i=0;
//                        while(receiveFiles.getData()[i] != -1) {
//                            String temp = "";
//                            for (;;) {
//                                if(receiveFiles.getData()[i] == 0)
//                                    break;
//                                else
//                                    temp += (char)receiveFiles.getData()[i] ;
//                            }
//                            fileFromServer.add(temp);
//                        }
            //String week[]= { "Fuck","This","Shit"};
            //fileHolders.setListData(week);
//                    }
//
//                    else{
//                        JOptionPane.showMessageDialog(null, "There was an error retrieving the available files from the proxy server");
//                    }
//
//                } catch (UnknownHostException ex) {
//                    JOptionPane.showMessageDialog(null, "There was an error connecting to the proxy server");
//                    throw new RuntimeException(ex);
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//                //table.add(the files available);
        return new String[]{"monday", "Tuesday", "Wednesday"};
    }

    //method to perform the sliding windows operation for downloading and uploading files
    public byte[] slidingWindows(){
        byte[] bytes = new byte[1];
        return bytes;
    }


}
