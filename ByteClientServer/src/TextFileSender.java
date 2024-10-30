import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class TextFileSender extends JFrame{
    JTextField t_input; // 입력창
    JTextArea t_display; // 상단의 디스플레이
    JButton b_connect, b_disconnect, b_exit, b_send; // 하단에 있는 3개의 버튼

    private final String serverAddress;
    private final int serverPort;

    Socket socket;
    private Writer out;

    public TextFileSender(String serverAddress, int serverPort) {
        super("TextFileSender");

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

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort); // 소캣 연결
            // 개행 문자 처리를 용이하게 하기 위해 PrintWriter사용해봄, Buffer는 내부에 이미 있어서 불필요
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
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
            ((PrintWriter)out).println(message);
            t_display.append("나: " + message + "\n");

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

        // 파일 내용 바로 보내는 게 아니라 파일명 먼저 보냄으로써 리시버 쪽에서 빈 파일 먼저 생성할 수 있도록 준비시킴
        ((PrintWriter)out).println(filename);

        BufferedReader br = null;
        // 파일 전달하는 FileInput은 바이트 스트림이여서 InputStream으로 문자 스트림으로 변환. 이후 Buffered에 넣어서 행단위로 전달할 수 있게.
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                ((PrintWriter)out).println(line);
            }
            // out은 connectToServer에서 만들어지기에 이후 또 전달하고 싶으면 재접속 해야함. 클라의 전송 끝났음을 서버에게 알릴 방법. 이후 수정.
            out.close();

            t_display.append("전송이 완료했습니다: " + filename + "\n");
            t_input.setText("");

        } catch (UnsupportedEncodingException e){
            t_display.append(">> 인코딩 형식을 알 수 없습니다: " + e.getMessage() + "\n");
            return;
        } catch (FileNotFoundException e) {
            // 위에서 파일 없는 경우 이미 처리했지만 필수임
            t_display.append(">> 파일이 존재하지 않습니다: " + e.getMessage() + "\n");
            return;
        } catch (IOException e) {
            t_display.append(">> 파일을 읽을 수 없습니다: " + e.getMessage() + "\n");
            return;
        } finally { // 영상에서 설명 빼먹으셨다 함
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                t_display.append(">> 파일을 닫을 수 없습니다: " + e.getMessage() + "\n");
                return;
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        new TextFileSender(serverAddress, serverPort);
    }
}


