import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;

public class EchoClientGUI extends JFrame{
    JTextField t_input; // 입력창
    JTextArea t_display; // 상단의 디스플레이
    JButton b_connect, b_disconnect, b_exit, b_send; // 하단에 있는 3개의 버튼

    private final String serverAddress;
    private final int serverPort;

    Socket socket;
    private Writer out;
    private Reader in; // BufferedReader로 정의해도 되지만 매번 참조변수 유형 안 바꾸기 위해 이 방식으로 사용. 대신 사용할 때마다 하위 클래스에서 정의된 멤버에 접근할 땐 다운캐스팅 해줘야 함.

    public EchoClientGUI(String serverAddress, int serverPort) {
        super("EchoClientGUI");

        buildGUI();

        setBounds(500,200,400,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);

        JPanel p_input = new JPanel(new GridLayout(2,0));
        p_input.add(createInputPanel());
        p_input.add(createControlPanel());
        add(p_input, BorderLayout.SOUTH);
        }

    private JPanel createDisplayPanel() { // 최상단 JTextArea
        JPanel p = new JPanel(new BorderLayout());

        t_display = new JTextArea();
        t_display.setEditable(false);

        p.add(new JScrollPane(t_display), BorderLayout.CENTER);

        return p;
    }

    private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼

        JPanel p = new JPanel(new BorderLayout());

        t_input = new JTextField(30);
        t_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
                receiveMessage();
            }
        });

        b_send = new JButton("보내기");
        b_send.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
                receiveMessage();
            }
        });

        p.add(t_input, BorderLayout.CENTER);
        p.add(b_send, BorderLayout.EAST);

        t_input.setEnabled(false);
        b_send.setEnabled(false);

        return p;
    }

    private JPanel createControlPanel() { // 제일 밑단 버튼 3개, 접속하기 접속끊기 종료하기
        JPanel p = new JPanel(new GridLayout(0,3));

        b_connect = new JButton("접속하기");
        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();

                b_connect.setEnabled(false);
                b_disconnect.setEnabled(true);

                t_input.setEnabled(true);
                b_send.setEnabled(true);
                b_exit.setEnabled(false);
            }
        });

        b_disconnect = new JButton("접속 끊기");
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
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        p.add(b_connect);
        p.add(b_disconnect);
        p.add(b_exit);

        b_connect.setEnabled(true);
        b_disconnect.setEnabled(false);
        b_exit.setEnabled(true);

        return p;
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort); // 소캣 연결
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            System.err.println("소캣 연결 오류 : " + e.getMessage());
            System.exit(-1);
        }
    }

    private void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류 > " + e.getMessage());
            System.exit(-1);
        }
    }

    private void sendMessage() {
        String message = t_input.getText();
        if (message.isEmpty()) return; // 입력창 비었으면 아무것도 안 함
        try {
            ((BufferedWriter)out).write(message + '\n');
            out.flush();
            t_display.append("나: " + message + "\n");
        }
        catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류 > " + e.getMessage());
            System.exit(-1);
        }
        t_input.setText(""); // 보낸 후 입력창은 비우기
    }

    private void receiveMessage() {
        try {
            String inMsg = ((BufferedReader)in).readLine();
            t_display.append("서버: \t" + inMsg + "\n");
        } catch (IOException e) {
            System.err.println("클라이언트 일반 수신 오류 > " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        new EchoClientGUI(serverAddress, serverPort);
    }
}


