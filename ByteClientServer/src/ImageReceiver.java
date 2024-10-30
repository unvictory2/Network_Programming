import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ImageReceiver extends JFrame {
    private int port;
    private ServerSocket serverSocket = null;

    private JTextPane t_display;
    private DefaultStyledDocument document;

    private Thread acceptThread = null;

    public ImageReceiver(int port) {
        super("ImageReceiver");

        buildGUI();

        setBounds(100, 200, 400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);

        this.port = port;
        acceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startServer();
            }
        });
        acceptThread.start();
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
        JPanel p = new JPanel(new BorderLayout());

        document = new DefaultStyledDocument();
        t_display = new JTextPane(document);
        t_display.setEditable(false);

        p.add(new JScrollPane(t_display), BorderLayout.CENTER);

        return p;
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

    private void printDisplay(String msg) {
        // 문자열 출력
        int len = t_display.getDocument().getLength();
        try {
            document.insertString(len, msg + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        t_display.setCaretPosition(len);
    }

    // 이미지 출력
    private void printDisplay(ImageIcon icon) {
        t_display.setCaretPosition(t_display.getDocument().getLength());

        // 이미지 아이콘 폭이 400 픽셀 넘으면 비율 유지하면서 폭을 400까지 줄임. 이미지 너무 크면 안 되니까.
        // 생소할 수 있는데 공식처럼 갖다 쓰면 된다.
        if (icon.getIconWidth() > 400) {
            Image img = icon.getImage();
            Image changeImg = img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
            icon = new ImageIcon(changeImg);
        }
        // 이미지를 아이콘화 한 객체를 textPane에 출력
        t_display.insertIcon(icon);

        // 줄바꿈 대신 빈 줄 하나
        printDisplay("");
    }

    private class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        private void receiveImage(Socket cs) {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(cs.getInputStream()));

                while (true) {
                    // in 스트림에서 전달되는 내용을 bos가 가르키는 파일에 순차적으로 저장 예정
                    String fileName = in.readUTF();
                    // 파일 크기 읽어오기
                    long size = (long)in.readLong();

                    File file = new File(fileName);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

                    // 1024바이트씩 내용 전달받기
                    byte[] buffer = new byte[1024];
                    int nRead;
                    // 읽을 거 남아있는 동안 계속 한다
                    while (size > 0) {
                        nRead = in.read(buffer);
                        size -= nRead;
                        bos.write(buffer, 0, nRead);
                    }

                    bos.close();

                    printDisplay("수신을 완료했습니다: " + file.getName());

                    ImageIcon icon = new ImageIcon(file.getName());
                    printDisplay(icon);
                }
            } catch (IOException e) {
                System.err.println("서버 읽기 오류 > " + e.getMessage());
            } finally {
                try {
                    printDisplay("클라이언트가 연결을 종료했습니다.");
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
            receiveImage(clientSocket);
        }
    }

    public static void main(String[] args) {
        int port = 54321;
        new ImageReceiver(port);
    }
}
