import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Vector;

public class MultiChatServer extends JFrame {
    private int port;
    private ServerSocket serverSocket;
    private JTextArea t_display;
    private JButton b_connect, b_disconnect, b_exit;

    private Vector<ClientHandler> users = new Vector<>();

    private Thread acceptThread = null;

    public MultiChatServer(int port) {
        super("MulTiChatServerGUI");
        this.port = port;
        buildGUI();
        this.setBounds(100, 200, 400, 300);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    } // 생성자

    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() { // 최상단 JTextArea
        t_display = new JTextArea();
        t_display.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(t_display), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() { // 제일 밑단 종료 버튼

        b_connect = new JButton("서버 시작");
        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                acceptThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startServer();
                    }
                });
                acceptThread.start();
                //접속 끊기 전에는 종료하거나 다시 접속하기 불가
                b_connect.setEnabled(false);
                b_disconnect.setEnabled(true);
                b_exit.setEnabled(false);

            }
        });

        b_disconnect = new JButton("서버 종료");
        b_disconnect.setEnabled(false); // 처음엔 비활성화
        b_disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                disconnect();
                b_connect.setEnabled(true);
                b_disconnect.setEnabled(false);
                b_exit.setEnabled(true);
            }
        });

        b_exit = new JButton("종료하기");
        b_exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    if(serverSocket != null) serverSocket.close();
                }
                catch (IOException e) {
                    System.err.println("서버 닫기 오류 > " + e.getMessage());
                    System.exit(-1);
                }
                System.exit(0);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0,3));

        panel.add(b_connect);
        panel.add(b_disconnect);
        panel.add(b_exit);
        b_disconnect.setEnabled(false);

        return panel;
    }

    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    private void startServer() {
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            printDisplay("서버가 시작됐습니다. " + getLocalAddr());

            while (acceptThread == Thread.currentThread()) { // 클라이언트 접속 기다림
                clientSocket = serverSocket.accept(); // 접속 받음

                String cAddr = clientSocket.getInetAddress().getHostAddress();
                printDisplay("클라이언트가 연결됐습니다: " + cAddr + "\n");
                //스레드 생성해서 시키기
                ClientHandler cHandler = new ClientHandler(clientSocket);
                users.add(cHandler);
                cHandler.start();
            }
        } catch (SocketException e) {
//            System.err.println("서버 소캣 종료 : " + e.getMessage());
            printDisplay("서버 소캣 종료");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                    if (clientSocket != null) clientSocket.close();
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
                    System.err.println("서버 닫기 오류 > " + e.getMessage());
                    System.exit(-1);
                }
            }
        }

    private void disconnect() {
        try {
            acceptThread = null;
            serverSocket.close();
        }
        catch (IOException e) {
            System.err.println("서버 닫기 오류 > " + e.getMessage());
            System.exit(-1);
        }
    }

    // localhost의 ip 주소 문자열로 반환
    private String getLocalAddr() {
        InetAddress local = null;
        String addr = "";
        try {
            local = InetAddress.getLocalHost();
            addr = local.getHostAddress();
            System.out.println(addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr;
    }

    private class ClientHandler extends Thread {
        private final Socket clientSocket;
        private BufferedWriter out;
        String uid;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
    }

        private void sendMessage(String inputText) {
            if (inputText.isEmpty()) return; // 입력창 비었으면 아무것도 안 함
            else {
                try {
                    ((BufferedWriter)out).write(inputText + '\n');
                    out.flush();
                } catch (IOException e) {
                    System.err.println("서버 쓰기 오류 > " + e.getMessage());
                    System.exit(-1);
                }
            }
        }

        void broadcasting(String msg) {
            for (ClientHandler c : users) {
                c.sendMessage(msg);
            }
        }

        private void receiveMessages(Socket cs) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
                    out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));

                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.contains("/uid")) {
                            // : 다음 내용 가져오기
                            String[] tok = message.split(":");
                            uid = tok[1];
                            printDisplay("새 참가자: " + uid);
                            printDisplay("현재 참가자 수 : " + users.size());
                            continue;
                        }
                            message = (uid + ": " + message);
                            printDisplay(message);
                            broadcasting(message);
                    }
                    users.removeElement(this);
                    printDisplay(uid + " 퇴장. 현재 참가자 수: " + users.size());
                } catch (IOException e) {
                    users.removeElement(this);
                    printDisplay(uid + " 연결 끊김. 현재 참가자 수: " + users.size());
                } finally {
                    try {
                        // 소캣 cs 닫으면 여기서 야기된 스트림들도 자동으로 닫힌다.
                        cs.close();
                        //스레드 종료 처리 어떻게?
                    } catch (IOException ex) {
                        System.err.println("서버 닫기 오류 > " + ex.getMessage());
                        System.exit(-1);
                    }
                }
            }

        @Override
        public void run() {
            // 특정 소캣에 대해서 receiveMessages
            receiveMessages(clientSocket);
        }
}

    public static void main(String[] args) {
        int port = 54321;
        MultiChatServer server = new MultiChatServer(port);
    }
}
