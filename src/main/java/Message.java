import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message {
    // 报文类型，规定：1:initialization  2:agreement  3:reverseRequest   4:reverseAnswer
    private short type;

    // 当 type=1 时就是任务书图示里的 N（总块数），3、4 就是传输 data 的长度。
    private int length;

    private String data;

    // 无参构造方法
    public Message() {
    }

    // 有参构造方法
    public Message(short type, int length, String data) {
        this.type = type;
        this.length = length;
        this.data = data;
    }

    public Message(short type, int length){
        this.type = type;
        this.length = length;
    }

    public Message(short type){
        this.type = type;
    }

    // Getter 和 Setter 方法
    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // 重写 toString 方法，方便打印对象信息
    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", length=" + length +
                ", data='" + data + '\'' +
                '}';
    }

    //序列化
    public byte[] serialize() {
        ByteBuffer buffer = null;
        if (type==1){
           buffer = ByteBuffer.allocate(6);
           buffer.putShort(type);
           buffer.putInt(length);// 当 type=1 时length就是N
        } else if (type==2){
            buffer=ByteBuffer.allocate(2);
            buffer.putShort(type);
        } else if (type==3) {
            buffer=ByteBuffer.allocate(6+data.getBytes(StandardCharsets.UTF_8).length);
            buffer.putShort(type);
            buffer.putInt(length);
            buffer.put(data.getBytes(StandardCharsets.UTF_8));
        } else if (type==4) {
            buffer=ByteBuffer.allocate(6+data.getBytes(StandardCharsets.UTF_8).length);
            buffer.putShort(type);
            buffer.putInt(length);
            buffer.put(data.getBytes(StandardCharsets.UTF_8));
        }

        return buffer.array();
    }

    //反序列化
    public static Message deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        //type:
        short type = buffer.getShort();

        Message message=new Message();
        message.setType(type);

        if (type==1){
            //N
            int N= buffer.getInt();
            message.setLength(N);
        } else if (type == 3||type==4) {
            int length=buffer.getInt();
            byte[] dataByte= new byte[]{buffer.get(length)};

            message.setLength(length);
            message.setData(new String(dataByte,StandardCharsets.UTF_8));
        }else {
            System.out.println("反序列化出错");
        }
        return message;
    }
}