import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;

import static java.lang.Double.parseDouble;

public class CalcClientGUI extends JFrame{ // 내가 프레임의 후손이 되는 방법. JBasicFrame1에서는 자기 안에 프레임을 뒀었다.
    private final String serverAddress;
    private final int serverPort;
    private ObjectOutputStream out;
    private DataInputStream in;
    JButton b_connect, b_disconnect, b_exit; // 하단에 있는 3개의 버튼
    JTextField t_input_num1; // 입력창
    JTextField t_input_num2;
    JTextField t_input_op;
    JTextArea t_result;
    CalcExpr msg;

    public CalcClientGUI(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        this.setBounds(500,200,400,300);
        this.setTitle("CalcClientGUI");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    }

    private void buildGUI() {
            JPanel southPanel = new JPanel(new GridLayout(1,0)); // 아래에 갈 패널 준비
            southPanel.add(createControlPanel());
            this.add(createInputPanel(), BorderLayout.NORTH);
            this.add(southPanel, BorderLayout.SOUTH);
        }

    private JPanel createInputPanel() {

        t_input_num1 = new JTextField(5);
        t_input_op = new JTextField(5);
        t_input_num2 = new JTextField(5);
        JLabel t_expression = new JLabel("=");
        t_result = new JTextArea(1,5);
        t_result.setEditable(false);

        t_input_num1.setEditable(false);
        t_input_op.setEditable(false);
        t_input_num2.setEditable(false);

        JButton b_send = new JButton("계산");
        b_send.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
                receiveMessage();
            }
        });

        // FlowLayout으로 중앙 정렬
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // 중앙 정렬, 가로 10px, 세로 10px 간격

        panel.add(t_input_num1);
        panel.add(t_input_op);
        panel.add(t_input_num2);
        panel.add(t_expression);
        panel.add(t_result);
        panel.add(b_send);

        return panel;
    }

    private JPanel createControlPanel() { // 제일 밑단 버튼 3개, 접속하기 접속끊기 종료하기

            b_connect = new JButton("접속하기");
            b_connect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    connectToServer();
                    //접속 끊기 전에는 종료하거나 다시 접속하기 불가
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
        Socket socket;
        try {
            socket = new Socket(serverAddress, serverPort); // 소캣 연결
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            System.out.println("소캣 연결 성공");
        } catch (IOException e) {
            System.err.println("소캣 연결 오류 : " + e.getMessage());
            System.exit(-1);
        }
        b_connect.setEnabled(false);
        b_disconnect.setEnabled(true);
        b_exit.setEnabled(false);

        t_input_num1.setEditable(true);
        t_input_num2.setEditable(true);
        t_input_op.setEditable(true);
    }

    private void sendMessage() {
        CalcExpr msg = new CalcExpr(parseDouble(t_input_num1.getText()), (t_input_op.getText()).charAt(0),parseDouble(t_input_num2.getText()));
        try {
            out.writeObject(msg);
            out.flush();
        }
        catch (IOException e) {
            System.err.println("클라이언트 쓰기 오류 > " + e.getMessage());
            System.exit(-1);
        }
    }

    private void receiveMessage() {
        try {
            Double inMsg = in.readDouble();
            t_result.setText(Double.toString(inMsg));
        } catch (IOException e) {
            System.err.println("클라이언트 일반 수신 오류 > " + e.getMessage());
        }
    }


    private void disconnect() {
        try {
            if(out!=null) out.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류 > " + e.getMessage());
            System.exit(-1);
        }
        b_connect.setEnabled(true);
        b_disconnect.setEnabled(false);
        b_exit.setEnabled(true);

        t_input_num1.setEditable(false);
        t_input_num2.setEditable(false);
        t_input_op.setEditable(false);
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        CalcClientGUI client = new CalcClientGUI(serverAddress, serverPort);
    }
}


