package cn.wu1588.common.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.CommonAppContext;

/**
 * Created by cxf on 2018/6/22.
 */

public class BitmapUtil {

    private static BitmapUtil sInstance;
    private Resources mResources;
    private BitmapFactory.Options mOptions;

    private BitmapUtil() {
        mResources = CommonAppContext.sInstance.getResources();
        mOptions = new BitmapFactory.Options();
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mOptions.inDither = true;
        mOptions.inSampleSize = 1;
    }

    public static BitmapUtil getInstance() {
        if (sInstance == null) {
            synchronized (BitmapUtil.class) {
                if (sInstance == null) {
                    sInstance = new BitmapUtil();
                }
            }
        }
        return sInstance;
    }


    public Bitmap decodeBitmap(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        return new SoftReference<>(bitmap).get();
    }


    public Bitmap decodeBitmap(int imgRes) {
        Bitmap bitmap = null;
        try {
            byte[] bytes = IOUtils.toByteArray(mResources.openRawResource(imgRes));
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, mOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SoftReference<>(bitmap).get();
    }

    /**
     * 把Bitmap保存成图片文件
     *
     * @param bitmap
     */
    public String saveBitmap(Bitmap bitmap) {
        String path = null;
        File dir = new File(CommonAppConfig.CAMERA_IMAGE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File imageFile = new File(dir, DateFormatUtil.getCurTimeString() + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            path = imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }


    /**
     * 把Bitmap保存成图片文件
     */
    public boolean saveBitmap(Bitmap bitmap, File imageFile) {
        boolean success = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            saveImageInfo(imageFile);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }


    /**
     * 把视频保存到ContentProvider,在选择上传的时候能找到
     */
    public static void saveImageInfo(File file) {
        try {
            String fileName = file.getName();
            long currentTimeMillis = System.currentTimeMillis();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            CommonAppContext.sInstance.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}
