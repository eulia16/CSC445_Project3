import java.io.IOException;
import java.net.URISyntaxException;

public class mainServer {

        public static void main(String argz[]) throws IOException, URISyntaxException, InterruptedException {

            ProxyServer pServer = new ProxyServer(false);
            pServer.listen();

        }
}
