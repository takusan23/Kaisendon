package io.github.takusan23.Kaisendon.CustomMenu;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UriToByte {

    private Context context;

    public UriToByte(Context context){
        this.context = context;
    }

    public byte[] getByte(Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while((len=inputStream.read(buffer))!=-1){
            byteArrayOutputStream.write(buffer,0,len);
        }
        return byteArrayOutputStream.toByteArray();
    }

}
