import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class FileSender extends JFrame{
    JTextField t_input; // 입력창
    JTextArea t_display; // 상단의 디스플레이
    JButton b_connect, b_disconnect, b_exit, b_send; // 하단에 있는 3개의 버튼

    private final String serverAddress;
    private final int serverPort;

    Socket socket;
    private OutputStream out;

    public FileSender(String serverAddress, int serverPort) {
        super("FileSender");

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

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        };

        t_input = new JTextField(30);
        t_input.addActionListener(listener);

        b_send = new JButton("보내기");
        b_send.addActionListener(listener);

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

    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort); // 소캣 연결
            // 파일이니까 바이트로. 바이트 단위로 하기 번거로우니까 버퍼에 연결 후 Data로.
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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

            //PrintWriter는 예외처리 불필요
        try {
            ((DataOutputStream)out).writeUTF(message + "\n");
            out.flush();

            t_display.append("나: " + message + "\n");
        } catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
            System.exit(-1);
        }

        t_input.setText(""); // 보낸 후 입력창은 비우기
    }

    private void sendFile() {
        //strip으로 불필요한 공백 없앰
        String filename = t_input.getText().strip();
        if (filename.isEmpty()) return;

        //입출력은 아니고 파일에 대한 정보만 가지고 있는 File
        File file = new File(filename);
        if (!file.exists()) {
            t_display.append(">> 파일이 존재하지 않습니다: " + filename + "\n");
            return;
        }

        BufferedInputStream bis = null;
        try {
            // 파일 내용 바로 보내는 게 아니라 파일명 먼저 보냄으로써 리시버 쪽에서 빈 파일 먼저 생성할 수 있도록 준비시킴
            ((DataOutputStream)out).writeUTF(filename);

            bis = new BufferedInputStream(new FileInputStream(file));

            //1KB 공간. 공간 크기는 상관 없음.
            byte[] buffer = new byte[1024];
            int nRead;
            // 데이터 읽어들이고 바로 출력 스트림에다 전달
            while ((nRead = bis.read(buffer)) != -1 ) {
                // buffer 버퍼의 0번 위치에서부터 nRead바이트 만큼의 정보 전달한다.
                out.write(buffer, 0, nRead);
            }
            // out은 connectToServer에서 만들어지기에 이후 또 전달하고 싶으면 재접속 해야함. 클라의 전송 끝났음을 서버에게 알릴 방법. 이후 수정.
            out.close();

            printDisplay("전송이 완료했습니다: " + filename);
            t_input.setText("");

        } catch (FileNotFoundException e) {
            // 위에서 파일 없는 경우 이미 처리했지만 필수임
            printDisplay(">> 파일이 존재하지 않습니다: " + e.getMessage() + "\n");
            return;
        } catch (IOException e) {
            printDisplay(">> 파일을 읽을 수 없습니다: " + e.getMessage() + "\n");
            return;
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException e) {
                printDisplay(">> 파일을 닫을 수 없습니다: " + e.getMessage() + "\n");
                return;
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        new FileSender(serverAddress, serverPort);
    }
}


