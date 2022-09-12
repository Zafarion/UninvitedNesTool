package uninvitednestool;

import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JOptionPane;

public class Romhack {
    
    RandomAccessFile rom, texto, itens, tbl;
    int ponteiros[][] = new int[257][2];
    int arvore1[] = new int[58];
    int arvore2[] = new int[58];
    int quantidade, posicao;
    String tabela[] = new String[256];
    
    Romhack (RandomAccessFile rom, RandomAccessFile texto, RandomAccessFile itens, RandomAccessFile tbl) {
      
        this.rom = rom;
        this.texto = texto;
        this.itens = itens;
        this.tbl = tbl;
        
    }
            
    public void extrair() {
          
        try {
            
        abrirTabela();
        
        //Ler o conteúdo das árvores e armazenar num vetor
        arvore1 = carregarArvore(0x1E43F, arvore1.length);
        arvore2 = carregarArvore(0x1DC47, arvore2.length);
        
        //Bloco 3
        calcularPonteiros(90128, 90639, 0xC010);
        texto.writeBytes("{" + Integer.toString(ponteiros[0][0]) + "}\r\n\r\n");
        for (posicao = 0; posicao < quantidade; posicao++)
            descompactar(ponteiros[posicao][0], true);
        
        //Bloco 2
        calcularPonteiros(49168, 49679, 0x4010);
        texto.writeBytes("{" + Integer.toString(ponteiros[0][0]) + "}\r\n\r\n");
        for (posicao = 0; posicao < quantidade; posicao++)
            descompactar(ponteiros[posicao][0], true);
        
        //Bloco 1
        calcularPonteiros(32784, 33295, 0x10);
        texto.writeBytes("{" + Integer.toString(ponteiros[0][0]) + "}\r\n\r\n");
        for (posicao = 0; posicao < quantidade; posicao++)
            descompactar(ponteiros[posicao][0], true);
        
        //Bloco Itens (não usa compressão)
        rom.seek(61456);
        while (rom.getFilePointer() < 63383) {
            for (int x = 0; x < 8; x++)
                itens.writeBytes(tabela[rom.readUnsignedByte()]);
            itens.writeBytes("#\r\n________\r\n\r\n");
        }
        
        } catch (IOException ex) {}
    }
    
    public void inserir() {
    
        int pos = 0, c = 0;
        
        try {
        
        abrirTabela();
        compactar();
        
       //Inserir Itens (não usa compressão)
       rom.seek(61456);
       byte bloco[] = new byte[(int)itens.length()];
       itens.read(bloco);
       String script = new String(bloco); 
       //Remover todos "_" e quebras de linha
       script = script.replace("\r\n", "").replace("_", "");
       //Escrever script na ROM
       while (pos < script.length()) {
        while (script.charAt(pos) != '#') {
            if (rom.getFilePointer() > 63383) {
            JOptionPane.showMessageDialog(null, "Item block overflow!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
            if (script.charAt(pos) == '<') {
                rom.writeByte(Integer.parseInt(script.substring(pos + 1, pos + 3), 16));
                pos = pos + 4;
            }
            else    
                rom.writeByte(encontrarPosicaoNaTabela(script.charAt(pos++)));
            c++;
        }
        for (int x = 0; x < 8 - c; x++)
            rom.writeByte(0xFF);
        pos++;
        c = 0;
       }
       
       } catch (IOException ex) {}
       
    }
    
     private void abrirTabela() throws IOException {
        
        String linha;
       
        while (tbl.getFilePointer() < tbl.length()) {
            linha = tbl.readLine();
            if (linha.contains("/"))
                    linha = linha.concat("\r\n");
            if (linha.contains("|"))
                    linha = linha.concat("\r\n\r\n");
            if (linha.contains("#"))
                    linha = linha.replace("#", "#\r\n").concat("\r\n\r\n");
            
            tabela[Integer.parseInt(linha.substring(0,2), 16)] = linha.substring(3);
        } 
        
    }
     
    private int[] carregarArvore(int endereco, int tamanho) throws IOException {
        
        int x, arvore[] = new int[tamanho];
        long backup;
        
        backup = rom.getFilePointer();
        rom.seek(endereco);
        for (x = 0; x < tamanho; x++)
            arvore[x] = rom.readUnsignedByte();
        rom.seek(backup);
        
        return arvore;
        
    }
    
    private long obterBaseCalculoPonteiros (long enderecoPonteiros, long enderecoTexto) throws IOException {
        
        long ponteiro1, ponteiro2, backup;
        
        backup = rom.getFilePointer();
        rom.seek(enderecoPonteiros);
        ponteiro1 = rom.readUnsignedByte();
        ponteiro2 = rom.readUnsignedByte();
        rom.seek(backup);
        
        return enderecoTexto - (ponteiro2 * 256 + ponteiro1);
        
    }
    
    private void calcularPonteiros(int inicioBloco, int finalBloco, int baseCalculo) throws IOException {
        
        int x, ponteiro1, ponteiro2;
        long backup;
         
        quantidade = 0;
        backup = rom.getFilePointer();
        rom.seek(inicioBloco);
        for (x = inicioBloco; x < finalBloco; x = x + 2) {
            ponteiro1 = rom.readUnsignedByte();
            ponteiro2 = rom.readUnsignedByte();
            ponteiros[quantidade][0] = (ponteiro2 * 256 + ponteiro1) + baseCalculo;
            ponteiros[quantidade][1] = x;
            quantidade++;
        }       
        //Tratamento do último ponteiro
        ponteiros[quantidade][0] = ponteiros[quantidade - 1][0] + 128;
        rom.seek(backup);
        
    }
    
    private void descompactar (int inicioSequencia, boolean escreverPonteiros) throws IOException {
        
        String buffer = "", bits;
        int pos = 0, x = 0;
        
        //Primeiro vamos salvar o endereço do ponteiro no começo da sequência de texto
        if (escreverPonteiros == true)    
            texto.writeBytes("[" + Integer.toString(ponteiros[posicao][1]) + "]\r\n");
            
        //Agora vamos converter toda uma sequênia de diálogos em binário, armazenando numa string
        rom.seek(inicioSequencia);
        while (rom.getFilePointer() < ponteiros[posicao + 1][0]) {
            bits = Integer.toBinaryString(rom.readUnsignedByte());
                while (bits.length() < 8)
                    bits = "0" + bits;
            buffer = buffer.concat(bits);
        }    
          
        //Convertida a sequência de texto em binário, vamos contar quantos Byte 0 tem até chegar no 1 e converter o resultado em binário
        while (pos < buffer.length()) {
            while (buffer.charAt(pos) != '1') {
                pos++;
                x++;
                if (pos == buffer.length() - 4)
                    break;
            }
             
            pos++;
            bits = Integer.toBinaryString(x);
            bits = bits.concat(buffer.substring(pos, pos + 3));
            pos = pos + 3;
            
            //Essa putaria abaixo tive que fazer manualmente pra separar os "códigos de controle" que ficam no meio dos caracteres comprimidos...
            //assim garantindo que esses códigos binários permaneçam inalterados. Logico que se eu entendesse 100% a rotina na ROM, faria de uma forma mais correta.
            if (Integer.parseInt(bits, 2) == 37 && buffer.length() - pos > 23) {
                if (buffer.substring(pos, pos + 23).equals("00000000001010000000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 23) + ">");
                    pos = pos + 23;
                }
                else if (buffer.substring(pos, pos + 21).equals("000000001010000000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 21) + ">");
                    pos = pos + 21; 
                }
                else if (buffer.substring(pos, pos + 20).equals("00000001010000000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 20) + ">");
                    pos = pos + 20;
                }
                else if (buffer.substring(pos, pos + 19).equals("0000001010000000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 19) + ">");
                    pos = pos + 19;
                }
                else if (buffer.substring(pos, pos + 18).equals("000001010000000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 18) + ">");
                    pos = pos + 18;
                }
                else if (buffer.substring(pos, pos + 17).equals("00001010100000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 17) + ">");
                    pos = pos + 17;
                }
                else if (buffer.substring(pos, pos + 17).equals("00001010000000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 17) + ">");
                    pos = pos + 17;
                }
                else if (buffer.substring(pos, pos + 16).equals("0001010000000101")) {
                    texto.writeBytes("<" + buffer.substring(pos - 8, pos + 16) + ">");
                    pos = pos + 16;
                }
            }
            //Fim da putaria. Se não passou por estes códigos de controle, basta escrever o caractere no arquivo texto como abaixo
            else {
                texto.writeBytes(tabela[arvore2[arvore1[Integer.parseInt(bits, 2)]]]);
                if (tabela[arvore2[arvore1[Integer.parseInt(bits, 2)]]].charAt(0) == '#')
                    break;
            }
               
            x = 0;
        }    
    }
    
    private void compactar() throws IOException {
        
        long ponteiroDeArquivo, baseCalculo = 0;
        int pos = 0, estatistica[] = new int[257];
        String buffer = "", buffer1, buffer2, buffer3;
        boolean bandeira = false;
        int indice1, indice2, indice3, c = 0;
        
        
        //Jogar o script inteiro numa variável String
        byte bloco[] = new byte[(int)texto.length()];
        texto.read(bloco);
        String script = new String(bloco);
        
        //Remover todas as quebras de linha e "_" do buffer script
        script = script.replace("\r\n", "").replace("_", "");
        
        //Contar a quantidade de caracteres no script
        while (pos < script.length()) {
            
            //Pular cabeçalho com endereço do bloco de texto
            if (script.charAt(pos) == '{') {
                while(script.charAt(pos++) != '}');
                pos--;
            }
            //Pular cabeçalho com endereço do ponteiro
            if (script.charAt(pos) == '[') {
                while (script.charAt(pos++) != ']');
                pos--;
            } 
            //Pular tudo que estiver entre <>
            if (script.charAt(pos) == '<') {
                while (script.charAt(pos++) != '>');
                pos--;
            }
            
            estatistica[encontrarPosicaoNaTabela(script.charAt(pos++))]++;
                
        }
        
        //Salvar todas as entradas que tem estatística na árvore 2 de compressão
        //rom.seek(0x1DC47);
        for (int x = 0; x < estatistica.length - 1; x++)
            if (estatistica[x] > 0) {
                //rom.writeByte(x); Infelizmente não deu pra alterar esta árvore porque buga o jogo após inserir muitas sequências de texto e não descobri porgue
                c++;
                //if (rom.getFilePointer() > 0x1DC80) {
                //    JOptionPane.showMessageDialog(null, "Internal tree overflow. Your script has more than 57 distinct chars, which is unsupported.", "Error", JOptionPane.ERROR_MESSAGE);
                //    System.exit(0);
                //}
            }
        
        System.out.println(c);
        
        //Abrir árvore 2 após alteração
        arvore2 = carregarArvore(0x1DC47, arvore2.length);
        
        //Mover o file pointer para o começo da árvore 1
        rom.seek(0x1E43F);
        
        //Laço para varrer árvore de compressão
        for (int x = 0; x < c; x++) {
            //Pegar o caractere mais frequente
            estatistica[estatistica.length - 1] = 0;
            for (int y = 0; y < estatistica.length - 1; y++)
                if (estatistica[y] > estatistica[estatistica.length - 1]) {
                    estatistica[estatistica.length - 1] = estatistica[y];
                    pos = y;
                }
            
            //A posição 37 da árvore 1 é reservada para códigos (lembra da "putaria" no método Descompactar?) então pularemos esta posição direto na ROM
            if (rom.getFilePointer() == 124004)
                rom.skipBytes(1);
            
            //Escrever na árvore 1 da ROM a entrada com maior quantidade, em ordem decrescente
            rom.writeByte(encontrarPosicaoNaArvore(arvore2, pos));
            System.out.println(tabela[pos] + " " + estatistica[pos]); 
         
            //Limpar entrada de maior valor no vetor de estatística para recomeçar
            estatistica[pos] = 0;
        }
         
        //Abrir árvore 1 após nova ordenação
        arvore1 = carregarArvore(0x1E43F, c + 1);
        
        
        //Resetar "pos"
        pos = 0;
            
        while (pos < script.length()) {
                 
                //Obter endereço do bloco de textos
                if (script.charAt(pos) == '{') {
                    while (script.charAt(pos++) != '}');
                    rom.seek(Integer.parseInt(script.substring(pos - 6, pos - 1)));
                    bandeira = true;
                }
                
                //Obter endereço do ponteiro no script, setar file poiner lá e escrever nova posição do ponteiro
                if (script.charAt(pos) == '[') {
                    while (script.charAt(pos++) != ']');
                    if (bandeira) { 
                        baseCalculo = obterBaseCalculoPonteiros(Long.parseLong(script.substring(pos - 6, pos - 1)), rom.getFilePointer());
                        bandeira = false;
                    }
                    //Ir ao endereço do ponteiro para escrevê-lo
                    ponteiroDeArquivo = rom.getFilePointer();
                    rom.seek(Integer.parseInt(script.substring(pos - 6, pos - 1)));
                    rom.writeShort(Short.reverseBytes((short)(ponteiroDeArquivo - baseCalculo)));
                    //Ir ao endereço do bloco de texto
                    rom.seek(ponteiroDeArquivo);
                }
                
                //Adicionar os binários que estão dentro de <>
                if (script.charAt(pos) == '<') {
                    while (script.charAt(++pos) != '>')
                        buffer = buffer + script.charAt(pos);
                    pos++;
                    //bandeira = false;
                }
                
                //Finalmente converter um caractere na tabela para binário
                
                indice1 = encontrarPosicaoNaTabela(script.charAt(pos));
                    
                //Indice1 menor que 255 significa que o caractere não foi localizado na tabela, portanto não podemos executar nada abaixo, pois pode ser um colchete ou chave
                if (indice1 < 255) {
                
                    indice2 = encontrarPosicaoNaArvore(arvore2, indice1);
                    indice3 = encontrarPosicaoNaArvore(arvore1, indice2);
                
                    buffer1 = Integer.toBinaryString(indice3);
                
                    while (buffer1.length() < 10)
                        buffer1 = '0' + buffer1;
                
                    buffer2 = '1' + buffer1.substring(7);
                    buffer3 = buffer1.substring(0, 7);
                
                    for (int x = 0; x < Integer.parseInt(buffer3, 2); x++)
                        buffer2 = '0' + buffer2;
                
                    buffer = buffer.concat(buffer2);
   
                    if (tabela[indice1].charAt(0) == '#') {
                        for (int x = 0; x < buffer.length(); x = x + 8) {
                            if ((buffer.length() - x) < 8)
                                while ((buffer.length() - x) < 8)
                                    buffer = buffer.concat("0");
                            rom.writeByte((Integer.parseInt(buffer.substring(x, x + 8), 2)));
    
                            if (rom.getFilePointer() == 43641) {
                                JOptionPane.showMessageDialog(null, "Text block 1 overflow!", "Error", JOptionPane.ERROR_MESSAGE);
                                System.exit(0);
                            }
                            if (rom.getFilePointer() == 61455) {
                                JOptionPane.showMessageDialog(null, "Text block 2 overflow!", "Error", JOptionPane.ERROR_MESSAGE);
                                System.exit(0);
                            }
                            if (rom.getFilePointer() == 98319) {
                                JOptionPane.showMessageDialog(null, "Text block 3 overflow!", "Error", JOptionPane.ERROR_MESSAGE);
                                System.exit(0);
                            }
                        }
                        buffer = "";
                    }
                }
                pos++;
        }  
    }
    
    private int encontrarPosicaoNaTabela (char caractere) {
        
        int x;
        
        for (x = 0; x < tabela.length; x++)
            if (tabela[x].charAt(0) == caractere)
                break;
        
        return x;
        
    }
    
    private int encontrarPosicaoNaArvore (int arvore[], int valor) {
        
        int x;
        
        for (x = 0; x < arvore.length; x++)
            if (arvore[x] == valor)
                break;
        
        return x;
        
    }
    
}