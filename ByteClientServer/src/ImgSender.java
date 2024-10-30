import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ImgSender extends JFrame{
    JTextField t_input; // 입력창
    JTextPane t_display; // 상단의 디스플레이
    JButton b_connect, b_disconnect, b_select, b_exit, b_send; // 하단에 있는 3개의 버튼

    private DefaultStyledDocument document;

    private final String serverAddress;
    private final int serverPort;

    Socket socket;
    private OutputStream out;

    public ImgSender(String serverAddress, int serverPort) {
        super("ImageSender");

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

        // 기본으로 display할 document
        document = new DefaultStyledDocument();
        t_display = new JTextPane(document);
        t_display.setEditable(false);

        p.add(new JScrollPane(t_display), BorderLayout.CENTER);

        return p;
    }

    private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼

        JPanel p = new JPanel(new BorderLayout());

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendImage();
            }
        };

        t_input = new JTextField(30);
        t_input.addActionListener(listener);

        b_send = new JButton("보내기");
        b_send.addActionListener(listener);

        b_select = new JButton("선택하기");
        b_select.addActionListener(new ActionListener() {

            //파일 선택 객체
            JFileChooser chooser = new JFileChooser();

            @Override
            public void actionPerformed(ActionEvent e) {
                // 파일 확장자 필터, 선택사항이자 공식처럼 갖다 쓰면 되는 부분
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "JPG & GIF & PNG Images", // 파일 이름에 창에 출력될 문자열
                        "jpg", "gif", "png"); // 파일 필터로 사용되는 확장자

                        chooser.setFileFilter(filter);

                        //파일 선택 창 띄우기. 인자로 컨테이너에 대한 객체. ImgSender 프레임의 중앙에 띄운다.
                        int ret = chooser.showOpenDialog(ImgSender.this);
                        //파일이 정상적으로 선택되면 APPROVE_OPTION 리턴
                        if(ret != JFileChooser.APPROVE_OPTION) {
                            JOptionPane.showMessageDialog(ImgSender.this, "파일을 선택하지 않았습니다");
                            return;
                        }
                        // 절대경로 알아내서 띄워줌
                        t_input.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        p.add(t_input, BorderLayout.CENTER);
        JPanel p_button = new JPanel(new GridLayout(1,0));
        p_button.add(b_select);
        p_button.add(b_send);
        p.add(p_button, BorderLayout.EAST);

        t_input.setEnabled(false);
        b_select.setEnabled(false);
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
                b_select.setEnabled(true);
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
                b_select.setEnabled(false);
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
        t_input.setText("");
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

    private void sendImage() {
        //strip으로 불필요한 공백 없앰
        String filename = t_input.getText().strip();
        if (filename.isEmpty()) return;

        //입출력은 아니고 파일에 대한 정보만 가지고 있는 File
        File file = new File(filename);
        if (!file.exists()) {
            printDisplay(">> 파일이 존재하지 않습니다: " + filename + "\n");
            return;
        }

        BufferedInputStream bis = null;
        try {
            // 파일 내용 바로 보내는 게 아니라 파일명 먼저 보냄으로써 리시버 쪽에서 빈 파일 먼저 생성할 수 있도록 준비시킴
            ((DataOutputStream)out).writeUTF(file.getName());
            // 파일 크기 long으로 보냄
            ((DataOutputStream)out).writeLong(file.length());

            bis = new BufferedInputStream(new FileInputStream(file));

            //1KB 공간. 공간 크기는 상관 없음.
            byte[] buffer = new byte[1024];
            int nRead;
            // 데이터 읽어들이고 바로 출력 스트림에다 전달
            while ((nRead = bis.read(buffer)) != -1 ) {
                // buffer 버퍼의 0번 위치에서부터 nRead바이트 만큼의 정보 전달한다.
                out.write(buffer, 0, nRead);
            }

            // 이제 out 안 닫는다
            out.flush();

            printDisplay("전송이 완료했습니다: " + filename);
            t_input.setText("");

            ImageIcon icon = new ImageIcon(filename);
            printDisplay(icon);

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

        new ImgSender(serverAddress, serverPort);
    }
}


