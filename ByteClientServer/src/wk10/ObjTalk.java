package wk10;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class ObjTalk extends JFrame{ // 내가 프레임의 후손이 되는 방법. JBasicFrame1에서는 자기 안에 프레임을 뒀었다.
    private String serverAddress;
    private int serverPort;
    String uid;

    Socket socket;
    private ObjectOutputStream out;

    JButton b_connect, b_disconnect, b_exit, b_send; // 하단에 있는 3개의 버튼
    JTextArea t_display; // 상단의 디스플레이
    JTextField t_input, t_userID, t_hostAddr, t_portNum; // 입력창

    private Thread receiveThread = null;

    public ObjTalk(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        this.setBounds(500,200,400,300);
        this.setTitle("ObjTalk");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    }

    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);

        JPanel p_input = new JPanel(new GridLayout(3,0)); // 아래에 갈 패널 준비
        p_input.add(createInputPanel());
        p_input.add(createInfoPanel());
        p_input.add(createControlPanel());
        add(p_input, BorderLayout.SOUTH);
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
                sendMessage();
            }
        });

        b_send = new JButton("보내기");
        b_send.setEnabled(false);
        b_send.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(t_input, BorderLayout.CENTER);
        panel.add(b_send, BorderLayout.EAST);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        InetAddress local = null;

        t_userID = new JTextField(7);
        t_hostAddr = new JTextField(12);
        t_portNum = new JTextField(5);

        t_userID.setText("guest" + getLocalAddr().split("\\.")[3]);
        t_hostAddr.setText(this.serverAddress);
        t_portNum.setText(String.valueOf(this.serverPort));

        t_portNum.setHorizontalAlignment(JTextField.CENTER);

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
                //MultiTalk.this 안 써줘도 되는데 명시적으로 쓸 거면 다 써야 된다. 그냥 this는 외부에 있는 클래스 MultiTalk를 지칭하지 않고 이 함수 내부를 의미하기 때문.
                serverAddress = t_hostAddr.getText();
                serverPort = Integer.parseInt(t_portNum.getText());

                try {
                    connectToServer();
                    sendUserID();
                } catch (UnknownHostException e1) {
                    printDisplay("서버 주소와 포트 번호를 확인하세요 : " + e1.getMessage());
                    //버튼 상태 안 바꾸고 빠져나옴
                    return;
                } catch (IOException e1) {
                    printDisplay("서버와의 연결 오류 : " + e1.getMessage());
                    return;
                }

                b_connect.setEnabled(false);
                b_disconnect.setEnabled(true);

                t_input.setEnabled(true);
                b_send.setEnabled(true);
                b_exit.setEnabled(false);

                t_userID.setEditable(false);
                t_hostAddr.setEditable(false);
                t_portNum.setEditable(false);

//                String returnMsg = connectToServer(serverAddress, serverPort);
//                if (Objects.equals(returnMsg, "Success")) {
//                    printDisplay("소캣 연결 성공");
//                    sendUserID();
//                } else {
//                    printDisplay("소캣 생성 오류 > " + returnMsg);
//                }
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

    private void connectToServer() throws UnknownHostException, IOException {
        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        // 서버 연결, 타임아웃 3초
        socket.connect(sa, 3000);

        // OBjectStream의 특징 : out은 in에서 데이터 안 받아도 상관없는데 in은 out에서 데이터 못 받으면 계속 대기함. 그래서 여기서 in 만들어서 무한대기 하게 하지 말고 스레드의 run에서 만듦
        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        receiveThread = new Thread(new Runnable() {
            private ObjectInputStream in;

            private void receiveMessage() {
                try {
                    ChatMsg inMsg = (ChatMsg)in.readObject();
                    if (inMsg == null) {
                        disconnect();
                        printDisplay("서버 연결 끊김");
                        return;
                    }
                    switch (inMsg.mode) {
                        // 문자열 읽는 모드일경우 출력
                        case ChatMsg.MODE_TX_STRING :
                            printDisplay(inMsg.userID + ": " + inMsg.message);
                            break;
                    }
                } catch (IOException e) {
                    System.err.println("클라이언트 일반 수신 오류 > " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    printDisplay("잘못된 객체가 전달되었습니다.");
                }
            }

            @Override
            public void run() {
                try {
                    in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                } catch (IOException e) {
                    printDisplay("입력 스트림이 열리지 않음");
                }
                while (receiveThread == Thread.currentThread()) {
                    receiveMessage();
                }
            }
        });
        receiveThread.start();
    }

    private void sendUserID() {
        uid = t_userID.getText();
        // 전달하는 인자의 종류와 모드 설정을 통해 송수신기 사이에 어떤 정보가 오가는 건지 판단 가능. 예전처럼 uid라고 앞에 붙일 필요 없다.
        send(new ChatMsg(uid, ChatMsg.MODE_LOGIN));
    }

    private void sendMessage() {
        String message = t_input.getText();
        if (message.isEmpty()) return; // 입력창 비었으면 아무것도 안 함
        send(new ChatMsg(uid, ChatMsg.MODE_TX_STRING, message));
        t_input.setText(""); // 보낸 후 입력창은 비우기
    }

    private void disconnect() {
        send(new ChatMsg(uid, ChatMsg.MODE_LOGOUT));
        try {
            receiveThread = null;
            socket.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류 > " + e.getMessage());
            System.exit(-1);
        }
    }

    private void send(ChatMsg msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류 > " + e.getMessage());
        }
    }

    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        ObjTalk client = new ObjTalk(serverAddress, serverPort);
    }
}


