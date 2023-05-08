import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestFile extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTable table;
    JScrollPane scrollPane;

    public TestFile() {

        String[] columnNames = {"Name", "Progress", "Delete"};

        Object[][] data = {
                {"File 1", 0, "Delete"},
                {"File 2", 0, "Delete"},
                {"File 3", 0, "Delete"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return getValueAt(0, columnIndex).getClass();
            }
        };

        table = new JTable(model);
        table.getColumn("Delete").setCellRenderer(new JButtonRenderer());
        table.getColumn("Delete").setCellEditor(new JButtonEditor());
        table.getColumn("Progress").setCellRenderer(new JProgressBarRenderer());
         //scrollPane = new JScrollPane(table);
       // add(scrollPane, BorderLayout.CENTER);
    }

    public JTable getTable(){
        return this.table;
    }

    class JButtonRenderer extends JButton implements TableCellRenderer {

        public JButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class JButtonEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        protected JButton button;

        private String label;
        private boolean isPushed;

        private int rowSelectedToDelete;

        public JButtonEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
                rowSelectedToDelete = row;
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }

            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                JOptionPane.showMessageDialog(button, label + " has been deleted");

            }
            isPushed = false;
            return new String(label);
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
            ((DefaultTableModel)table.getModel()).removeRow(rowSelectedToDelete);
        }
    }

    class JProgressBarRenderer extends JProgressBar implements TableCellRenderer {

        public JProgressBarRenderer() {
            setOpaque(true);
            setMinimum(0);
            setMaximum(100);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            int progress = Integer.parseInt(value.toString());
            progress = 30;
            setValue(progress);
            return this;
        }
    }

    public static void main(String[] args) {
        new TestFile();
    }
}
