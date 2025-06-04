项目启动方法：
clone下来后，分别进入src/main/java
打开cmd，先编译：
javac -encoding UTF-8 Client.java Message.java

javac -encoding UTF-8 Server.java ClientHandler.java Message.java

后运行（先server后client）：
java Server

java Client 127.0.0.1 10086 300 350