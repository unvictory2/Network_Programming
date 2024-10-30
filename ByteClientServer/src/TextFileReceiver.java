import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TextFileReceiver extends JFrame {
    int port;
    ServerSocket serverSocket = null;

    JTextArea t_display;

    public TextFileReceiver(int port) {
        super("TextFileReceiver");

        buildGUI();

        setBounds(100, 200, 400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);

        this.port = port;
        startServer();
    } // 생성자

    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private void startServer() {
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            printDisplay("서버가 시작됐습니다.");
            while (true) { // 클라이언트 접속 기다림
                clientSocket = serverSocket.accept(); // 접속 받음
                printDisplay("클라이언트가 연결됐습니다.");
                //스레드 생성해서 시키기
                ClientHandler cHandler = new ClientHandler(clientSocket);
                cHandler.start();
            }
        } catch (IOException e) {
            System.err.println("서버 소캣 생성 실패 : " + e.getMessage());
            System.exit(-1);
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

        JButton endButton = new JButton("종료");
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        JPanel panel = new JPanel(new GridLayout());
        panel.add(endButton);

        return panel;
    }

    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    private class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        private void receiveFile(Socket cs) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));

                    // 새 파일 만들어서 전달받은 내용 입력 예정
                    String fileName = in.readLine();
                    File file = new File(fileName);
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), true);

                    // 내용 전달받기
                    String line;
                    // TextFileSender에서 내용 다 보내고 out 스트림 닫아버린 이유가 여기서 null값 주기 위함임.
                    while ((line = in.readLine()) != null) {
                        pw.println(line);
                    }

                    printDisplay("수신을 완료했습니다: " + file.getName());
                    printDisplay("클라이언트가 연결을 종료했습니다.");
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
            receiveFile(clientSocket);
        }
    }

    public static void main(String[] args) {
        int port = 54321;
        new TextFileReceiver(port);
    }
}
