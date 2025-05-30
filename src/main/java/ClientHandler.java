import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread{
    Socket socket;
    public ClientHandler(Socket  socket){
        this.socket = socket;
    }
    public void run(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            //回复Init
            byte[] bytes = reader.readLine().getBytes(StandardCharsets.UTF_8);
            System.out.println(bytes);


            Message initMessage=Message.deserialize(bytes);

            if (initMessage.getType()!=1){
                System.out.println("非法信息");
                return;
            }

            System.out.println("建立连接");
            int N=initMessage.getLength();

            //Accept
            Message accept=new Message((short) 2);
            writer.println(accept.serialize());

            //循环N次对话
            for (int i = 0; i < N; i++) {
                byte[] requestByte = reader.readLine().getBytes(StandardCharsets.UTF_8);
                Message request=Message.deserialize(requestByte);
                System.out.println("收到第"+(i+1)+"块文本："+request.getData());

                StringBuilder   sb=new StringBuilder(request.getData());
                String  content=sb.reverse().toString();

                Message response =new Message((short) 4,request.getLength(), content);
            }

            socket.close();



        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
