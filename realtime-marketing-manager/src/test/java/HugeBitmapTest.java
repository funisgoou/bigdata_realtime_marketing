import org.roaringbitmap.RoaringBitmap;

import java.io.*;

public class HugeBitmapTest {
    public static void main(String[] args) throws IOException {
        //存1亿个用户id到bitmap
        RoaringBitmap bm=RoaringBitmap.bitmapOf();
        for(int i=0;i<100000000;i++){
            bm.add(i);
        }
        float i = (float)bm.serializedSizeInBytes()/1024/1024;
        System.out.println(i);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/bitmap.dat"));
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
        bm.serialize(dataOutputStream);
        dataOutputStream.close();
        fileOutputStream.close();
    }
}
