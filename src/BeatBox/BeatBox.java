package BeatBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.midi.*;

public class BeatBox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence seq;
    JFrame theFrame;
    Track track;
    String[] instrumentNames = new String[]{"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare",
            "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instruments = new int[]{35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }

    public void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartlistener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStoplistener());
        buttonBox.add(stop);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempolistener());
        buttonBox.add(downTempo);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempolistener());
        buttonBox.add(upTempo);

        JButton serializeIt = new JButton("Serialize it");
        serializeIt.addActionListener(new MySendlistener());
        buttonBox.add(serializeIt);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new MyReadInlistener());
        buttonBox.add(restore);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setHgap(2);
        grid.setVgap(1);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }
        setUpMIDI();

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    public void setUpMIDI() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            seq = new Sequence(Sequence.PPQ, 4);
            track = seq.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildtrackandStart() {
        int[] tracklist = null;
        seq.deleteTrack(track);
        track = seq.createTrack();
        for (int i = 0; i < 16; i++) {
            tracklist = new int[16];
            int key = instruments[i];
            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
                if (jc.isSelected()) {
                    tracklist[j] = key;
                } else {
                    tracklist[j] = 0;
                }
            }
            makeTrack(tracklist);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(seq);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void makeTrack(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];
            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
        }
        return event;
    }

    public class MyStartlistener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildtrackandStart();
        }
    }

    public class MyStoplistener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    public class MyUpTempolistener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    public class MyDownTempolistener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));
        }
    }
    public class MySendlistener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean [] checkBoxState = new boolean[256];
            for (int i = 0; i < checkBoxState.length; i++) {
                JCheckBox check = (JCheckBox)checkboxList.get(i);
                if (check.isSelected()){
                    checkBoxState[i] = true;
                }
            }
            try{
                JFileChooser saveFile = new JFileChooser();
                saveFile.showSaveDialog(theFrame);
                FileOutputStream fileStream = new FileOutputStream
                        (new File(String.valueOf(saveFile.getSelectedFile())));
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkBoxState);
                os.close();
            }catch (Exception ex){ex.printStackTrace();}
        }
    }
    public class MyReadInlistener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean [] checkBoxState = null;

            try{
                JFileChooser openFile = new JFileChooser();
                openFile.showOpenDialog(openFile);
                FileInputStream fileIn = new FileInputStream(new File(String.valueOf(openFile.getSelectedFile())));
                ObjectInput in = new ObjectInputStream(fileIn);
                checkBoxState = (boolean[])in.readObject();
            }catch (Exception ex){ex.printStackTrace();}
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox)checkboxList.get(i);
                if (checkBoxState[i]){
                    check.setSelected(true);
                }else {
                    check.setSelected(false);
                }
            }
            sequencer.stop();
            buildtrackandStart();
        }
    }
}
