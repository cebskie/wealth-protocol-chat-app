import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class C {

    String serverAddress;
    Scanner in;
    PrintWriter out;

    private JFrame frame;
    private JTextField textField;
    private JPanel panelForMessages;

    public String name = null;
    JScrollPane scrollPane;
    JLabel messageLabel;

    JButton Group = new JButton("Group");
    JButton PC = new JButton("PC");

    public C(String serverAddress) {
        this.serverAddress = serverAddress;

        frame = new JFrame("Chatter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 800);
        frame.setLayout(new BorderLayout());

        JPanel panelInput = new JPanel();
        panelInput.setLayout(new BoxLayout(panelInput, BoxLayout.X_AXIS));
        panelInput.setBackground(new Color(250,250,250));
        panelInput.setOpaque(true);
        panelInput.setBorder(BorderFactory.createEmptyBorder(8, 7, 8, 7));

        RoundedPanel textFieldPanel = new RoundedPanel(25, 25);
        textFieldPanel.setBackground(new Color(230, 230, 230));
        textFieldPanel.setOpaque(true);
        textFieldPanel.setPreferredSize(new Dimension(455, 21));

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(new Color(230,230,230));
        textField.setOpaque(true);
        textField.setEditable(false);
        textField.setPreferredSize(new Dimension(450, 30));
        textField.setBorder(null);
        
        textFieldPanel.add(textField);
        panelInput.add(textFieldPanel);
        frame.add(panelInput, BorderLayout.SOUTH);

        panelForMessages = new JPanel();
        panelForMessages.setLayout(new BoxLayout(panelForMessages, BoxLayout.Y_AXIS));
        panelForMessages.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelForMessages.setBackground(new Color(230,230,230));
        panelForMessages.setOpaque(true);

        scrollPane = new JScrollPane(panelForMessages);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane, BorderLayout.CENTER);

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                if (message.toLowerCase().startsWith("/group")) {
                    out.println("Group"+ID(true));
                    textField.setText("");
                } else if (message.toLowerCase().startsWith("/pc")) {
                    out.println("PC"+ID(false));
                    textField.setText("");
                } else {
                    if (!message.isEmpty()) {
                        out.println(message); 
                        textField.setText("");
                    }
                }
            }
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection", JOptionPane.PLAIN_MESSAGE);
    }

    private String getPass(boolean is_new) {
        return JOptionPane.showInputDialog(frame, "Input Password:", is_new?"Input New Password":"Input Password", JOptionPane.PLAIN_MESSAGE);
    }

    private String Group(){
        int choice = JOptionPane.showConfirmDialog(frame, "Group Or Not", "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            return "Group"+ID(true);
        } else{
            return "PC"+ID(false);
        }
    }

    private String ID(boolean is_group){
        return JOptionPane.showInputDialog(frame, "Input Name", is_group?"Input Group Name":"Input Name",
        JOptionPane.PLAIN_MESSAGE);
    }

    private String groupPass(boolean is_new) {
        return JOptionPane.showInputDialog(frame, "Input Password:", is_new?"Input new Group Password":"Input Group Password",
        JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, 59001);
            System.out.println("connect");
            System.out.println(serverAddress);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NEWPASS")) {
                    out.println(getPass(true));
                }else if (line.startsWith("SUBMITPASS")) {
                    out.println(getPass(false));
                }else if (line.startsWith("NEWGROUPPASS")) {
                    out.println(groupPass(true));
                }else if (line.startsWith("SUBMITGROUPPASS")) {
                    out.println(groupPass(false));
                }else if (line.startsWith("NAMEACCEPTED")) {
                    name = line.substring(13);
                    out.println(Group());
                }else if(line.startsWith("CHATSTART")){
                    this.frame.setTitle("Chatter - " + line.substring(9));
                    textField.setEditable(true);
                    System.out.println(line);
                }else if (line.startsWith("MESSAGE")) {
                    String message = line.substring(8);
                    if (message.contains("has joined") || message.contains("has left")) {
                        appendAnnouncementToChat(message);  
                    } else {
                        appendMessageToChat(message, name); 
                    }
                }else if (line.startsWith("RESET") ){
                    textField.setEditable(false);
                    panelForMessages.removeAll();
                }
                System.out.println(line);
            }
            socket.close();
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    private void appendMessageToChat(String message, String name) {
        String[] msgPart = message.split(":", 2);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(440, 35));
        chatPanel.setMaximumSize(new Dimension(440, 35));
        chatPanel.setOpaque(false);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
        namePanel.setOpaque(false);

        JLabel nameLabel = new JLabel((msgPart[0].equals(name))? "You" : "<html><div style=width:100%;>"+msgPart[0]+"<div></html>" );
        nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        nameLabel.setForeground(new Color(56, 56, 56));
        namePanel.add(Box.createHorizontalGlue());
        namePanel.add(nameLabel);
        
        RoundedPanel messagePanel = new RoundedPanel(15,15);
        messagePanel.setOpaque(true);

        if (msgPart[0].equals(name)) {
            nameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            messagePanel.setBackground(new Color(151, 219, 124));
            messagePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        } else {
            nameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            namePanel.setAlignmentX(Box.RIGHT_ALIGNMENT);
            messagePanel.setBackground(new Color(255, 255, 255));
            messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        }

        if (msgPart[1].length() > 45) {
            messageLabel = new JLabel("<html><div style='width: 250px; word-wrap: break-word;'>" + msgPart[1] + "</div></html>");
            int height = Math.max(25, messageLabel.getPreferredSize().height+ 2*(int)Math.sqrt(messageLabel.getPreferredSize().height));
            messageLabel.setPreferredSize(new Dimension(370, height-5));
            messagePanel.setPreferredSize(new Dimension(370, messageLabel.getPreferredSize().height));
            chatPanel.setPreferredSize(new Dimension(440, messagePanel.getPreferredSize().height-10));
            chatPanel.setMaximumSize(new Dimension(440, messagePanel.getPreferredSize().height));
        } else {
            messageLabel = new JLabel(msgPart[1]);
            messageLabel.setPreferredSize(new Dimension(messageLabel.getPreferredSize().width + msgPart[1].length(), 25));
            messagePanel.setPreferredSize(new Dimension(messageLabel.getPreferredSize().width, 35));
        }

        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        messagePanel.add(messageLabel);
        if (msgPart[0].equals(name)) {
            chatPanel.add(messagePanel, BorderLayout.EAST);
        } else {
            chatPanel.add(messagePanel, BorderLayout.WEST);
        }
        panelForMessages.add(namePanel);
        panelForMessages.add(chatPanel);
        panelForMessages.add(Box.createVerticalStrut(10));
        panelForMessages.revalidate();
        panelForMessages.repaint();

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> {
            vertical.setValue(vertical.getMaximum() - vertical.getVisibleAmount());
        });
    }

    private void appendAnnouncementToChat(String announcement) {
        JPanel announcementPanel = new JPanel();
        announcementPanel.setOpaque(false);
        announcementPanel.setLayout(new BoxLayout(announcementPanel, BoxLayout.Y_AXIS)); 
    
        JLabel announcementLabel = new JLabel(announcement);
        announcementLabel.setFont(new Font("Arial", Font.BOLD, 14));
        announcementLabel.setForeground(Color.GRAY);
        announcementLabel.setOpaque(false);
        announcementLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        announcementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        announcementPanel.add(announcementLabel);
    
        panelForMessages.add(announcementPanel);
        panelForMessages.revalidate(); 
        panelForMessages.repaint(); 
    }

    public static void main(String[] args) throws Exception {
        String ip;
        if(args.length!=1){
            ip="127.0.0.1";
        } else {
            ip=args[0];
        }
        var client = new C(ip);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}

class RoundedPanel extends JPanel {
    private static int ARC_WIDTH ;
    private static int ARC_HEIGHT ;

    public RoundedPanel(int w, int h) {
        ARC_WIDTH = w;
        ARC_HEIGHT = h;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_WIDTH, ARC_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width += 20;  
        size.height += 20;
        return size;
    }
}