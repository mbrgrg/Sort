package sort;
 
import java.util.*;
import java.io.*;
 

public class Sort {
    
    public static long startTime = System.currentTimeMillis(); 
    public static int x=1; //contatore file totali che vengono ordinati
    public static int MAX_TEMP = 128; //max number of slice
    

    public static void main(String[] args) throws IOException {
        try{
            Long start = System.currentTimeMillis();
            String inputfile =Utils.readLine("Insert file name,"
                    + " with path if different from root app");
            String outputfile = "result.txt";
            MAX_TEMP = Utils.readNumber("Insert max number of slice");
            List<File> file = sortInBatch(new File(inputfile));
            //Utils.printLine(Utils.Messages.ELAPSED_TIME_FILE, (System.currentTimeMillis() - start) / 1000);
            //Utils.printLine(Utils.Messages.SORT_END_FILE);
            //startTime = System.currentTimeMillis();
            BinaryFileBuffer.mergeSortedFiles(file,outputfile);
            //Utils.printLine(Utils.Messages.ELAPSED_TIME_FILE, (System.currentTimeMillis() - startTime) / 1000);
            //Utils.printLine(Utils.Messages.MERGE_END);
            Utils.printLine(Utils.Messages.ELAPSED_TIME, (System.currentTimeMillis() - start) / 1000);
            System.out.println("Result in root app: "+outputfile);
        }catch(IOException e){
            System.err.println("File not exist!");
            main(args);
        }
    }  
    
    // Carica i file in blocchi di x righe, li ordina in memoria e
    // scrive i risultati in file temporanei che verranno poi fusi (merge)
    public static List<File> sortInBatch(File file) throws IOException {
        List<File> files = new ArrayList<>();
        BufferedReader fbr = new BufferedReader(new FileReader(file));
        long blocksize = file.length() / MAX_TEMP;
        if(blocksize==0)
            blocksize=file.length();
        int dim=0;
        String line = "";
        long[] list = new long[(int)blocksize];
        long []lista;
        try {
            while(line != null) {
                long blocksizecorrente = 0;
                while((blocksizecorrente < blocksize) 
                &&(   (line = fbr.readLine()) != null) ){
                    list[dim] = Long.valueOf(line);
                    dim++;
                    blocksizecorrente ++;
                }
                if(blocksizecorrente < blocksize){
                    lista = new long[(int)blocksizecorrente];
                    for(int i=0;i<lista.length;i++)
                        lista[i]=list[i];
                    files.add(sortAndSave(lista));
                    dim = 0;
                }
                else{
                    files.add(sortAndSave(list));
                    dim=0;
                }
            }
        }catch(EOFException oef) {
                if(list.length>0) {
                    files.add(sortAndSave(list));
                    list = new long[(int)blocksize];
                }
        }finally {
            fbr.close();
        }
        return files;
        }
    
 
 
    public static File sortAndSave(long[]tmplist) throws IOException  {
        //startTime = System.currentTimeMillis();
        //System.out.println("Sort file "+x+" with "+tmplist.length+" number");
        //x++; //incremento il contatore dei file che sto ordinando
        QuickSort(tmplist,0,tmplist.length-1);
        File newtmpfile = File.createTempFile("sortInBatch", "flatFile");
        newtmpfile.deleteOnExit();
        //Utils.printLine(Utils.Messages.SPLIT_AND_SORT_FILE);
        //Utils.printLine(Utils.Messages.ELAPSED_TIME_FILE, (System.currentTimeMillis() - startTime) / 1000);
        //startTime = System.currentTimeMillis();
        try(
            BufferedWriter fbw = new BufferedWriter(new FileWriter(newtmpfile))) {
            for(int i = 0; i<tmplist.length ; i++){
                if(i!=tmplist.length){
                    fbw.write(new String(""+tmplist[i]));
                    fbw.newLine();
                }
                else
                    fbw.write(new String(""+tmplist[i]));
            }
        }
        //Utils.printLine(Utils.Messages.ELAPSED_TIME_FILE, (System.currentTimeMillis() - startTime) / 1000);
        return newtmpfile;
    }

 
    

    //Quick sort
    public static void QuickSort(long[]arr, int l, int h){
        int stack[] = new int[h-l+1];
        int top = -1;
        stack[++top] = l;
        stack[++top] = h;
        while (top >= 0){
            h = stack[top--];
            l = stack[top--];
            int p = partition(arr, l, h);
            if ( p-1 > l ){
                stack[ ++top ] = l;
                stack[ ++top ] = p - 1;
            }
            if ( p+1 < h ){
                stack[ ++top ] = p + 1;
                stack[ ++top ] = h;
            }
       }
    }    
    public static int partition (long[]arr, int l, int h){
        long val = arr[h];
        int i = (l - 1);
 
        for (int j = l; j <= h- 1; j++){
            if (arr[j] <= val){
                i++;
                swap(arr,i,j);
            }
        }
        swap(arr,i+1,h);
        return (i + 1);
    }
    public static void swap(long[]arr,int i,int j)
    {
        long t = arr[j];
        arr[j] = arr[i];
        arr[i] = t;
    }
}