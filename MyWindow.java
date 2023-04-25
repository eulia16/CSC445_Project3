import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MyWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JLabel projectNameLabel;
    private JLabel proxyServerLabel;
    private JPanel tablePanel;
    private JButton uploadDownloadButton;
    private JButton findFileButton;
    private JTextField fileNameTextField;
    private JButton uploadFileButton;

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
        setPreferredSize(new Dimension(800, 600));

        // Create the components
        projectNameLabel = new JLabel("Baby Torrent");
        String proxyConnectedTo = "pi.cs.oswego.edu";
        proxyServerLabel = new JLabel("Proxy Server Connected To: " + proxyConnectedTo);
        tablePanel = new JPanel();
        //tablePanel.setBackground(Color.BLACK);
        uploadDownloadButton = new JButton("Download");
        findFileButton = new JButton("Find File");
        fileNameTextField = new JTextField("File Name");
        uploadFileButton = new JButton("Upload/Download");

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
        add(new TestFile().getTable(), gbc);


        //load images
        BufferedImage previewButton = ImageIO.read(new File("/Users/ethan/Desktop/CSC445_Project3/ImageFiles/unClickedButtonNew.png"));
        Image previewButtonIcon = previewButton.getScaledInstance(70, 50, previewButton.SCALE_SMOOTH);

        //resize the timage to fit the frame
        Image resizedImage = previewButtonIcon.getScaledInstance(20, 20,  previewButtonIcon.SCALE_SMOOTH);//this.originalPic.SCALE_SMOOTH);//originalPicPanel.getWidth(), originalPicPanel.getHeight(), this.originalPic.SCALE_SMOOTH);
          //Image resizedImage = previewButtonIcon.getScaledInstance(originalPicPanel.getWidth(), originalPicPanel.getHeight(), this.originalPic.SCALE_SMOOTH);
        //set the image icon to the resized image
        ImageIcon icon = new ImageIcon(resizedImage);

        //JTogglebutton right above to allow between uploading and downloading
        uploadOrDownloadOption = new JToggleButton(icon);
          //setButtonIconCircle(icon);
        uploadOrDownloadOption.setPressedIcon(icon);

//        gbc.gridx = 0;
//        gbc.gridy = 2;
//        gbc.gridwidth = 1;
//        gbc.weightx = 0.0;
//        gbc.weighty = 0.0;
//        gbc.fill = GridBagConstraints.NONE;
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.insets = new Insets(10, 20, 70, 10);
//        add(uploadOrDownloadOption, gbc);

//        gbc.gridx = 0;
//        gbc.gridy = 2;
//        gbc.gridwidth = 1;
//        gbc.weightx = 0.0;
//        gbc.weighty = 0.0;
//        gbc.fill = GridBagConstraints.NONE;
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.insets = new Insets(10, 60, 70, 10);
//        add(new JLabel("Click to change between uploading and downloading files"), gbc);


        // Add the upload/download button to the bottom left
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
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
}
