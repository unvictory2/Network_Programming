import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PChatServer extends JFrame {
    private int port;
    private ServerSocket serverSocket;
    private JTextArea t_display;
    private JTextField t_input;
    private JButton b_connect, b_disconnect, b_exit, b_send;
    private BufferedWriter out;
    private Thread acceptThread = null;

    public P2PChatServer(int port) {
        super("P2PChatServerGUI");
        this.port = port;
        buildGUI();
        this.setBounds(100, 200, 400, 300);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    } // 생성자

    private void buildGUI() {

        JPanel southPanel = new JPanel(new GridLayout(2,0)); // 아래에 갈 패널 준비
        southPanel.add(createInputPanel());
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
                t_input.setEnabled(false);
                b_send.setEnabled(false);
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
                }
                System.exit(-1);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0,3));

        panel.add(b_connect);
        panel.add(b_disconnect);
        panel.add(b_exit);
        b_disconnect.setEnabled(false);

        return panel;
    }

    private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼
        t_input = new JTextField(30);
        t_input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(t_input.getText());
//                receiveMessage();
            }
        });

        b_send = new JButton("보내기");
        b_send.setEnabled(false);
        b_send.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage(t_input.getText());
//                receiveMessage();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(t_input, BorderLayout.CENTER);
        panel.add(b_send, BorderLayout.EAST);

        return panel;
    }

    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    private void sendMessage(String inputText) {
        if (inputText.isEmpty()) return; // 입력창 비었으면 아무것도 안 함
        else {
            try {
                ((BufferedWriter)out).write(inputText + '\n');
                out.flush();
            }
            catch (NumberFormatException e) { // 정수 아니면 오류
                System.err.println("정수가 아님! " + e.getMessage());
                return;
            } catch (IOException e) {
                System.err.println("클라이언트 쓰기 오류 > " + e.getMessage());
                System.exit(-1);
            }
            t_display.append("나: " + inputText + "\n");
            t_input.setText(""); // 보낸 후 입력창은 비우기
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

    private class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            t_input.setEnabled(true);
            b_send.setEnabled(true);
    }

    private void receiveMessages(Socket cs) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
                out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));

                String message;
                while ((message = in.readLine()) != null) {
                    printDisplay("클라이언트 메시지: " + message);
                }
                printDisplay("클라이언트가 연결을 종료했습니다.\n");
            } catch (IOException e) {
                System.err.println("서버 읽기 오류 > " + e.getMessage());
            } finally {
                try {
                    cs.close();
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
        P2PChatServer server = new P2PChatServer(port);
    }
}
