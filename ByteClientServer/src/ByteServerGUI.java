import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ByteServerGUI extends JFrame {
    int port;

    public ByteServerGUI(int port) {
        this.port = port;

        buildGUI();
        this.setBounds(100,200,300,400);
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
            System.out.println("서버 시작");
            while (true) { // 클라이언트 접속 기다림
                clientSocket = serverSocket.accept(); // 접속 받음
                System.out.println("클라이언트가 연결되었습니다.");
                receiveMessages(clientSocket); // 해당 클라이언트가 보내는 메시지 받기
            }
        } catch (IOException e) {
            System.err.println("서버 소캣 생성 실패 : " + e.getMessage());
            System.exit(-1);
        }
    }

    private JPanel createDisplayPanel() { // 최상단 JTextArea
        JTextArea textArea = new JTextArea("서버가 시작되었습니다.\n");
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(); // 스크롤 되게
        scrollPane.add(textArea);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() { // 제일 밑단 종료 버튼

        JButton endButton = new JButton("종료");
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // TODO
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
            System.out.println("클라이언트가 연결을 종료했습니다.");
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
    //!!!!문제발생!!!! textArea는 지역 변수니까 printDisplay가 알 수가 없다.
    private void printDisplay(String message) {
        textArea.append(message + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }



    public static void main(String[] args) {
        int port = 54321;

        ByteServerGUI server = new ByteServerGUI(port);
        server.startServer();
    }

}
