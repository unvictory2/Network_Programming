import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ByteClientGUI extends JFrame{ // 내가 프레임의 후손이 되는 방법. JBasicFrame1에서는 자기 안에 프레임을 뒀었다.
    private String serverAddress;
    private int serverPort;
    private OutputStream out;

    public ByteClientGUI(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        this.setBounds(100,200,300,400);
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
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(); // 스크롤 되게
        scrollPane.add(textArea);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼

        JTextField inputTextField = new JTextField();

        JButton sendButton = new JButton("보내기");
        sendButton.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage(inputTextField.getText());
                inputTextField.setText(""); // 보낸 후 입력창은 비우기
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(inputTextField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
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
        }
    }

    private JPanel createControlPanel() { // 제일 밑단 버튼 3개

            JButton connectButton = new JButton("접속하기");
            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    connectToServer();
                }
            });

            JButton disconnectButton = new JButton("접속 끊기");
            disconnectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    disconnect();
                }
            });

            JButton endButton = new JButton("종료하기");
            endButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // TODO
                }
            });

            JPanel panel = new JPanel(new GridLayout(0,3));

            panel.add(connectButton);
            panel.add(disconnectButton);
            panel.add(endButton);

            return panel;
        }

    private void disconnect() {

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
    }

    private void

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        ByteClientGUI client = new ByteClientGUI(serverAddress, serverPort);

    }
}


