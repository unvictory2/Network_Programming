import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ByteServerGUI extends JFrame {

    public ByteServerGUI() {
        super("ByteServer GUI"); // super를 통해 부모 클래스의 명시적인 생성자 호출

        buildGUI();

        this.setBounds(100,200,300,400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true); //this는 전부 필수 아니지만 있는 게 나음
    }

    private void buildGUI() {

        this.add(createDisplayPanel(), BorderLayout.CENTER);
        this.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() { // 최상단 JTextArea
        JTextArea textArea = new JTextArea("서버가 시작되었습니다.");
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(); // 스크롤 되게
        scrollPane.add(textArea);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() { // 제일 밑단 버튼 3개

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
}
