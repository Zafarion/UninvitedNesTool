package uninvitednestool;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class UninvitedNesTool { 
        
    RandomAccessFile rom, texto, itens, tabela;
    JFrame tela = new JFrame ("Uninvited [NES] Text Dump/Insert Tool. Written by Zafarion");
    Formulario formulario = new Formulario(4, 3);
        
    public static void main(String[] args) {
        
        final UninvitedNesTool p = new UninvitedNesTool();
        
        p.tela.setSize(660,330);
        p.tela.setLocation(175,240);
        p.tela.setResizable(false);
  
        p.tela.add(p.formulario);
        p.tela.setVisible(true);
        
        //Evento ao clicar "Abrir Rom"
        p.formulario.getBotao(0).addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    p.rom = p.abrirArquivo(0);
                }
            }
        );
        
        //Evento ao clicar "Abrir texto"
        p.formulario.getBotao(1).addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    p.texto = p.abrirArquivo(1);
                }
            }
        );
        
        //Evento ao clicar "Abrir itens"
        p.formulario.getBotao(2).addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    p.itens = p.abrirArquivo(2);
                }
            }
        );
        
        //Evento ao clicar "Abrir tabela"
        p.formulario.getBotao(3).addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    p.tabela = p.abrirArquivo(3);
                }
            }
        );
        
        //Evento ao clicar "Extrair"
        p.formulario.getTarefa(0).addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {                   
                    if (p.rom == null || p.texto == null || p.itens == null || p.tabela == null)
                        JOptionPane.showMessageDialog(null, "Check if you loaded a ROM file and any empty .txt file!", "Warning", JOptionPane.WARNING_MESSAGE);
                    else {
                        Romhack extracao = new Romhack(p.rom, p.texto, p.itens, p.tabela);
                        extracao.extrair();
                    }
               }  
           }    
                    
        );
        
        //Evento ao clicar "Inserir"
        p.formulario.getTarefa(1).addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {                   
                    if (p.rom == null || p.texto == null || p.itens == null || p.tabela == null)
                        JOptionPane.showMessageDialog(null, "First load the files!", "Warning", JOptionPane.WARNING_MESSAGE);
                    else {
                        Romhack insercao = new Romhack(p.rom, p.texto, p.itens, p.tabela);
                        insercao.inserir();
                    }
               }  
           }    
                    
        );
        
        //Evento ao clicar "Sair"
        p.formulario.getTarefa(2).addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(1);
                }
            }
        );
    }
    
    private RandomAccessFile abrirArquivo(final int numero) {
        
        RandomAccessFile abrir = null;
        JFileChooser arquivo = new JFileChooser();
        
        arquivo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int resultado = arquivo.showOpenDialog(arquivo);
        if (resultado == JFileChooser.CANCEL_OPTION)
            return null;
        File diretorio = arquivo.getSelectedFile();
        if (diretorio == null || diretorio.getName().equals(""))
            JOptionPane.showMessageDialog(null, "Invalid file", "Error", JOptionPane.ERROR_MESSAGE);
        else {
            try {
                abrir = new RandomAccessFile(diretorio, "rw");
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null, "Invalid file", "Error", JOptionPane.ERROR_MESSAGE);
              }                  
            formulario.setCampo(diretorio.getPath(), numero);
            return abrir;
        }
        return abrir;
    }
}


