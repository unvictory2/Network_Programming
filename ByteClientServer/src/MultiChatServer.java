import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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

        JPanel southPanel = new JPanel(new GridLayout(1,0)); // 아래에 갈 패널 준비
        southPanel.add(createControlPanel());

        this.add(createDisplayPanel(), BorderLayout.CENTER);
        this.add(southPanel, BorderLayout.SOUTH);

//        this.add(createDisplayPanel(), BorderLayout.CENTER);
//        this.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private void startServer() {
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            printDisplay("서버가 시작됐습니다.");
            while (acceptThread == Thread.currentThread()) { // 클라이언트 접속 기다림
                clientSocket = serverSocket.accept(); // 접속 받음
                printDisplay("클라이언트가 연결됐습니다.");
                //스레드 생성해서 시키기
                ClientHandler cHandler = new ClientHandler(clientSocket);
                users.add(cHandler);
                cHandler.start();
            }
        } catch (IOException e) {
//            System.err.println("서버 소캣 종료 : " + e.getMessage());
            printDisplay("서버 소캣 종료");
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
            for (ClientHandler h : users) {
                h.sendMessage(msg);
            }
        }

        private void receiveMessages(Socket cs) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
                    out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));

                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.contains("/uid")) {
                            // message.indexOf : 실제 uid가 나오는 부분의 인덱스
                            // subString : 해당 인덱스의 내용 추출
                            uid = message.substring(message.indexOf("/uid:") + 5);
                            printDisplay("새 참가자: " + uid);
                            printDisplay("현재 참가자 수 : " + users.size());
                        }
                        else {
                            message = (uid + ": " + message);
                            printDisplay(message);
                            broadcasting(message);
                        }
                    }
                    printDisplay("클라이언트가 연결을 종료했습니다.");
                } catch (IOException e) {
                    System.err.println("서버 읽기 오류 > " + e.getMessage());
                } finally {
                    try {
                        users.remove(this);
                        cs.close();
                        out.close();
                        clientSocket.close();
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
