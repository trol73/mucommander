package com.mucommander.ui.main.statusbar;

import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.TogglePerformanceMonitorAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.text.SizeFormat;
import com.mucommander.utils.text.Translator;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PerformanceMonitorDialog extends FocusDialog implements ActionListener {

    private static class GraphData {

        private final long freeMemoryData;
        private final long totalMemoryData;
        private final double systemLoadAverage;

        private GraphData() {
            this.freeMemoryData = PerformanceMonitorDialog.getFreeMemoryData();
            this.totalMemoryData = PerformanceMonitorDialog.getTotalMemoryData();
            this.systemLoadAverage = PerformanceMonitorDialog.getSystemLoadAverage();
        }

        private long getFreeMemoryData() {
            return freeMemoryData;
        }

        private long getTotalMemoryData() {
            return totalMemoryData;
        }

        private double getSystemLoadAverage() {
            return systemLoadAverage;
        }

    }

    /**
     * Panel for displaying memory usage information
     */
    private static class MemoryPanel extends JPanel implements Runnable {

        private static final Font FONT = new Font("Dialog", Font.PLAIN, 11);
        private static final Color GRAPH_COLOR = new Color(46, 139, 87);
        private static final Color FREE_DATA_COLOR = new Color(0, 100, 0);
        private static final Color INFO_COLOR = Color.GREEN;
        private static final Color BACKGROUND = Color.BLACK;
        private static final Color DATA_COLOR = Color.YELLOW;

        private Thread thread;
        private int w;
        int h;
        Graphics2D graphics;
        private BufferedImage bufferedImage;
        private int columnInc;
        int ascent;
        int descent;
        private Rectangle graphOutlineRect = new Rectangle();
        private Rectangle2D mfRect = new Rectangle2D.Float();
        private Rectangle2D muRect = new Rectangle2D.Float();
        private Line2D graphLine = new Line2D.Float();

        MemoryPanel() {
            setBackground(BACKGROUND);
        }

        protected float getFreeData(GraphData graphData) {
            return graphData.getFreeMemoryData();
        }

        protected float getTotalData(GraphData graphData) {
            return graphData.getTotalMemoryData();
        }

        protected String getMaxValueString(float max) {
            return SizeFormat.format((long) max, MemoryLabel.MEMORY_INFO_SIZE_FORMAT);
        }

        protected String getCurrentValueString(float min) {
            return SizeFormat.format((long) min, MemoryLabel.MEMORY_INFO_SIZE_FORMAT);
        }

        @Override
        public void paint(Graphics g) {
            if (graphics == null) {
                return;
            }

            graphics.setBackground(getBackground());
            graphics.clearRect(0, 0, w, h);

            java.util.List<GraphData> dataToPaint;
            synchronized (GRAPH_DATA) {
                dataToPaint = new ArrayList<>(GRAPH_DATA);
            }
            final GraphData currentGraphData = dataToPaint.get(dataToPaint.size() - 1);
            final float freeData = getFreeData(currentGraphData);
            final float totalData = getTotalData(currentGraphData);

            // .. Draw allocated and used strings ..
            graphics.setColor(INFO_COLOR);
            graphics.drawString(getMaxValueString(totalData), 4.0f, (float) ascent + 0.5f);
            graphics.drawString(getCurrentValueString(totalData - freeData), 4, h - descent);

            // Calculate remaining size
            float ssH = ascent + descent;
            float remainingHeight = h - (ssH * 2) - 0.5f;
            float blockHeight = remainingHeight / 10;
            float blockWidth = 20.0f;

            // .. Free ..
            graphics.setColor(FREE_DATA_COLOR);
            int memUsage = (int) ((freeData / totalData) * 10);
            int i = 0;
            for (; i < memUsage; i++) {
                mfRect.setRect(5, ssH + i * blockHeight, blockWidth, blockHeight - 1);
                graphics.fill(mfRect);
            }

            // .. Used ..
            graphics.setColor(INFO_COLOR);
            for (; i < 10; i++) {
                muRect.setRect(5, ssH + i * blockHeight, blockWidth, blockHeight - 1);
                graphics.fill(muRect);
            }

            // .. Draw History Graph ..
            graphics.setColor(GRAPH_COLOR);
            int graphX = 30;
            int graphY = (int) ssH;
            int graphW = w - graphX - 5;
            int graphH = (int) remainingHeight;
            graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
            graphics.draw(graphOutlineRect);

            int graphRow = graphH / 10;

            // .. Draw row ..
            for (int j = graphY; j <= graphH + graphY; j += graphRow) {
                graphLine.setLine(graphX, j, graphX + graphW, j);
                graphics.draw(graphLine);
            }

            // .. Draw animated column movement ..
            int graphColumn = 30;

            if (columnInc == 0) {
                columnInc = graphColumn;
            }

            for (int j = graphX + columnInc; j < graphW + graphX; j += graphColumn) {
                graphLine.setLine(j, graphY, j, graphY + graphH);
                graphics.draw(graphLine);
            }

            --columnInc;

            graphics.setColor(DATA_COLOR);
            final int size = dataToPaint.size();
            if (size > 1) {
                for (i = 0; i < size - 1 && i < graphW; i++) {
                    final GraphData graphData = dataToPaint.get(size - 1 - i);
                    final GraphData graphDataPrev = dataToPaint.get(size - 1 - i - 1);
                    final int y = (int) (graphY + graphH * (getFreeData(graphData) / getTotalData(graphData)));
                    final int yPrev = (int) (graphY + graphH * (getFreeData(graphDataPrev) / getTotalData(graphDataPrev)));
                    int j = graphX + graphW - i;
                    graphics.drawLine(j - 1, yPrev, j, y);
                }
            }
            g.drawImage(bufferedImage, 0, 0, this);
        }

        protected void start() {
            if (thread == null) {
                thread = new Thread(this);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName(createThreadName());
                thread.setDaemon(true);
                thread.start();
            }
        }

        protected String createThreadName() {
            return "MemoryMonitor";
        }

        @Override
        public void run() {
            final Thread me = Thread.currentThread();
            while (thread == me && !isShowing() || getSize().width == 0) {
                try {
                    Thread.sleep(StatusBar.MEMORY_INFO_AUTO_UPDATE_PERIOD);
                } catch (InterruptedException e) {
                    return;
                }
            }
            while (thread == me) {
                final Dimension d = getSize();
                if ((d.width != w || d.height != h) && GRAPH_DATA.size() > 1) {
                    w = Math.max(d.width, 1);
                    h = Math.max(d.height, 1);
                    bufferedImage = (BufferedImage) createImage(w, h);
                    graphics = bufferedImage.createGraphics();
                    graphics.setFont(FONT);
                    final FontMetrics fm = graphics.getFontMetrics(FONT);
                    ascent = fm.getAscent();
                    descent = fm.getDescent();
                }
                SwingUtilities.invokeLater(this::repaint);
                try {
                    Thread.sleep(SLEEP_AMOUNT);
                } catch (InterruptedException e) {
                    break;
                }
            }
            thread = null;
        }

    }

    /**
     * Panel for displaying CPU usage information
     */
    private static class CpuPanel extends MemoryPanel {

        @Override
        protected String createThreadName() {
            return "CpuMonitor";
        }

        @Override
        protected float getFreeData(GraphData graphData) {
            return 100F - (float) graphData.getSystemLoadAverage();
        }

        @Override
        protected float getTotalData(GraphData graphData) {
            return 100F;
        }

        @Override
        protected String getMaxValueString(float max) {
            return "100 %";
        }

        @Override
        protected String getCurrentValueString(float min) {
            return (int) min + " %";
        }

    }

    /**
     * Border panel for graph panels
     */
    private static class BorderPanel extends JPanel {

        private BorderPanel(MemoryPanel panel) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEtchedBorder());
            add(panel, BorderLayout.CENTER);
        }

    }

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMonitorDialog.class);

    /**
     * Minimum dialog size
     */
    private static final Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(300, 200);

    /**
     * OperatingSystemMXBean reference
     */
    private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN;

    static {
        final MBeanServerConnection platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        OperatingSystemMXBean operatingSystemMxBean = null;
        try {
            operatingSystemMxBean = ManagementFactory.newPlatformMXBeanProxy(platformMBeanServer, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
            if (operatingSystemMxBean.getSystemLoadAverage() < 0) {
                operatingSystemMxBean = null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        OPERATING_SYSTEM_MX_BEAN = operatingSystemMxBean;
    }

    /**
     * Runtime reference
     */
    private static final Runtime RUNTIME = Runtime.getRuntime();

    /**
     * History queue size
     */
    private static final int HISTORY_SIZE = 20000;

    /**
     * History queue
     */
    private static final CircularFifoQueue<GraphData> GRAPH_DATA = new CircularFifoQueue<>(HISTORY_SIZE);

    /**
     * Refresh period
     */
    private static final long SLEEP_AMOUNT = TimeUnit.SECONDS.toMillis(1);

    static {
        try {
            final Thread thread = new Thread(() -> {
                while (true) {
                    GRAPH_DATA.add(new GraphData());
                    try {
                        Thread.sleep(SLEEP_AMOUNT);
                    } catch (InterruptedException e) {
                        break;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Action that opens and closes this dialog (needed to update menu tet on close)
     */
    private final MuAction closeAction;

    /**
     * Button that closes the dialog.
     */
    private final JButton okButton;

    /**
     * Split pane that splits panels with graphs
     */
    private final JSplitPane splitPane;

    /**
     * Indicates that the dialog is just created and will be showing for the first time
     */
    private boolean firstTime = true;

    public PerformanceMonitorDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get("PerformanceMonitor.title"), mainFrame);
        setModal(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        closeAction = ActionManager.getActionInstance(TogglePerformanceMonitorAction.Descriptor.ACTION_ID, mainFrame);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        if (isSystemLoadAverageInfoAvailable()) {
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setOneTouchExpandable(true);
            splitPane.setContinuousLayout(true);
            splitPane.add(createCpuPanel());
            splitPane.add(createMemoryPanel());
            contentPane.add(splitPane, BorderLayout.CENTER);
        } else {
            splitPane = null;
            contentPane.add(createMemoryPanel(), BorderLayout.CENTER);
        }
        okButton = new JButton(Translator.get("ok"));
        okButton.addActionListener(this);
        final FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.RIGHT);
        final JPanel okPanel = new JPanel(layout);
        okPanel.add(okButton);
        contentPane.add(okPanel, BorderLayout.SOUTH);
        setMinimumSizeDialog(MINIMUM_DIALOG_DIMENSION);
        setInitialFocusComponent(okButton);
        getRootPane().setDefaultButton(okButton);
    }

    @Override
    public void setVisible(boolean visible) {
        if (isSystemLoadAverageInfoAvailable()) {
            if (visible && firstTime) {
                firstTime = false;
                SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.5D));
            }
        }
        super.setVisible(visible);
    }

    private Component createMemoryPanel() {
        final MemoryPanel memoryPanel = new MemoryPanel();
        memoryPanel.start();
        return new BorderPanel(memoryPanel);
    }

    private Component createCpuPanel() {
        final CpuPanel cpuPanel = new CpuPanel();
        cpuPanel.start();
        return new BorderPanel(cpuPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            closeAction.performAction();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        closeAction.performAction();
    }

    private static boolean isSystemLoadAverageInfoAvailable() {
        return OPERATING_SYSTEM_MX_BEAN != null;
    }

    private static long getFreeMemoryData() {
        return RUNTIME.freeMemory();
    }

    private static long getTotalMemoryData() {
        return RUNTIME.totalMemory();
    }

    private static double getSystemLoadAverage() {
        if (isSystemLoadAverageInfoAvailable()) {
            return OPERATING_SYSTEM_MX_BEAN.getSystemLoadAverage();
        } else {
            return 0D;
        }
    }

}
