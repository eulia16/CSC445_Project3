import java.io.File;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    HashMap<Integer,File> fileHashMap;
    Socket socket;
    int port;

    public Server(Socket socket, int port){
        fileHashMap = new HashMap<>();
        this.socket = socket;
        this.port = port;
    }

    public void removeFile(File file){
        fileHashMap.remove(file.hashCode());
    }

    public byte[] getFileBytes(int hashCode){
        return fileHashMap.get(hashCode).toString().getBytes();
    }
}
