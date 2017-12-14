package com.calvin.smartfilemanager.misc;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.support.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:17-12-12
 */

public class ImageUtils {


    private static final String TAG = "ImageUtils";
    private static final String BASE64_URI_PREFIX = "base64,";
    private static final Pattern BASE64_IMAGE_URI_PATTERN = Pattern.compile("^(?:.*;)?base64,.*");

    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    public static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

    public static Bitmap getThumbnail(ContentResolver resolver, Uri imageUri, int mMaxWidth, int mMaxHeight) {
        try {
            return getContentThumbnail(resolver, imageUri, mMaxWidth, mMaxHeight);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * Scales one side of a rectangle to fit aspect ratio.
     *
     * @param maxPrimary Maximum size of the primary dimension (i.e. width for
     *        max width), or zero to maintain aspect ratio with secondary
     *        dimension
     * @param maxSecondary Maximum size of the secondary dimension, or zero to
     *        maintain aspect ratio with primary dimension
     * @param actualPrimary Actual size of the primary dimension
     * @param actualSecondary Actual size of the secondary dimension
     * @param scaleType The ScaleType used to calculate the needed image size.
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
                                           int actualSecondary, ImageView.ScaleType scaleType) {
        // If no dominant value at all, just return the actual.
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }
        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        if (scaleType == ImageView.ScaleType.FIT_XY) {
            if (maxPrimary == 0) {
                return actualPrimary;
            }
            return maxPrimary;
        }
        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }
        if (maxSecondary == 0) {
            return maxPrimary;
        }
        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        // If ScaleType.CENTER_CROP fill the whole rectangle, preserve aspect ratio.
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if ((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }
        if ((resized * ratio) > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    private static Bitmap getContentThumbnail(ContentResolver resolver, Uri imageUri, int mMaxWidth, int mMaxHeight) {

        Bitmap.Config mDecodeConfig = Bitmap.Config.RGB_565;
        ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER_CROP;

        Bitmap bitmap = null;

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inInputShareable = true;
        decodeOptions.inPurgeable = true;
        decodeOptions.inPreferredConfig = mDecodeConfig;

        if (mMaxWidth == 0 && mMaxHeight == 0) {
            bitmap = ImageUtils.decodeStream(resolver, imageUri, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            ImageUtils.decodeStream(resolver, imageUri, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight, mScaleType);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth, mScaleType);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;

            decodeOptions.inSampleSize = ImageUtils.findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = ImageUtils.decodeStream(resolver, imageUri, decodeOptions);
            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }
        return bitmap;
    }

    /**
     * Create a bitmap from a local URI
     *
     * @param resolver The ContentResolver
     * @param uri      The local URI
     * @return The new bitmap or null
     */
    public static Bitmap decodeStream(final ContentResolver resolver, final Uri uri,
                                      BitmapFactory.Options opts) {
        Bitmap result = null;
        final InputStreamFactory factory = createInputStreamFactory(resolver, uri);
        try {
            result = decodeStream(factory, null, opts);
            return result;

        } catch (FileNotFoundException exception) {
            // Do nothing - the photo will appear to be missing
        } catch (IllegalArgumentException exception) {
            // Do nothing - the photo will appear to be missing
        } catch (SecurityException exception) {
        }
        return result;
    }

    /**
     * Wrapper around {@link BitmapFactory#decodeStream(InputStream, Rect,
     * BitmapFactory.Options)} that returns {@code null} on {@link
     * OutOfMemoryError}.
     *
     * @param factory    Used to create input streams that holds the raw data to be decoded into a
     *                   bitmap.
     * @param outPadding If not null, return the padding rect for the bitmap if
     *                   it exists, otherwise set padding to [-1,-1,-1,-1]. If
     *                   no bitmap is returned (null) then padding is
     *                   unchanged.
     * @param opts       null-ok; Options that control downsampling and whether the
     *                   image should be completely decoded, or just is size returned.
     * @return The decoded bitmap, or null if the image data could not be
     * decoded, or, if opts is non-null, if opts requested only the
     * size be returned (in opts.outWidth and opts.outHeight)
     */
    public static Bitmap decodeStream(final InputStreamFactory factory, final Rect outPadding,
                                      final BitmapFactory.Options opts) throws FileNotFoundException {
        InputStream is = null;
        try {
            // Determine the orientation for this image
            is = factory.createInputStream();
            final ExifInterface exif = new ExifInterface(is);
            final int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            is.close();

            // Decode the bitmap
            is = factory.createInputStream();
            final Bitmap originalBitmap = BitmapFactory.decodeStream(is, outPadding, opts);

            if (is != null && originalBitmap == null && !opts.inJustDecodeBounds) {
                Log.w(TAG, "ImageUtils#decodeStream(InputStream, Rect, Options): "
                        + "Image bytes cannot be decoded into a Bitmap");
                throw new UnsupportedOperationException(
                        "Image bytes cannot be decoded into a Bitmap.");
            }

            // Rotate the Bitmap based on the orientation
            if (originalBitmap != null && orientation != 0) {
                final Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(),
                        originalBitmap.getHeight(), matrix, true);
            }
            return originalBitmap;
        } catch (OutOfMemoryError oome) {
            Log.e(TAG, "ImageUtils#decodeStream(InputStream, Rect, Options) threw an OOME", oome);
            return null;
        } catch (IOException ioe) {
            Log.e(TAG, "ImageUtils#decodeStream(InputStream, Rect, Options) threw an IOE", ioe);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Utility class for when an InputStream needs to be read multiple times. For example, one pass
     * may load EXIF orientation, and the second pass may do the actual Bitmap decode.
     */
    public interface InputStreamFactory {

        /**
         * Create a new InputStream. The caller of this method must be able to read the input
         * stream starting from the beginning.
         * @return
         */
        InputStream createInputStream() throws FileNotFoundException;
    }

    public static InputStreamFactory createInputStreamFactory(final ContentResolver resolver,
                                                              final Uri uri) {
        final String scheme = uri.getScheme();
        if ("data".equals(scheme)) {
            return new DataInputStreamFactory(resolver, uri);
        }
        return new BaseInputStreamFactory(resolver, uri);
    }

    private static class BaseInputStreamFactory implements InputStreamFactory {
        protected final ContentResolver mResolver;
        protected final Uri mUri;

        public BaseInputStreamFactory(final ContentResolver resolver, final Uri uri) {
            mResolver = resolver;
            mUri = uri;
        }

        @Override
        public InputStream createInputStream() throws FileNotFoundException {
            return mResolver.openInputStream(mUri);
        }
    }

    private static class DataInputStreamFactory extends BaseInputStreamFactory {
        private byte[] mData;

        public DataInputStreamFactory(final ContentResolver resolver, final Uri uri) {
            super(resolver, uri);
        }

        @Override
        public InputStream createInputStream() throws FileNotFoundException {
            if (mData == null) {
                mData = parseDataUri(mUri);
                if (mData == null) {
                    return super.createInputStream();
                }
            }
            return new ByteArrayInputStream(mData);
        }

        private byte[] parseDataUri(final Uri uri) {
            final String ssp = uri.getSchemeSpecificPart();
            try {
                if (ssp.startsWith(BASE64_URI_PREFIX)) {
                    final String base64 = ssp.substring(BASE64_URI_PREFIX.length());
                    return Base64.decode(base64, Base64.URL_SAFE);
                } else if (BASE64_IMAGE_URI_PATTERN.matcher(ssp).matches()){
                    final String base64 = ssp.substring(
                            ssp.indexOf(BASE64_URI_PREFIX) + BASE64_URI_PREFIX.length());
                    return Base64.decode(base64, Base64.DEFAULT);
                } else {
                    return null;
                }
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, "Mailformed data URI: " + ex);
                return null;
            }
        }
    }

}
