import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;

public class PageGrabber {

    private String destFile;

    //empty constructor
    public PageGrabber(String destinationFile) {
        this.destFile = destinationFile;
    }

    public File obtainImageFromURL(String passedURL) throws IOException, URISyntaxException {
        //create URI object
        URI uri = new URI(passedURL);
        //open stream to read file from http location
        InputStream inputStream = uri.toURL().openStream();
        //create fileoutputstream to write file contents to file, passed as parameter
        File file = new File(System.getProperty("user.home") + "/CSC445/project2/" + destFile + ".jpg");

        OutputStream outputStream = new FileOutputStream(file);

        byte[] b = new byte[2048];
        int length;

        while ((length = inputStream.read(b)) != -1) {
            outputStream.write(b, 0, length);
        }

        inputStream.close();
        outputStream.close();

        return file;

    }

}
