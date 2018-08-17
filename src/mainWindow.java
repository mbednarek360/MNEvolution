import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import java.awt.Color;

import org.jfree.data.Range;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartPanel;


public class mainWindow {
    //init jframe variables
    private JSpinner tCompIn;
    private JPanel mainPanel;
    private JSpinner npgIn;
    private JSpinner mRateIn;
    private JButton mainButton;
    private JTextField datasetIn;
    private JPanel chartPanel;
    private JSpinner cRateIn;
    private JLabel targetOut;
    private JLabel bestOut;
    private JLabel bestGenOut;
    private JLabel bestNodeOut;
    private JCheckBox clearCheckIn;
    private JCheckBox smoothCheckIn;
    private JCheckBox overlapCheckIn;
    private JSpinner rRateIn;
    private JCheckBox aRangeCheckIn;
    private JSpinner spacingIn;

    //init config variables
    boolean active = false;
    boolean finished = false;
    int generation;
    int cRate;
    boolean smooth = false;
    boolean aRange = true;
    int lowLoss;
    boolean clearEnabled = true;
    int tComp;
    int[] best = new int[2];
    String dataset;
    char[][] target = {};
    Node[] nodes = {};
    int mRate;
    int npg;
    boolean overlap = true;
    int spacing;
    int rRate;

    // init graph variables
    XYSeriesCollection chartData;
    XYSeries lossLine;
    XYSeries minLossLine;
    JFreeChart chart;
    XYSplineRenderer sRend;
    XYPlot plot;
    XYLineAndShapeRenderer rend;
    NumberAxis range;


    public mainWindow() {
        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startEvolution();
            }
        });

        // setup graph
        chartData = new XYSeriesCollection();
        lossLine = new XYSeries("Loss");
        minLossLine = new XYSeries("Minimum Loss");
        chart = ChartFactory.createXYLineChart("Evolution", "Generation", "Loss", chartData);
        sRend = new XYSplineRenderer();
        sRend.setSeriesShapesVisible(0, false);
        sRend.setSeriesShapesVisible(1, true);
        rend =  new XYLineAndShapeRenderer();
        rend.setSeriesShapesVisible(0, false);
        rend.setSeriesShapesVisible(1, true);
        plot = (XYPlot)chart.getPlot();
        range = (NumberAxis)plot.getRangeAxis();
        chartData.addSeries(lossLine);
        chartData.addSeries(minLossLine);
        chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
        sRend.setSeriesPaint(0, new Color(0,0,255));
        sRend.setSeriesPaint(1, new Color(255,0,0));
        rend.setSeriesPaint(0, new Color(0,0,255));
        rend.setSeriesPaint(1, new Color(255,0,0));
        plot.setRenderer(rend);


        // default values
        tCompIn.setValue(4);
        npgIn.setValue(250);
        mRateIn.setValue(5);
        rRateIn.setValue(1000);
        cRateIn.setValue(10000);
        spacingIn.setValue(450);


        // action listeners
        clearCheckIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // toggle auto clear
                clearEnabled = !clearEnabled;
                cRateIn.setEnabled(clearEnabled);
            }
        });
        smoothCheckIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // toggle graph renderer
                smooth = !smooth;
                if (smooth) {
                    plot.setRenderer(sRend);
                }
                else {
                    plot.setRenderer(rend);
                }
            }
        });

        overlapCheckIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // toggle graph shape visibility
                overlap = !overlap;
                if (overlap) {
                    rend.setSeriesShapesVisible(1, true);
                }
                else {
                    rend.setSeriesShapesVisible(1, false);
                }
            }
        });
        aRangeCheckIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // toggle auto range scaling
                aRange = !aRange;
                rRateIn.setEnabled(aRange);

            }
        });
    }


    // window setup
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
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
    }

    // generate random matrix
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
        // get input values and set variables
        chartPanel.update(chartPanel.getGraphics());
        generation = 1;
        mRate = (Integer)mRateIn.getValue();
        npg = (Integer)npgIn.getValue();
        dataset = datasetIn.getText();
        tComp = (Integer)tCompIn.getValue();
        cRate = (Integer)cRateIn.getValue();
        rRate = (Integer)rRateIn.getValue();
        spacing = (Integer)spacingIn.getValue();
        target = genTarget(tComp, dataset);

        // print target matrix
        targetOut.setText("<html>Target Matrix:<br>");
        for (int i = 0; i < target.length; i++) {
            targetOut.setText(targetOut.getText() + (Arrays.toString(target[i]) + "<br>"));
        }
        targetOut.setText(targetOut.getText() + "</html>");

        // create new node array
        nodes = createNodes(npg);

        // calculate max loss and set it as current lowest
        lowLoss = (dataset.length() * (int)Math.pow(tComp, 2) * npg);

        // start evolution thread
        Thread generator = new Thread(new Runnable() {
            @Override
            public void run() {
                generate();
            }});
        generator.start();

    }

    // clear debug console
    public void clearOut() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void createUIComponents() {
    }

    // node class
    public class Node {
        public char[][] guess;
    }

    // tabulate loss of all nodes in a generation
    public int getTotalLoss() {
        int out = 0;
        for (int i = 0; i < npg; i++) {
            out += getLoss(nodes[i].guess);
        }
        return out;
    }

    // get difference between to charectars in the input dataset
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

    // loss function
    public int getLoss(char[][] guess) {
        int dist = 0;
        for (int m = 0; m < guess.length; m++) {
            for (int x = 0; x < guess.length; x++) {
                dist += dataGap(guess[m][x], target[m][x]);
            }
        }
        return dist;
    }

    // create new node array and fill with random guesses
    public Node[] createNodes(int nodeAmount) {
        Node[] nodeArr = new Node[nodeAmount];
        for(int n = 0; n < npg; n++) {
            nodeArr[n] = new Node();
            nodeArr[n].guess = genTarget(tComp, dataset);
        }
        return nodeArr;
    }

    public void breed() {
        // get best nodes
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

        // check if new lowest loss per generation
        if (getTotalLoss() < lowLoss) {
            lowLoss = getTotalLoss();
            bestOut.setText("<html>Best Matrix:<br>");
            bestGenOut.setText("Gen: " + generation);
            bestNodeOut.setText("Node: " + (best[0]+1));
            for (int i = 0; i < (nodes[(best[0])].guess).length; i++) {
                bestOut.setText(bestOut.getText() + (Arrays.toString(nodes[(best[0])].guess[i]) + "<br>"));
            }
            bestOut.setText(bestOut.getText() + "</html>");
            minLossLine.add((double)generation, (double)getTotalLoss());
            if (getLoss(nodes[(best[0])].guess) == 0) {
                finished = true;
            }
        }


        // swap guess chars with new nodes guess chars randomly
        char[][] nTemp = nodes[(best[0])].guess;
        char[][] nTemp1 = nodes[(best[1])].guess;
        nodes = createNodes(npg);

        for (int l = 0; l < npg; l++) {
            int r = ThreadLocalRandom.current().nextInt(0, npg);
            int r1 = ThreadLocalRandom.current().nextInt(0, tComp);
            int r2 = ThreadLocalRandom.current().nextInt(0, tComp);
            nodes[r].guess[r1][r2] = nTemp[r1][r2];

        }
        for (int l = 0; l < npg; l++) {
            int r = ThreadLocalRandom.current().nextInt(0, npg);
            int r1 = ThreadLocalRandom.current().nextInt(0, tComp);
            int r2 = ThreadLocalRandom.current().nextInt(0, tComp);
            nodes[r].guess[r1][r2] = nTemp1[r1][r2];

        }

        lossLine.add((double) generation, (double) getTotalLoss());

        // clear graph if enabled and on x generation
        if (clearEnabled) {

            if (generation % cRate == 0) {
                lossLine.clear();
                minLossLine.clear();
            }
        }

        // scale graph if enabled and on x generation
        if (aRange) {

            if (generation % rRate == 0 || generation == 1) {
                range.setRange(new Range(lowLoss - spacing, (getTotalLoss() + spacing)));
            }
        }

    }

    public void mutate() {
        for (int i = 0; i < (npg * (mRate / 100)); i++) {
            char[][] t = genTarget(tComp, dataset);
            Node[] nTemp;
            int r = ThreadLocalRandom.current().nextInt(0, npg);
            int r1 = ThreadLocalRandom.current().nextInt(0, tComp);
            int r2 = ThreadLocalRandom.current().nextInt(0, tComp);
            nodes[r].guess[r1][r2] = t[r1][r2];
        }
    }

    // loop breed function and iterate generation until matrix match foundd
    public void generate() {
        while (!finished) {
            breed();
            mutate();
            generation++;

        }
        //runs after evolution complete
        System.out.println("done");

    }

}
