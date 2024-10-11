import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class CalcServerGUI extends JFrame {
    int port;
    ServerSocket serverSocket;
    JTextArea t_display;
    CalcExpr message;
    ObjectInputStream in;

    public CalcServerGUI(int port) {
        super("CalcServerGUI");
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
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(cs.getOutputStream()));
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(cs.getInputStream()));

            while(true) {
                message = (CalcExpr) in.readObject();
                double result = calculate(message);

                printDisplay(Double.toString(message.operand1) + " " + message.operator + " " + Double.toString(message.operand2) + " = " + Double.toString(result));
                out.writeDouble(result);
                out.flush();
            }

        } catch (IOException e) {
            t_display.append("클라이언트가 연결을 종료했습니다.\n");
            try {
                cs.close();
            } catch (IOException ex) {
                System.err.println("서버 닫기 오류 > " + e.getMessage());
                System.exit(-1);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("클래스를 찾지 못함 > " + e.getMessage());
            System.exit(-1);
        }
    }

    private double calculate(CalcExpr message) {
        double result = 0;
        switch (message.operator) {
            case '+':
                result = message.operand1 + message.operand2;
                break;
            case '-':
                result = message.operand1 - message.operand2;
                break;
            case '*':
                result = message.operand1 * message.operand2;
                break;
            case '/':
                if (message.operand2 != 0) {
                    result = message.operand1 / message.operand2;
                } else {
                    System.out.println("오류: 0으로 나눌 수 없습니다.");
                    System.exit(-1);
                }
                break;
            default:
                System.out.println("오류: 잘못된 연산자입니다.");
                System.exit(-1);
        }
        result = Math.round(result * 100.0) / 100.0;
        return result;
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

        CalcServerGUI server = new CalcServerGUI(port);
        server.startServer();
    }
}
