package com.narola.digitalsignature;
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EDoc2Jnlp extends JPanel implements ActionListener {
    protected JTextField pin1Text;
    protected JTextField pin2Text;
    protected JButton generateButton;
    protected static String value="";
    protected JDialog d;
    private final static String newline = "\n";


    public EDoc2Jnlp() {
        super(new GridBagLayout());

        pin1Text = new JTextField(20);
        pin1Text.addActionListener(this);

        pin2Text = new JTextField(20);
        pin2Text.addActionListener(this);

        add(pin1Text);
        add(pin2Text);
        generateButton=new JButton("Generate");
        generateButton.addActionListener(this);
        add(generateButton);
        //JScrollPane scrollPane = new JScrollPane(textArea);
        JFrame f= new JFrame();
        d = new JDialog(f , "Dialog Example", true);
        d.setLayout( new FlowLayout() );
        d.add( new JLabel ("Passed Value"+value));
        d.setSize(300,300);
        d.setVisible(true);
        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;
        //add(textField, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        //add(scrollPane, c);
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getActionCommand()=="Generate") {
            String pin1 = pin1Text.getText();
            String pin2 = pin2Text.getText();
        }


        //Make sure the new text is visible, even if there
        //was a selection in the text area.

    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.

     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(new EDoc2Jnlp());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(final String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        value=args[0];
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();

            }
        });
    }
}