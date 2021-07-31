import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class Programa extends JFrame implements ActionListener {  
    Connection con;
    Statement stmt; 
    JDesktopPane desktop;
            
    JMenuItem menuCadastraLivro;
    JMenuItem menuCadastraCliente;
    JMenuItem menuEmprestaLivro;
    JMenuItem menuConsultaLivro;
    JMenuItem menuConsultaCliente;
    JMenuItem menuConsultaEmpresta;
    JMenuItem menuTermina;

    JanelaConsulta janelaConsultaLivro;
    JanelaConsulta janelaConsultaCliente;
    JanelaConsulta janelaConsultaEmpresta;

    public Programa() {  
        super("Trabalho Banco de Dados");  
            
        setBounds(50,50,700,500);  
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
            
        desktop = new JDesktopPane();  
        add(desktop);  
            
        setJMenuBar(criaMenu());
        
        iniciaBD();
        criaTabelas();

        janelaConsultaLivro = new JanelaConsulta(desktop, con, "LIVRO");  
        janelaConsultaCliente = new JanelaConsulta(desktop, con, "CLIENTE");  
        janelaConsultaEmpresta = new JanelaConsulta(desktop, con, "CLIENTE_EMPRESTA_LIVRO");  
        desktop.add(janelaConsultaLivro);  
        janelaConsultaLivro.setVisible(false);  

        desktop.add(janelaConsultaCliente);  
        janelaConsultaCliente.setVisible(false);  

        desktop.add(janelaConsultaEmpresta);  
        janelaConsultaEmpresta.setVisible(false);  
        
        setVisible(true);  
    }  

    JMenuBar criaMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuBD = new JMenu("Banco de Bados");
        menuBar.add(menuBD);

        menuCadastraLivro = new JMenuItem("Cadastrar Livro");
        menuBD.add(menuCadastraLivro);

        menuCadastraCliente = new JMenuItem("Cadastrar Cliente");
        menuBD.add(menuCadastraCliente);

        menuEmprestaLivro = new JMenuItem("Emprestar Livro");
        menuBD.add(menuEmprestaLivro);

        menuConsultaLivro = new JMenuItem("Consulta Tabela Livros");
        menuBD.add(menuConsultaLivro);

        menuConsultaCliente = new JMenuItem("Consulta Tabela Clientes");
        menuBD.add(menuConsultaCliente);

        menuConsultaEmpresta = new JMenuItem("Consulta Tabela Empresta");
        menuBD.add(menuConsultaEmpresta);

        menuTermina = new JMenuItem("Termina");
        menuBar.add(menuTermina);
        
        menuCadastraLivro.addActionListener(this);
        menuCadastraCliente.addActionListener(this);
        menuEmprestaLivro.addActionListener(this);
        menuConsultaLivro.addActionListener(this);
        menuConsultaCliente.addActionListener(this);
        menuConsultaEmpresta.addActionListener(this);
        menuTermina.addActionListener(this);

        return menuBar;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuCadastraLivro) {
            new JanelaInsere(desktop, con, "LIVRO");
        } else if (e.getSource() == menuCadastraCliente) {
            new JanelaInsere(desktop, con, "CLIENTE");
        } else if (e.getSource() == menuEmprestaLivro) {
            new JanelaInsere(desktop, con, "CLIENTE_EMPRESTA_LIVRO");
        } else if (e.getSource() == menuConsultaLivro) {
            janelaConsultaLivro.setVisible(true);
        } else if (e.getSource() == menuConsultaCliente) {
            janelaConsultaCliente.setVisible(true);
        } else if (e.getSource() == menuConsultaEmpresta) {
            janelaConsultaEmpresta.setVisible(true);
        } else if (e.getSource() == menuTermina) {
            System.exit(0);
        }
    }

    void iniciaBD() {
        try {
            Class.forName("org.hsql.jdbcDriver");
            con = DriverManager.getConnection("jdbc:HypersonicSQL:hsql://localhost:8080", "sa", "");
            stmt = con.createStatement();
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "O driver do banco de dados não foi encontrado.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro na iniciação do acesso ao banco de dados\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    void criaTabelas() {
        try {
            stmt.executeUpdate("CREATE TABLE LIVRO (COD_ISBN INTEGER NOT NULL PRIMARY KEY, TITULO VARCHAR(150), AUTOR VARCHAR(60), EDITORA VARCHAR(60))");
            stmt.executeUpdate("CREATE TABLE CLIENTE (ID INTEGER NOT NULL PRIMARY KEY, NOME VARCHAR(150), CPF CHAR(14))");
            stmt.executeUpdate("CREATE TABLE CLIENTE_EMPRESTA_LIVRO (ID INTEGER NOT NULL PRIMARY KEY, COD_CLIENTE INTEGER, COD_ISBN_LIVRO INTEGER, FOREIGN KEY (COD_CLIENTE) REFERENCES CLIENTE(ID), FOREIGN KEY (COD_ISBN_LIVRO) REFERENCES LIVRO(COD_ISBN))");
            JOptionPane.showMessageDialog(null, "Tabelas criada com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void finalize() {
        try {
            stmt.close();
            con.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    public static void main(String[] args ) {  
        new Programa();  
    }  
}  

class JanelaInsere extends JInternalFrame {
    PreparedStatement pStmt;
    JDesktopPane desktop;
    JButton bt1;

    ArrayList<JTextField> textfields = new ArrayList<JTextField>();

    public JanelaInsere(JDesktopPane d, Connection con, String table) {
        super("Insere na tabela" + table, false, true, false, true); //resizable, closable, maximizable, iconifiable
        desktop = d;
        try {

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
            ResultSetMetaData rsmd = rs.getMetaData();
            int column_count = rsmd.getColumnCount();

            String query = "INSERT INTO " + table + " VALUES ";

            for(int i = 0; i < column_count; i++) {
                if ( i == 0 )
                    query += "(?,";
                else if ( i == column_count - 1 ) 
                    query += "?)";
                else 
                    query += "?,";
            }

            pStmt = con.prepareStatement(query);

            setLayout(new GridLayout(column_count + 1, 2));
            setMaximumSize(new Dimension(700, 500));

            for(int i = 1; i <= column_count; i++) {
                JPanel panel = new JPanel();
                JLabel label = new JLabel(rsmd.getColumnName(i) + ":");
                label.setPreferredSize(new Dimension(100, 100));
                panel.add(label);
                textfields.add(new JTextField(30));
                panel.add(textfields.get(i - 1));
                add(panel);
            }

            JPanel holderButton = new JPanel();

            bt1 = new JButton("Insere");
            bt1.setMaximumSize(new Dimension(150, 50));

            holderButton.add(bt1);
            add(holderButton);
            pack();
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setVisible(true);
            desktop.add(this);

            bt1.addActionListener(new ActionListener() {    //classe interna listener sem nome
                public void actionPerformed(ActionEvent e) {
                    try {
                        for(int i = 1; i <= column_count; i++) {
                            String columnType = rsmd.getColumnTypeName(i);
                            if ( columnType == "VARCHAR" || columnType == "CHAR" ) {
                                pStmt.setString(i, textfields.get(i - 1).getText());
                            } else if ( columnType == "INTEGER" ) {
                                pStmt.setInt(i, Integer.parseInt(textfields.get(i - 1).getText()));
                            }
                        }
                        pStmt.executeUpdate();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void finalize() {
        try {
            pStmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Problema interno.\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class JanelaConsulta extends JInternalFrame implements ActionListener {
    Statement stmt;
    PreparedStatement pStmt;
    JDesktopPane desktop;
    JButton bt1;
    JTextField tf1;
    JLabel labelSelectedField;
    String selectedField;
    JPanel l1;

    public JanelaConsulta(JDesktopPane d, Connection con, String table) {
        super("Consulta na tabela " + table, true, true, false, true); //resizable, closable, maximizable, iconifiable

        desktop = d;

        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
            ResultSetMetaData rsmd = rs.getMetaData();
            int column_count = rsmd.getColumnCount();

            JComboBox<String> fieldList = new JComboBox<>();

            for(int i = 1; i <= column_count; i++) {
                fieldList.addItem(rsmd.getColumnName(i));
            }

            l1 = new JPanel();

            l1.add(fieldList);
            l1.add(labelSelectedField = new JLabel(""));
            l1.add(tf1 = new JTextField(30));

            add(l1, BorderLayout.WEST);

            fieldList.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    try {
                        selectedField = fieldList.getSelectedItem().toString();
                        labelSelectedField.setText(selectedField + ":");
                        pStmt = con.prepareStatement("SELECT * FROM " + table + " WHERE " + selectedField + " LIKE ?");
                        l1 = new JPanel();
                        pack();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Problema interno.\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            fieldList.setSelectedIndex(0);

            l1 = new JPanel();

            l1.add(bt1 = new JButton("Pesquisa"));
            bt1.addActionListener(this);

            add(l1, BorderLayout.SOUTH);

            pack();

            setDefaultCloseOperation(HIDE_ON_CLOSE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(desktop, "Problema interno janela consulta.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            pStmt.setString(1, tf1.getText());

            ResultSet rs = pStmt.executeQuery();
            ResultSetMetaData metadata = rs.getMetaData();
            String tableName = metadata.getTableName(1);
            int column_count = metadata.getColumnCount();

            Vector<String> columnNames = new Vector<String>(column_count);

            Vector<Vector<String>> array = new Vector<Vector<String>>();

            int row = 1;

            while (rs.next()) {
                Vector<String> temp = new Vector<String>();
                for (int col = 1; col <= column_count; col++) {
                    String columnType = metadata.getColumnTypeName(col);

                    if (row == 1) {
                        columnNames.add(metadata.getColumnName(col));
                    }

                    if ( columnType == "VARCHAR" || columnType == "CHAR" ) {
                        temp.add(rs.getString(col));
                    } else if ( columnType == "INTEGER" ) {
                        temp.add(Integer.toString(rs.getInt(col)));
                    }
                }
                row++;
                array.add(temp);
            }

            JTable table = new JTable(array, columnNames);

            JScrollPane tabelPanel = new JScrollPane(table);

            l1 = new JPanel();

            l1.add(tabelPanel);

            add(l1, BorderLayout.EAST);

            pack();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(desktop, "Problema interno action performed.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void finalize() {
        try {
            pStmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}