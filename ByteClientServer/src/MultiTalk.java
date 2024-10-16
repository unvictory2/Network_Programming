import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiTalk extends JFrame{ // 내가 프레임의 후손이 되는 방법. JBasicFrame1에서는 자기 안에 프레임을 뒀었다.
    private String serverAddress;
    private int serverPort;

    Socket socket;
    private Writer out;
    private Reader in;

    JButton b_connect, b_disconnect, b_exit, b_send; // 하단에 있는 3개의 버튼
    JTextArea t_display; // 상단의 디스플레이
    JTextField t_input, t_userID, t_hostAddr, t_portNum; // 입력창

    private Thread receiveThread = null;

    public MultiTalk(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        this.setBounds(500,200,400,300);
        this.setTitle("P2P Free Talk");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    }

    private void buildGUI() {
            JPanel southPanel = new JPanel(new GridLayout(2,0)); // 아래에 갈 패널 준비
            southPanel.add(createInputPanel());
            southPanel.add(createControlPanel());

            this.add(createDisplayPanel(), BorderLayout.CENTER);
            this.add(southPanel, BorderLayout.SOUTH);
        }

    private JPanel createDisplayPanel() { // 최상단 JTextArea
        t_display = new JTextArea();
        t_display.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(t_display), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼
        t_input = new JTextField(30);
        t_input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(t_input.getText());
            }
        });

        b_send = new JButton("보내기");
        b_send.setEnabled(false);
        b_send.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage(t_input.getText());
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(t_input, BorderLayout.CENTER);
        panel.add(b_send, BorderLayout.EAST);

        return panel;
    }

    private JPanel crateInfoPanel() {

        InetAddress local = null;
        try {
            local = InetAddress.getLocalHost();
            String addr = local.getHostAddress();
            String[] part = addr.split("\\.");
            t_userID.setText("guest" + part[3]);
        } catch (UnknownHostException e) {
            System.err.println("호스트 찾을 수 없음 > " + e.getMessage());
        }

        t_hostAddr.setText(serverAddress);
        t_portNum.setText(String.valueOf(serverPort));


        JPanel panel = new JPanel();
        return panel;
    }

    private JPanel createControlPanel() { // 제일 밑단 버튼 3개, 접속하기 접속끊기 종료하기

        b_connect = new JButton("접속하기");
        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                t_hostAddr.getText();
                t_portNum.getText();
                connectToServer();
                sendUserID();
            }
        });

        b_disconnect = new JButton("접속 끊기");
        b_disconnect.setEnabled(false); // 처음엔 비활성화
        b_disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                disconnect();
            }
        });

        b_exit = new JButton("종료하기");
        b_exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0,3));

        panel.add(b_connect);
        panel.add(b_disconnect);
        panel.add(b_exit);

        return panel;
    }
    
    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort); // 소캣 연결
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            System.out.println("소캣 연결 성공");

            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (receiveThread == Thread.currentThread()) {
                        receiveMessage();
                    }
                }
            });
            receiveThread.start();
        } catch (IOException e) {
            System.err.println("소캣 연결 오류 : " + e.getMessage());
            System.exit(-1);
        }
        b_connect.setEnabled(false);
        b_disconnect.setEnabled(true);
        b_exit.setEnabled(false);
        b_send.setEnabled(true);
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

    private void receiveMessage() {
        try {
            String inMsg = ((BufferedReader)in).readLine();
            t_display.append("서버: \t" + inMsg + "\n");
        } catch (IOException e) {
            System.err.println("클라이언트 일반 수신 오류 > " + e.getMessage());
        }
    }

    private void disconnect() {
        try {
            receiveThread = null;
            out.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류 > " + e.getMessage());
            System.exit(-1);
        }
        b_connect.setEnabled(true);
        b_disconnect.setEnabled(false);
        b_exit.setEnabled(true);
        b_send.setEnabled(false);
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        MultiTalk client = new MultiTalk(serverAddress, serverPort);
    }
}


