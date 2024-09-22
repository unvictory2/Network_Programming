import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ByteClientGUI extends JFrame{ // 내가 프레임의 후손이 되는 방법. JBasicFrame1에서는 자기 안에 프레임을 뒀었음.

        public ByteClientGUI() {
            super("ByteClient GUI"); // super를 통해 부모 클래스의 명시적인 생성자 호출

            buildGUI();

            this.setBounds(100,200,300,400);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
        }

        private void buildGUI() {
            JPanel southPanel = new JPanel(new GridLayout(2,0)); // 아래에 갈 패널 준비
            southPanel.add(createInputPanel());
            southPanel.add(createControlPanel());

            this.add(createDisplayPanel(), BorderLayout.CENTER);
            this.add(southPanel, BorderLayout.SOUTH);
        }

        private JPanel createDisplayPanel() { // 최상단 JTextArea
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(); // 스크롤 되게
            scrollPane.add(textArea);

            JPanel panel = new JPanel(new BorderLayout());

            panel.add(textArea, BorderLayout.CENTER);

            return panel;
        }

        private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼

            JTextField inputTextField = new JTextField();
            inputTextField.addActionListener(new ActionListener() { // 이벤트 리스너
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // TODO
                }
            });

            JButton sendButton = new JButton("보내기");
            sendButton.addActionListener(new ActionListener() { // 이벤트 리스너
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // TODO
                }
            });

            JPanel panel = new JPanel(new BorderLayout());

            panel.add(inputTextField, BorderLayout.CENTER);
            panel.add(sendButton, BorderLayout.EAST);

            return panel;
        }

        private JPanel createControlPanel() { // 제일 밑단 버튼 3개

            JButton connectButton = new JButton("접속하기");
            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // TODO
                }
            });

            JButton disconnectButton = new JButton("접속 끊기");
            disconnectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // TODO
                }
            });

            JButton endButton = new JButton("종료하기");
            endButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // TODO
                }
            });

            JPanel panel = new JPanel(new GridLayout(0,3));

            panel.add(connectButton);
            panel.add(disconnectButton);
            panel.add(endButton);

            return panel;
        }
}
