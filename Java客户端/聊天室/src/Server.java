import javax.sound.sampled.Port;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.text.*;
import java.util.*;
import java.nio.channels.*;
import static java.lang.System.*;


public class Server {
    private static List<ThreadServer> clients=new ArrayList<ThreadServer>();


    public void startup() throws IOException {
        out.println("正在监听2021端口......");
        /*ServerSocketChannel serversocketChannel = ServerSocketChannel.open();
        serversocketChannel.configureBlocking(false);
        serversocketChannel.socket().bind(new InetSocketAddress(2021));*/
        ServerSocket ss=new ServerSocket(2021);//监听指定端口
        while(true){                            //循环等待连接

            try{
                //SocketChannel socketChannel=serversocketChannel.accept();
                Socket socket=ss.accept();
                out.println("【】有新用户正在尝试连接......");
                Thread st=new Thread(new ThreadServer(socket));//开启新的线程处理连接
                st.start();
            }catch(Exception e){
                out.println("【】该客户端连接已断开......");
            }

        }
    }

    public static class ThreadServer implements Runnable{
        private Socket socket;
        private BufferedReader br;
        private PrintWriter out;
        private String name = "游客";
        private Boolean flag=true;

        public ThreadServer(Socket socket) throws IOException {
            this.socket=socket;
            try {//建立好连接后，从socket中获取输入流
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(socket.getOutputStream(), true);
                String str = br.readLine();
                name=str+"["+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+"]";
            }catch(IOException e)
            {
                System.out.println("【】接收客户端用户信息失败^…^");
            }
            int n = clients.size()+1;
            System.out.println("【】"+name+"已连接本服务器。当前共:"+n+"*客户端");
            send("【系统消息】@"+name+"<加入了本群。群聊当前共"+n+"人。");
            clients.add(this);

        }
        //private
        public void send(String message) {              //发送消息函数
            try{
                for (ThreadServer threadServer : clients)
                {
                    threadServer.out.print(message);
                    threadServer.out.flush();               //清空缓存区
                }
            }catch(Exception e) {}
        }
        private void receive() throws IOException {             //接收消息函数

            String message;
            while(flag=true) {
                message=br.readLine();
                System.out.print("【】消息接收时间: ");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println(df.format(new Date()));
                System.out.println("@"+name+"<: "+message);           //将接收到的消息转发至每一个客户端上
                send("@"+name+"<: "+message);
            }


        }
        public void run() {
            try {
                while(flag=true) {
                    receive();

                }
            } catch (Exception e) {
                System.out.println("【】客户端"+name+"退出，连接已断开^…^");
                send("【系统消息】@"+name+"退出了本群");
                clients.remove(this);
            }finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    System.out.println("【】关闭socket失败^…^");
                }
            }
        }

    }

    public static void main(String []args) throws IOException {

        try{
            Server server=new Server();
            out.print("【】服务器已打开，");
            server.startup();
        }catch(Exception e){
            out.println("【】服务器开启失败，请重试^…^");
        }
    }

}