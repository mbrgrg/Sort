package sort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;


public class BinaryFileBuffer {
    public static int BUFFERSIZE = 1024;
    public BufferedReader fbr;
    public File originalfile;
    private String cache;
    private boolean empty;

    public BinaryFileBuffer(File f) throws IOException{
        originalfile = f;
        fbr = new BufferedReader(new FileReader(f), BUFFERSIZE);
        reload();
    }
    public boolean empty() {
        return empty;
    }
    private void reload() throws IOException{
        try {
            if((this.cache = fbr.readLine()) == null){
                empty = true;
                cache = null;
            }
            else{
                empty = false;
            }
        } catch(EOFException oef){
            empty = true;
            cache = null;
        }
    }
    public void close() throws IOException{
        fbr.close();
    }
    public String peek() {
        if(empty()) return "";
        return cache;
    }
    public String pop() throws IOException{
        String answer = peek();
        reload();
        return answer;
    }

    public static int mergeSortedFiles(List<File> files, String outputfile) throws IOException {
        //questo è il metodo che fa la fusione che mi rallenta tutto
        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<>(11,
                (BinaryFileBuffer i, BinaryFileBuffer j) -> {
                    if(i.peek().length()<j.peek().length())
                        return -1;
                    else if(i.peek().length()>j.peek().length())
                        return 1;
                    else if(Long.valueOf(i.peek())>Long.valueOf(j.peek()))
                        return 1;
                    else if(Long.valueOf(i.peek())<Long.valueOf(j.peek()))
                        return -1;
                    return 0;
                });
        long size = 0;
        for (File f : files) {
            BinaryFileBuffer bfb = new BinaryFileBuffer(f);
            pq.add(bfb);
            size+=f.length();
        }
        BufferedWriter fbw = new BufferedWriter(new FileWriter(outputfile));
        int rowcounter = 0;
        try {
            while(pq.size()>0) {
                BinaryFileBuffer bfb = pq.poll();
                String r = bfb.pop();
                fbw.write(r);
                if(rowcounter!=size || rowcounter == 0){
                    fbw.newLine();
                }
                if(bfb.empty()) {
                    bfb.fbr.close();
                    bfb.originalfile.delete();
                } else {
                    pq.add(bfb);
                }
                rowcounter++;
            }
        } finally {
            fbw.close();
            for(BinaryFileBuffer bfb : pq ) bfb.close();
        }
        return rowcounter;
    }
}
