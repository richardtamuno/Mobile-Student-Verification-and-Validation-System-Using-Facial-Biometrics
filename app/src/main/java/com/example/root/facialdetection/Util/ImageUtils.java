package com.example.root.facialdetection.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by root on 8/11/17.
 */

    public class ImageUtils {
        private static final int imgWidth = 720;
        private static final int imgHeight = 720;

        public static boolean resizeUploadImage(String sourcePath, String destPath) {
            Bitmap source = correctBitmapOrientation(sourcePath);
            Bitmap out = Bitmap.createScaledBitmap(source, imgWidth, imgHeight, false);

            File file = new File(destPath);
            try {
                FileOutputStream fOut = new FileOutputStream(file);
                out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();

                source.recycle();
                out.recycle();

                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                //failed to write stream to new destination, copy instead
                return copyImage(sourcePath, destPath);
            }
        }

        private static boolean copyImage(String sourcePath, String destPath){
            try{
                InputStream is = new FileInputStream(sourcePath);
                OutputStream os = new FileOutputStream(destPath);
                byte[] buff = new byte[1024];
                int len;
                while((len=is.read(buff))>0){
                    os.write(buff,0,len);
                }
                is.close();
                os.close();

                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private static Bitmap correctBitmapOrientation(String path){
            int orientation = 0;
            try {
                ExifInterface exif = new ExifInterface(path);
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (orientation == 0)
                return bitmap;
            else
                return rotateBitmap(bitmap, orientation);
        }

        private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }
            try {
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            }
            catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        }
    }

