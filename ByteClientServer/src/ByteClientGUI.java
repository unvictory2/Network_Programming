import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ByteClientGUI extends JFrame{ // 내가 프레임의 후손이 되는 방법. JBasicFrame1에서는 자기 안에 프레임을 뒀었다.
    private final String serverAddress;
    private final int serverPort;
    private OutputStream out;
    JButton b_connect, b_disconnect, b_exit; // 하단에 있는 3개의 버튼
    JTextArea t_display; // 상단의 디스플레이

    public ByteClientGUI(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        this.setBounds(500,200,400,300);
        this.setTitle("ByteClientGUI");
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
        JScrollPane scrollPane = new JScrollPane(); // 스크롤 되게
        scrollPane.add(t_display);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(t_display, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼

        JTextField t_input = new JTextField();
        t_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //엔터 누르면 전송하게
            }
        });

        JButton sendButton = new JButton("보내기");
        sendButton.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage(t_input.getText());
                t_input.setText(""); // 보낸 후 입력창은 비우기
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(t_input, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

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
            out = socket.getOutputStream();
            System.out.println("소캣 연결 성공");
        } catch (IOException e) {
            System.err.println("소캣 연결 오류 : " + e.getMessage());
            System.exit(-1);
        }
        b_connect.setEnabled(false);
        b_disconnect.setEnabled(true);
        b_exit.setEnabled(false);
    }

    private void sendMessage(String inputText) {
        if (inputText.isEmpty()) return; // 입력창 비었으면 아무것도 안 함
        else {
            try {
                out.write(Integer.parseInt(inputText)); // 정수로 바꾸기
            }
            catch (NumberFormatException e) { // 정수 아니면 오류
                System.err.println("정수가 아님! " + e.getMessage());
                return;
            } catch (IOException e) {
                System.err.println("클라이언트 쓰기 오류 > " + e.getMessage());
                System.exit(-1);
            }
            t_display.append("나: " + inputText + "\n");
        }
    }

    private void disconnect() {
        try {
            out.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류 > " + e.getMessage());
            System.exit(-1);
        }
        b_connect.setEnabled(true);
        b_disconnect.setEnabled(false);
        b_exit.setEnabled(true);
    }



    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        ByteClientGUI client = new ByteClientGUI(serverAddress, serverPort);

    }
}


