import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread{
    Socket socket;
    public ClientHandler(Socket  socket){
        this.socket = socket;
    }
    public void run(){
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {

            // 接收初始化消息
            Message initMessage = receiveMessage(inputStream);
            if (initMessage.getType() != 1) {
                System.out.println("非法信息");
                return;
            }

            System.out.println("建立连接，总块数: " + initMessage.getLength());
            int N = initMessage.getLength();

            //发送Agree
            Message accept = new Message((short) 2);
            sendMessage(outputStream, accept);

            //循环N次对话
            for (int i = 0; i < N; i++) {
                Message request = receiveMessage(inputStream);
                System.out.println("收到第" + (i + 1) + "块文本：" + request.getData());

                StringBuilder   sb=new StringBuilder(request.getData());
                String  content=sb.reverse().toString();
                Message response =new Message((short) 4,request.getLength(), content);
                sendMessage(outputStream,response);
            }

            System.out.println("一个线程处理完成，关闭连接："+socket.getInetAddress()+":"+socket.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 发送消息
    private void sendMessage(OutputStream out, Message message) throws IOException {
        byte[] data = message.serialize();
        out.write(data);
        out.flush();
    }

    // 接收消息
    private Message receiveMessage(InputStream in) throws IOException {
        byte[] typeBytes = new byte[2];
        byte[] lengthBytes = new byte[4];

        // 先读type（2字节）
        int bytesRead = 0;
        while (bytesRead < typeBytes.length) {
            int count = in.read(typeBytes, bytesRead, typeBytes.length - bytesRead);
            if (count == -1) throw new EOFException("连接已关闭");
            bytesRead += count;
        }

        // 解析type
        ByteBuffer buffer = ByteBuffer.wrap(typeBytes);
        short type = buffer.getShort();

        // 根据type决定后续读取策略（服务端只会收到1或3）
        int dataLength = 0;
        if (type == 1) {
            // 类型1: 初始化消息，读取N（length字段）
            bytesRead = 0;
            while (bytesRead < lengthBytes.length) {
                int count = in.read(lengthBytes, bytesRead, lengthBytes.length - bytesRead);
                if (count == -1) throw new EOFException("连接已关闭");
                bytesRead += count;
            }
            buffer = ByteBuffer.wrap(lengthBytes);
            int N = buffer.getInt();
            return new Message((short) 1, N);
        } else if (type == 3) {
            // 类型3: 反向请求，读取length和data
            bytesRead = 0;
            while (bytesRead < lengthBytes.length) {
                int count = in.read(lengthBytes, bytesRead, lengthBytes.length - bytesRead);
                if (count == -1) throw new EOFException("连接已关闭");
                bytesRead += count;
            }
            buffer = ByteBuffer.wrap(lengthBytes);
            dataLength = buffer.getInt();

            // 读取数据部分
            byte[] data = new byte[dataLength];
            bytesRead = 0;
            while (bytesRead < dataLength) {
                int count = in.read(data, bytesRead, dataLength - bytesRead);
                if (count == -1) throw new EOFException("连接已关闭");
                bytesRead += count;
            }

            // 合并完整消息
            byte[] fullData = new byte[2 + 4 + dataLength];
            System.arraycopy(typeBytes, 0, fullData, 0, 2);
            System.arraycopy(lengthBytes, 0, fullData, 2, 4);
            System.arraycopy(data, 0, fullData, 6, dataLength);

            return Message.deserialize(fullData);
        } else {
            throw new IOException("未知消息类型: " + type);
        }
    }
}
