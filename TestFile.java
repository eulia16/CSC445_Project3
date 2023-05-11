import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;




public class TestFile extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTable table;
    JScrollPane scrollPane;

    public TestFile(ArrayList<Integer> val) {

        String[] columnNames = {"Name", "Progress", "Delete"};

        Object[][] data = {
                  //{"Example 1", 37, "Delete"},
//                {"Example 2", 25, "Delete"},
//                {"Example 3", 75, "Delete"}
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
        table.getColumn("Delete").setCellEditor(new JButtonEditor(val));
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

        private ArrayList<Integer> val;

        private String label;
        private boolean isPushed;

        private int rowSelectedToDelete;

        public JButtonEditor(ArrayList<Integer> val) {
            super(new JCheckBox());
            this.val = val;
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
            this.val.remove(rowSelectedToDelete);
              //shiftValuesByOne(this.val);
        }

        public void setIntArray(ArrayList<Integer> values){
            this.val = values;
        }
        public void shiftValuesByOne(ArrayList<Integer> values){
            System.out.println("inside shift by one");
            for(int i: values){
                System.out.println("values before shift: " + values.get(i));
            }

            for(Integer i : values){
                //changed from values.set(i, values.get(i) -1);
                values.set(i, values.get(i) - 1);
            }
            for(int i: values){
                System.out.println("values afters shift: " + values.get(i));
            }
        }
    }

    class JProgressBarRenderer extends JProgressBar implements TableCellRenderer {

        public JProgressBarRenderer() {
            setOpaque(true);
            setMinimum(0);
            setMaximum(100);
            setStringPainted(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            int progress = Integer.parseInt(value.toString());
            setValue(progress);
            return this;
        }
    }

    public static void main(String[] args) {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        temp.add(0, 10);
        temp.add(1, 20);
        temp.add(2, 30);
        new TestFile(temp);
    }

}
