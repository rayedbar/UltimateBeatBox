package ultimatebeatbox;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Rayed Bin Wahed 12201114 CSE BRAC University
 *         CSE310 Final Project: Client-Server Chat Beat Box
 * 
 */

public class UltimateBeatBox {

    private String mUserName;
    private final String[] mInstrumentName;
    private final int[] mInstruments;
    private final ArrayList<JCheckBox> mCheckBoxList;
    private final Vector<String> mListVector;
    
    private JFrame mFrame;
    private JTextField mUserMessage;
    private JList mIncomingList;
    
    public static void main(String[] args) {
        new UltimateBeatBox().startUp();
    }
    
    private UltimateBeatBox() {
        this.mCheckBoxList = new ArrayList<>();
        this.mListVector = new Vector<>();
        this.mInstrumentName = new String [] {
            "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", 
            "Acoustic Snare", "Crash Cymbal", "Hand Clap", 
            "High Tom", "Hi Bongo", "Maracas", 
            "Whistle", "Low Conga", "Cowbell", 
            "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi COnga"
        };
        this.mInstruments = new int [] {
            35, 42, 46, 38, 49, 39, 50, 60, 
            70, 72, 64, 56, 58, 47, 67, 63
        };
    }
    
    private void startUp() {
        System.out.print("Enter User Name: ");
        Scanner scanner = new Scanner(System.in);
        mUserName = scanner.nextLine();
        try {
            Socket socket = new Socket("127.0.0.1", 7777);
        } catch (IOException ex) {
            Logger.getLogger(UltimateBeatBox.class.getName()).log(Level.SEVERE, null, ex);
        }
        buildGUI();
        setUpMIDI();
      
    } 

    private void buildGUI() {
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton starButton = new JButton("Start");
        starButton.addActionListener(new StartListener());
        buttonBox.add(starButton);
        
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new StopListener());
        buttonBox.add(stopButton);
        
        JButton tempoUpButton = new JButton("Tempo Up");
        tempoUpButton.addActionListener(new TempoUpListener());
        buttonBox.add(tempoUpButton);
        
        JButton tempoDownButton = new JButton("Tempo Down");
        tempoDownButton.addActionListener(new TempoDownListener());
        buttonBox.add(tempoDownButton);
        
        JButton sendItButton = new JButton("Send It");
        sendItButton.addActionListener(new SendItListener());
        buttonBox.add(sendItButton);
        
        mUserMessage = new JTextField();
        buttonBox.add(mUserMessage);
        
        mIncomingList = new JList();
        mIncomingList.setListData(mListVector);
        mIncomingList.addListSelectionListener(new MyListSelectionListener());
        mIncomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(mIncomingList);
        buttonBox.add(scrollPane);
        
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; ++i){
            nameBox.add(new Label(mInstrumentName[i]));
        }
        
        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        JPanel mMainPanel = new JPanel(grid);
        
        for (int i = 0; i < 256; ++i){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            mCheckBoxList.add(c);
            mMainPanel.add(c);
        }
        
        background.add(BorderLayout.CENTER, mMainPanel);
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        
        mFrame = new JFrame("Ultimate Beat Box");
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrame.getContentPane().add(background);
        mFrame.setBounds(50, 50, 300, 300);
        mFrame.pack();
        mFrame.setVisible(true); 
    }
    
    private void setUpMIDI(){
        
    }

    private class StartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }


    private class StopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    private class TempoUpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private class TempoDownListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    
    private class SendItListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    private class MyListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
