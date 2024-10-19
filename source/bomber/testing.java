package bomber;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class testing {
    Icon up = new ImageIcon("images/assets/up.png");
    Icon down = new ImageIcon("images/assets/down.png");

    public JPanel createReorderablePanel(JLabel[] labels) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (int i = 0; i < labels.length; i++) {
            JPanel itemPanel = createItemPanel(labels[i], panel);
            panel.add(itemPanel);
        }

        return panel;
    }

    private JPanel createItemPanel(JLabel label, JPanel parentPanel) {
        JPanel itemPanel = new JPanel(new BorderLayout(4,4));

        // Panel to hold the label
        JPanel labelPanel = new JPanel();
        labelPanel.add(label);

        // Panel to hold the buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        //gbc.fill=GridBagConstraints.NONE;
        
        JButton upButton = new JButton("");
        upButton.setIcon(up);
        upButton.setOpaque(false);
        upButton.setBackground(new Color(0,0,0,0));
        upButton.setPreferredSize(new Dimension(20,20));
        upButton.setFocusable(false);
        upButton.setBorder(null);
        JButton downButton = new JButton("");
        downButton.setIcon(down);
        downButton.setOpaque(false);
        downButton.setBackground(new Color(0,0,0,0));
        downButton.setFocusable(false);
        downButton.setPreferredSize(new Dimension(20,20));
        downButton.setBorder(null);

        // Move up action
        upButton.addActionListener(e -> moveItem(itemPanel, parentPanel, -1));

        // Move down action
        downButton.addActionListener(e -> moveItem(itemPanel, parentPanel, 1));

        buttonPanel.add(upButton,gbc);
        buttonPanel.add(downButton,gbc);
        itemPanel.add(label, BorderLayout.CENTER);
        
        itemPanel.add(buttonPanel, BorderLayout.WEST);

        return itemPanel;
    }

    private void moveItem(JPanel itemPanel, JPanel parentPanel, int direction) {
        int index = parentPanel.getComponentZOrder(itemPanel);
        int newIndex = index + direction;

        // Ensure the new index is within bounds
        if (newIndex >= 0 && newIndex < parentPanel.getComponentCount()) {
            parentPanel.remove(itemPanel);
            parentPanel.add(itemPanel, newIndex);
            parentPanel.revalidate();
            parentPanel.repaint();
        }
    }

    // Test the panel
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Reorderable JLabel Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            testing reorderablePanel = new testing();
            JLabel[] labels = {
                new JLabel("Item 1"),
                new JLabel("Item 2"),
                new JLabel("Item 3"),
                new JLabel("Item 4")
            };

            JPanel panel = reorderablePanel.createReorderablePanel(labels);
            frame.add(new JScrollPane(panel), BorderLayout.CENTER);

            frame.setSize(300, 300);
            frame.setVisible(true);
        });
    }
}
