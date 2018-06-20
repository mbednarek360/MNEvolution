import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Random;


public class mainWindow {
    private JSpinner tCompIn;
    private JPanel mainPanel;
    private JSpinner npgIn;
    private JSpinner mRateIn;
    private JButton mainButton;
    private JTextField datasetIn;
    private JPanel chartPanel;
    boolean active;
    int generation;
    int tComp;
    int[] best = new int[2];
    String dataset;
    char[][] target = {};
    Node[] nodes = {};
    int mRate;
    int npg;

    public mainWindow() {
        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startEvolution();
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame = new JFrame("MNEvolution");
        frame.setContentPane(new mainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int)(screenSize.getWidth()*0.125), (int)(screenSize.getHeight()*0.25));
        //frame.pack();
        //frame.setResizable(false);
        frame.setVisible(true);
    }

    public char[][] genTarget(int comp, String set) {
        char[][] t = new char[comp][comp];
        final String alphabet = set;
        final int N = alphabet.length();
        for (int i = 0; i < comp; i++) {
            for (int n = 0; n < comp; n++) {
                Random r = new Random();
                t[n][i] = alphabet.charAt(r.nextInt(N));
            }
        }
        return t;
    }


    public void startEvolution() {
        generation = 1;
        mRate = (Integer)mRateIn.getValue();
        npg = (Integer)npgIn.getValue();
        dataset = datasetIn.getText();
        tComp = (Integer)tCompIn.getValue();
       target = genTarget(tComp, dataset);
       clearOut();
       System.out.println("Target Matrix:");
        for (int i = 0; i < target.length; i++) {
            System.out.println(Arrays.toString(target[i]));
        }
        nodes = createNodes(npg);
        //System.out.println(getLoss(nodes[0].guess));
        generate();






    }


    public void clearOut() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public class Node {
        public char[][] guess;
    }

    public int getTotalLoss() {
        int out = 0;
        for (int i = 0; i < npg; i++) {
            out += getLoss(nodes[i].guess);
        }
        return out;
    }


    public int dataGap(char a, char b) {
        int l1 = 0;
        int l2 = 0;
        for (int n = 0; n < dataset.length(); n++) {
            if (dataset.charAt(n) == a) {
                l1 = n;
            }
        }
        for (int y = 0; y < dataset.length(); y++) {
            if (dataset.charAt(y) == b) {
                l2 = y;
            }
        }
        if (l1 > l2) {
            return (l1-l2);
        }
        else if (l1 < l2) {
            return (l2-l1);
        }
        else {
            return 0;
        }

    }

    //loss function
   public int getLoss(char[][] guess) {
        int dist = 0;
        for (int m = 0; m < guess.length; m++) {
            for (int x = 0; x < guess.length; x++) {
                dist += dataGap(guess[m][x], target[m][x]);
            }
        }
    return dist;
    }


    public Node[] createNodes(int nodeAmount) {
        Node[] nodeArr = new Node[nodeAmount];
        System.out.println();
        System.out.println("Nodes:");
        for(int n = 0; n < npg; n++) {
            nodeArr[n] = new Node();
            nodeArr[n].guess = genTarget(tComp, dataset);
            System.out.println("Created node #" + (n+1) + ".");
        }
        return nodeArr;
    }

    public void breed() {
        //get best nodes
        int temp = getLoss(nodes[0].guess);
        for (int m = 0; m < npg; m++) {
            if (getLoss(nodes[m].guess) < temp) {
                temp = getLoss(nodes[m].guess);
                best[0] = m;
            }
        }
        if (best[0] < 1) {
            temp = getLoss(nodes[(best[0] + 1)].guess);
        }
        else {
            temp = getLoss(nodes[(best[0] - 1)].guess);
        }
        for (int i = 0; i < npg; i++) {
            if (getLoss(nodes[i].guess) < temp && i != best[0]) {
                temp = getLoss(nodes[i].guess);
                best[1] = i;
            }
        }
        //regen nodes
        nodes = createNodes(npg);
        //swap guess chars








    }


    public void mutate(Node n) {

    }

    public void generate() {
        while (getTotalLoss() != 0) {


















            generation++;

        }
    }


}
