/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzip2;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author joaoc
 */
public class bzip2 {
    public static String hufmanTree;
    public static String BurrowsWheelerStringsSorted;
    public static String BurrowsWheelerStringsUnsorted;
    public static String mtfString;
    public static String rleString;
    
    public static ArrayList<String> startEncoding(String text){
        String textoFinal = "String Original: "+text;
        hufmanTree = "";
        BurrowsWheelerStringsSorted = "";
        BurrowsWheelerStringsUnsorted = "";
        mtfString = "";
        rleString = "";
        
        //Burrows-Wheeler
        String[] rotations = new String[text.length()];
        generateRotations(text, rotations);
        sortStrings(rotations, text.length());
        printStringArray(rotations, text.length());
        
        String BurrowsWheelerResult=lastChars(rotations, text.length());
        textoFinal = textoFinal + "\n\n-------- Burrows-Wheeler --------\nCombinações possiveis da string original:\n"+BurrowsWheelerStringsUnsorted;
        textoFinal = textoFinal + "\nOrdenação das combinações:\n"+BurrowsWheelerStringsSorted;
        textoFinal = textoFinal + "\nResultado Burrows-Wheeler: "+BurrowsWheelerResult;
        
        //Move-to-front
        List<Integer> encoded = mtf(BurrowsWheelerResult.toLowerCase());
        textoFinal = textoFinal + "\n\n\n\n-------- Move-to-front --------\n"+mtfString;
        textoFinal = textoFinal + "\nResultado do Move-to-front: " + encoded + "\n";
        
        
        //RLE
        textoFinal = textoFinal + "\n\n\n\n-------- Run-Length --------\nString inicial do Run-Length: "+encoded;
        textoFinal = textoFinal + "\n\nAplicação do Run-Length:";
        String rleOutput = rleEncode(encoded);
        textoFinal = textoFinal + "\n"+rleString;
        textoFinal = textoFinal + "\nResultado final do Run-Length: "+rleOutput;

        int[] charFreqs = new int[256];
        for (char c : rleOutput.toCharArray()){
            charFreqs[c]++;
        }

        
        //Huffman
        ArrayList<String> huffman = new ArrayList<>();
        HuffmanTree tree = buildTree(charFreqs);
        textoFinal = textoFinal + "\n\n\n\n-------- Huffman --------\nTabela de Huffman:\nSYMBOL\t\tWEIGHT\t\tHUFFMAN CODE";
        printCodes(tree, new StringBuffer(), huffman, "");
        textoFinal = textoFinal + "\n"+hufmanTree;
        String huffmanString="";
        huffmanString = huffman.stream().map((s) -> s).reduce(huffmanString, String::concat);
        textoFinal = textoFinal + "\nResultado de Huffman: "+huffmanString;
        float racioCompressao=(float) (text.length()*8)/huffmanString.length();
        DecimalFormat df = new DecimalFormat("0.00");
        textoFinal = textoFinal + "\n\nRácio de Compressão: "+df.format(racioCompressao)+" : 1";
        ArrayList<String> result = new ArrayList<>(2);
        result.add(textoFinal);
        result.add(huffmanString);
        return result;
    }
    
    public static String rleEncode(List<Integer> source) {
        StringBuffer dest = new StringBuffer();
        for (int i = 0; i < source.size(); i++) {
            int runLength = 1;
            while (i+1 < source.size() && source.get(i) == source.get(i+1)) {
                runLength++;
                i++;
            }
            dest.append(runLength);
            dest.append(source.get(i));
            rleString=rleString+runLength+"  ->  "+source.get(i)+"\n";
        }
        return dest.toString();
    }

    public static List<Integer> mtf(String msg){
        String symTable = "abcdefghijklmnopqrstuvwxyz";
        mtfString=mtfString+"Tabela de Simbolos Inicial: "+symTable+"\n\n";
        mtfString=mtfString+"Aplicação do Move-To-Front:\n";
        List<Integer> output = new LinkedList<>();
        StringBuilder s = new StringBuilder(symTable);
        for(char c : msg.toCharArray()){
            int idx = s.indexOf("" + c);
            output.add(idx);
            mtfString=mtfString+c+" -> "+idx+"\n";
            s = s.deleteCharAt(idx).insert(0, c);
            mtfString=mtfString+s+"\n";
        }
        mtfString=mtfString+"\nTabela de Simbolos Final: "+s;
        return output;
    }

    // Generate all the rotations of a String, rotating one position at a time
    public static void generateRotations(String source, String[] rotations) {
        rotations[0] = source;
        for (int i = 1; i < source.length(); i++) {
            rotations[i] = rotations[i - 1].substring(1) + rotations[i - 1].charAt(0);
            BurrowsWheelerStringsUnsorted = BurrowsWheelerStringsUnsorted + rotations[i]+"\n";
        }
    }

    // Get each of the last characters of an array of Strings
    public static String lastChars(String[] s, int SIZE) {
        String result = "";

        for (int i = 0; i < SIZE; i++) {
            result = result + s[i].charAt(SIZE - 1);
        }

        return result;
    }

    // Sort an array of strings
    public static void printStringArray(String[] rotations, int SIZE) {
        for (int i = 0; i < SIZE; i++) {
            BurrowsWheelerStringsSorted=BurrowsWheelerStringsSorted+rotations[i]+"\n";
        }
    }

    public static void sortStrings(String[] rotations, int SIZE) {
        for (int pass = 0; pass <= SIZE - 2; pass++) {
            for (int i = 0; i <= SIZE - pass - 2; i++) {
                if (rotations[i].compareTo(rotations[i + 1]) > 0) {
                    swap(rotations, i, i + 1);
                }
            }
        }
    }

    // Swap 2 given positions in an array of Strings
    public static void swap(String[] a, int i, int j) {
        String t = a[i];
        a[i] = a[i + 1];
        a[i + 1] = t;
    }

    public static HuffmanTree buildTree(int[] charFreqs) {
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
        // initially, we have a forest of leaves
        // one for each non-empty character
        for (int i = 0; i < charFreqs.length; i++)
            if (charFreqs[i] > 0)
                trees.offer(new HuffmanLeaf(charFreqs[i], (char)i));

        assert trees.size() > 0;
        // loop until there is only one tree left
        while (trees.size() > 1) {
            // two trees with least frequency
            HuffmanTree a = trees.poll();
            HuffmanTree b = trees.poll();

            // put into new node and re-insert into queue
            trees.offer(new HuffmanNode(a, b));
        }
        return trees.poll();
    }
    
    public static String printCodes(HuffmanTree tree, StringBuffer prefix,ArrayList<String> huffman,String text) {
        assert tree != null;
        
        if (tree instanceof HuffmanLeaf) {
            HuffmanLeaf leaf = (HuffmanLeaf)tree;
            huffman.add(prefix.toString());
            hufmanTree=hufmanTree+leaf.value + "\t\t" + leaf.frequency + "\t\t" + prefix+"\n";
        } else if (tree instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode)tree;

            // traverse left
            prefix.append('0');
            printCodes(node.left, prefix,huffman,text);
            prefix.deleteCharAt(prefix.length()-1);

            // traverse right
            prefix.append('1');
            printCodes(node.right, prefix,huffman,text);
            prefix.deleteCharAt(prefix.length()-1);
        }
        return text;
    }
}
