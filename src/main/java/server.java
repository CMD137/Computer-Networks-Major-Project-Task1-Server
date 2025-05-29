import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    public static void main(String[] args) {
        try(ServerSocket serverSocket=new ServerSocket(10087)){
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("客户端连接: " + socket.getInetAddress());
                ClientHandler  clientHandler=new ClientHandler(socket);
                clientHandler.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
