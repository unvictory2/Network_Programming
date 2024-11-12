import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ByteServerGUI extends JFrame {
    int port;
    JTextArea t_display; // 상단의 텍스트필드. 이벤트 처리 등을 위해 현재 클래스의 다른 메서드에서도 접근이 필요한 요소는 참조변수의 선언을 멤버필드(클래스의 멤버변수를 선언하는 위치) 영역에 선언
    // 즉 기존에 createDisplayPanel()에서 생성하던 textArea는 다른 곳에서도 접근 가능해야 하니까 여기에 만든다. 이름수정.

    public ByteServerGUI(int port) {
        super("ByteServerGUI");
        this.port = port;

        buildGUI();
        this.setBounds(100,200,400,300);
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
            ServerSocket serverSocket = new ServerSocket(port);
            t_display.append("서버가 시작되었습니다.\n");
            while (true) { // 클라이언트 접속 기다림
                clientSocket = serverSocket.accept(); // 접속 받음
                t_display.append("클라이언트가 연결되었습니다\n");
                receiveMessages(clientSocket); // 해당 클라이언트가 보내는 메시지 받기
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

    private void receiveMessages(Socket cs) {
        InputStream in = null;
        try {
            in = cs.getInputStream();
            int message;
            while ((message = in.read()) != -1) { // -1이면 비었다는 뜻
                printDisplay("클라이언트 메시지: " + message); // 출력
            }
            t_display.append("클라이언트가 연결을 종료했습니다.\n");
        } catch (IOException e) {
            System.err.println("서버 읽기 오류 > " + e.getMessage());
            System.exit(-1);
        } finally {
            try {
                cs.close();
            } catch (IOException e) {
                System.err.println("서버 닫기 오류 > " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args) {
        int port = 54321;

        ByteServerGUI server = new ByteServerGUI(port);
        server.startServer();
    }

}
