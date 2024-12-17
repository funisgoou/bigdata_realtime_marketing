package cn.rtmk.test.bitmap_inject;

import org.roaringbitmap.RoaringBitmap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        int[] ruleProfileUsers={222,223,224,225,1,2,965};
        //把查询出来的人群的guid变成bitmap，发布到mysql
        //1.id转换成bitmap
        RoaringBitmap roaringBitmap = RoaringBitmap.bitmapOf(ruleProfileUsers);
        //2.把生成好的Bitmap序列化到一个字节数组中
        System.out.println(roaringBitmap);
    }
}
