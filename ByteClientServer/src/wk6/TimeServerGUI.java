import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TimeServerGUI extends JFrame {
    int port;
    ServerSocket serverSocket;
    JTextArea t_display; // 상단의 텍스트필드. 이벤트 처리 등을 위해 현재 클래스의 다른 메서드에서도 접근이 필요한 요소는 참조변수의 선언을 멤버필드(클래스의 멤버변수를 선언하는 위치) 영역에 선언
    // 즉 기존에 createDisplayPanel()에서 생성하던 textArea는 다른 곳에서도 접근 가능해야 하니까 여기에 만든다. 이름수정.

    public TimeServerGUI(int port) {
        super("TimeServerGUI");
        this.port = port;
        buildGUI();
        this.setBounds(100, 200, 400, 300);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    } // 생성자

    private void buildGUI() {

        this.add(createDisplayPanel(), BorderLayout.CENTER);
        this.add(createControlPanel(), BorderLayout.SOUTH);
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

    private void receiveMessages(Socket cs) {
        try {
//            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));

            String message;
//            while ((message = in.readLine()) != null) {
                message = cs.getInetAddress().getHostAddress() + ": " + new Date().toString();
                printDisplay("클라이언트 메시지: " + message);
                out.write(message + "\n"); // printWriter 사용했다면 write 대신 println을 이용해서 별도의 개행 문자 없이 출력 가능
                out.flush();
//            }
            t_display.append("클라이언트가 연결을 종료했습니다.\n");
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

    private class ClientHandler extends Thread {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        // 특정 소캣에 대해서 receiveMessages
        receiveMessages(clientSocket);
    }
}

    public static void main(String[] args) {
        int port = 54321;

        TimeServerGUI server = new TimeServerGUI(port);
        server.startServer();
    }
}
