import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Objects;

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
        this.setTitle("MultiTalk");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    }

    private void buildGUI() {
            JPanel southPanel = new JPanel(new GridLayout(3,0)); // 아래에 갈 패널 준비
            southPanel.add(createInputPanel());
            southPanel.add(createInfoPanel());
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

    private JPanel createInfoPanel() {
        JPanel p = new JPanel();
        InetAddress local = null;

        t_userID = new JTextField(5);
        t_hostAddr = new JTextField(8);
        t_portNum = new JTextField(5);

        t_userID.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                t_hostAddr.requestFocus();
            }
        });

        t_hostAddr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                t_portNum.requestFocus();
            }
        });

        t_portNum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                b_send.requestFocus();
            }
        });

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


        p.add(new JLabel("아이디:"));
        p.add(t_userID);
        p.add(new JLabel("서버주소:"));
        p.add(t_hostAddr);
        p.add(new JLabel("포트번호:"));
        p.add(t_portNum);

        return p;
    }

    private JPanel createControlPanel() { // 제일 밑단 버튼 3개, 접속하기 접속끊기 종료하기

        b_connect = new JButton("접속하기");
        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                serverAddress = t_hostAddr.getText();
                serverPort = Integer.parseInt(t_portNum.getText());
                String returnMsg = connectToServer(serverAddress, serverPort);
                if (Objects.equals(returnMsg, "Success")) {
                    printDisplay("소캣 연결 성공");
                    sendUserID();
                } else {
                    printDisplay("소캣 생성 오류 > " + returnMsg);
                }
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

    private void sendUserID() {
        try {
            out.write("/uid:" + t_userID.getText() + '\n');
            out.flush();
        } catch (IOException e) {
            System.err.println("클라이언트 쓰기 오류 > " + e.getMessage());
            System.exit(-1);
        }
    }

    private String connectToServer(String serverAddress, int serverPort) {

            socket = new Socket();
            SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        try {
            // 서버 연결, 타임아웃 3초
            socket.connect(sa, 3000);
        } catch (IOException e) {
            return e.getMessage();
        }

        try {
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            return e.getMessage();
        }
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (receiveThread == Thread.currentThread()) {
                    receiveMessage();
                }
            }
        });
        receiveThread.start();
        b_connect.setEnabled(false);
        b_disconnect.setEnabled(true);
        b_exit.setEnabled(false);
        b_send.setEnabled(true);
        return "Success";
    }

    private void sendMessage(String inputText) {
        if (inputText.isEmpty()) return; // 입력창 비었으면 아무것도 안 함
        else {
            try {
                out.write(inputText + '\n');
                out.flush();
            } catch (IOException e) {
                System.err.println("클라이언트 쓰기 오류 > " + e.getMessage());
                System.exit(-1);
            }
            t_input.setText(""); // 보낸 후 입력창은 비우기
        }
    }

    private void receiveMessage() {
        String inMsg;
        try {
            while((inMsg = ((BufferedReader)in).readLine()) != null)
                printDisplay(inMsg);
        } catch (IOException e) {
            System.err.println("클라이언트 일반 수신 오류 > " + e.getMessage());
        }
        disconnect();
        printDisplay("서버 연결 종료");
    }

    private void disconnect() {
        try {
            receiveThread = null;
            socket.close();
            in.close();
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

    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        MultiTalk client = new MultiTalk(serverAddress, serverPort);
    }
}


