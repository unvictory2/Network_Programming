import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServerGUI extends JFrame {
    int port;
    ServerSocket serverSocket = null;

    JTextArea t_display;

    public EchoServerGUI(int port) {
        super("EchoServerGUI");

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

    private void receiveMessages(Socket cs) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));

            String message;
            while ((message = in.readLine()) != null) {
                printDisplay("클라이언트 메시지: " + message);
                out.write("'" + message + "' ...echo\n");
                out.flush();
            }
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
        new EchoServerGUI(port);
    }
}
