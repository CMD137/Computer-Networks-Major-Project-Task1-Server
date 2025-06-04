import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try(ServerSocket serverSocket=new ServerSocket(10086)){
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("客户端连接: " + socket.getInetAddress()+socket.getPort());
                ClientHandler  clientHandler=new ClientHandler(socket);
                clientHandler.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
