package uninvitednestool;

import java.awt.*;
import javax.swing.*;

public class Formulario extends JPanel {
    
    protected final static String nomesRotulos[] = {"ROM Directory", "TXT Directory", "Itens Directory", "Table Directory"};
    protected final static String nomesTarefas[] = {"Dump", "Insert", "Exit"};
    protected JLabel rotulos[], rotuloSuperior;
    protected JTextField campos[];
    protected JButton botoes[], tarefas[];
    protected JPanel painelInternoSuperior, painelInternoCentral, painelInternoInferior, painelInternoEsquerdo, painelInternoDireito;
    
    public Formulario (int quantidade, int numero) {
        
        rotulos = new JLabel[quantidade];
        campos = new JTextField[quantidade];
        botoes = new JButton[quantidade];
        tarefas = new JButton[numero];
        
        painelInternoSuperior = new JPanel();
        rotuloSuperior = new JLabel("Uninvited [NES] Dumper/Inserter 1.0. Requires a ROM with NES header. Only tested with US version.");
        painelInternoSuperior.add(rotuloSuperior);
        
        painelInternoCentral = new JPanel();
        painelInternoCentral.setLayout(new FlowLayout(3));
        for (int c = 0; c < quantidade; c++) {
            rotulos[c] = new JLabel (nomesRotulos[c]); 
            campos[c] = new JTextField(50);
            campos[c].setEditable(false);
            botoes[c] = new JButton("Select");
            painelInternoCentral.add(rotulos[c]);
            painelInternoCentral.add(campos[c]);
            painelInternoCentral.add(botoes[c]);
        }
        
        painelInternoInferior = new JPanel();
        painelInternoInferior.setLayout(new FlowLayout(1, 20, 10));
        for (int c = 0; c < numero; c++) {
            tarefas[c] = new JButton(nomesTarefas[c]);
            painelInternoInferior.add(tarefas[c]);
        }
        
        painelInternoEsquerdo = new JPanel();
        
        painelInternoDireito = new JPanel();
        
        setLayout(new BorderLayout());
        add(painelInternoSuperior, BorderLayout.NORTH);
        add(painelInternoCentral, BorderLayout.CENTER);
        add(painelInternoInferior, BorderLayout.SOUTH);
        add(painelInternoEsquerdo, BorderLayout.WEST);
        add(painelInternoDireito, BorderLayout.EAST);
        
        validate();
        
    }
    
    public JButton getTarefa(int numero) {
        
        return tarefas[numero];

    }
     
    public JButton getBotao( int numero) {
        
        return botoes[numero];

    }
    
    public String getCampo(int numero) {
        
        return campos[numero].getText();
        
    }
    
    public void setCampo(String campo, int numero) {
        
        campos[numero].setText(campo);
        
    }
    
}


