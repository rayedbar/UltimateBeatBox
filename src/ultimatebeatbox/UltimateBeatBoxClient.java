/**
 *
 * @author Rayed Bin Wahed - 12201114 BRAC University CSE310 Final Project:
 * Client-Server Beat Box Application
 *
 * Client Code
 */
package ultimatebeatbox;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UltimateBeatBoxClient {

    private JFrame mFrame;
    private JPanel mMainPanel;
    private JTextField mUserMessage;
    private JList mIncomingList;

    private String mUserName;

    private final ArrayList<JCheckBox> mCheckBoxList;
    private final Vector<String> mListVector;
    private final HashMap<String, boolean[]> othersSequenceMap;
    private final String[] mInstrumentNames;
    private final int[] mInstruments;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    public static void main(String[] args) {
        new UltimateBeatBoxClient().startUp();
    }

    public UltimateBeatBoxClient() {
        this.mCheckBoxList = new ArrayList<>();
        this.mListVector = new Vector<>();
        this.othersSequenceMap = new HashMap<>();
        this.mInstrumentNames = new String[]{
            "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat",
            "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas",
            "Whistle", "Low Conga", "Cowbell",
            "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi COnga"
        };
        this.mInstruments = new int[]{
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
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (IOException ex) {
            Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        buildGUI();
        setUpMIDI();
    }

    private void buildGUI() {
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(new StartListener());
        buttonBox.add(startButton);

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
        for (int i = 0; i < 16; ++i) {
            nameBox.add(new Label(mInstrumentNames[i]));
        }

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mMainPanel = new JPanel(grid);

        for (int i = 0; i < 256; ++i) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            mCheckBoxList.add(c);
            mMainPanel.add(c);
        }

        background.add(BorderLayout.CENTER, mMainPanel);
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new SaveMenuItemListener());
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(new OpenMenuItemListener());
        JMenuItem clearMenuItem = new JMenuItem("Clear");
        clearMenuItem.addActionListener(new ClearMenuItemListener());

        fileMenu.add(saveMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(clearMenuItem);
        menuBar.add(fileMenu);

        mFrame = new JFrame("Ultimate Beat Box");
        mFrame.setJMenuBar(menuBar);
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrame.getContentPane().add(background);
        mFrame.setBounds(50, 50, 300, 300);
        mFrame.pack();
        mFrame.setVisible(true);
    }

    private void setUpMIDI() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.setTempoInBPM(120);
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
        } catch (MidiUnavailableException | InvalidMidiDataException ex) {
            Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildTrackAndLaunch() {
        ArrayList<Integer> trackList;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; ++i) {
            trackList = new ArrayList<>();
            for (int j = 0; j < 16; ++j) {
                JCheckBox checkBox = mCheckBoxList.get(j + 16 * i);
                if (checkBox.isSelected()) {
                    int instrumentNumber = mInstruments[i];
                    trackList.add(instrumentNumber);
                } else {
                    trackList.add(null);
                }
            }
            makeTracks(trackList);
        }
        try {
            track.add(makeEvent(192, 9, 1, 0, 15));
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.setTempoInBPM(120);
            sequencer.start();
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void makeTracks(ArrayList trackList) {
        Iterator it = trackList.iterator();
        for (int i = 0; i < 16; ++i) {
            Integer num = (Integer) it.next();
            if (num != null) {
                track.add(makeEvent(144, 9, num, 100, i));
                track.add(makeEvent(128, 9, num, 100, i + 2));
            }
        }
    }

    private MidiEvent makeEvent(int command, int channel, int note, int velocity, int beat) {
        MidiEvent event = null;
        try {
            ShortMessage message = new ShortMessage(command, channel, note, velocity);
            event = new MidiEvent(message, beat);
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return event;
    }

    private void changeSequence(boolean[] checkBoxState) {
        for (int i = 0; i < 256; ++i) {
            if (checkBoxState[i]) {
                mCheckBoxList.get(i).setSelected(true);
            } else {
                mCheckBoxList.get(i).setSelected(false);
            }
        }
    }

    private class RemoteReader implements Runnable {

        boolean[] checkBoxState = null;
        String senderName = null;
        Object object = null;

        @Override
        public void run() {
            try {
                while ((object = ois.readObject()) != null) {
                    System.out.println("Received an object from the server");
                    System.out.println(object.getClass());
                    senderName = (String) object;
                    checkBoxState = (boolean[]) ois.readObject();
                    othersSequenceMap.put(senderName, checkBoxState);
                    mListVector.add(senderName);
                    mIncomingList.setListData(mListVector);
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class StartListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndLaunch();
        }
    }

    private class StopListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    private class TempoUpListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    private class TempoDownListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }

    private class SendItListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkBoxState = new boolean[256];
            for (int i = 0; i < 256; ++i) {
                if (mCheckBoxList.get(i).isSelected()) {
                    checkBoxState[i] = true;
                }
            }
            try {
                oos.writeObject(mUserName + ": " + mUserMessage.getText());
                oos.writeObject(checkBoxState);
            } catch (IOException ex) {
                Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            mUserMessage.setText("");
        }
    }

    private class MyListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                String selected = (String) mIncomingList.getSelectedValue();
                if (selected != null) {
                    changeSequence(othersSequenceMap.get(selected));
                    sequencer.stop();
                    buildTrackAndLaunch();
                }

            }
        }
    }

    private class SaveMenuItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //System.out.println("you clicked on save");
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(mFrame);
            File file = fileChooser.getSelectedFile();
            saveFile(file);
        }

        private void saveFile(File file) {
            boolean[] checkBoxState = new boolean[256];
            for (int i = 0; i < 256; ++i) {
                if (mCheckBoxList.get(i).isSelected()) {
                    checkBoxState[i] = true;
                }
            }
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(checkBoxState);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class OpenMenuItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //System.out.println("you clicked on opoen");
            JFileChooser fileOpener = new JFileChooser();
            fileOpener.showOpenDialog(mFrame);
            File file = fileOpener.getSelectedFile();
            openFile(file);
        }

        private void openFile(File file) {
            boolean[] checkBoxState;
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                checkBoxState = (boolean[]) in.readObject();
                changeSequence(checkBoxState);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(UltimateBeatBoxClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    private class ClearMenuItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 256; ++i){
                if(mCheckBoxList.get(i).isSelected())
                    mCheckBoxList.get(i).setSelected(false);
            }
        }
    }

}
