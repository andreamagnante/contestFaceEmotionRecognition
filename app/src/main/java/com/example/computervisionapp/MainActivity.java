package com.example.computervisionapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button btnCapture;
    private ImageButton sinistra,destra;
    private TextureView textureView;
    private ImageView play,foto;
    int x = 0;

    //Check state orientation of output image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    //Save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    String returnValue = "0";
    String hashtag = "GIOIA";

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView)findViewById(R.id.textureView);
        //From Java 1.4 , you can use keyword 'assert' to check expression true or false
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        play = (ImageView) findViewById(R.id.play);
        foto = (ImageView) findViewById(R.id.foto);
        btnCapture = (Button)findViewById(R.id.btnCapture);
        sinistra = (ImageButton) findViewById(R.id.sinistra);
        destra = (ImageButton) findViewById(R.id.destra);
        Intent intent = getIntent();
        if(intent.getExtras() != null){
            returnValue= intent.getStringExtra("returnValue");
            if(Integer.parseInt(returnValue) == 1){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Nessun volto rilevato");
                builder.setMessage("Riprova di nuovo");
                builder.show();
                returnValue = "0";
            }
            String fotoPath = intent.getStringExtra("returnFoto");
            if(Integer.valueOf(fotoPath) == 9){
                x = 0;
            }else {
                x = Integer.valueOf(fotoPath) + 1;
            }
            cambia(0);
        }
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setVisibility(View.INVISIBLE);
                foto.setVisibility(View.VISIBLE);
                final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

                exec.schedule(new Runnable(){
                    @Override
                    public void run(){
                        takePicture();

                    }
                }, 1, TimeUnit.SECONDS);

            }
        });
        sinistra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   cambia(0);
            }
        });
        destra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambia(1);
            }
        });
    }


    private void cambia(int i){
        foto.setVisibility(View.INVISIBLE);
        play.setVisibility(View.VISIBLE);
        if(x == 0){
            if(i == 0){
                play.setImageResource(R.drawable.play10);
                foto.setImageResource(R.drawable.foto10);
                hashtag = "GIOIA";
                x = 9;
            }
            else{
                play.setImageResource(R.drawable.play2);
                foto.setImageResource(R.drawable.foto2);
                hashtag = "GIOIA";
                x++;
            }
        }
        else if (x == 1){
            if(i == 0){
                play.setImageResource(R.drawable.play1);
                foto.setImageResource(R.drawable.foto1);
                hashtag = "GIOIA";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play3);
                foto.setImageResource(R.drawable.foto3);
                hashtag = "GIOIA";
                x++;
            }
        }
        else if (x == 2){
            if(i == 0){
                play.setImageResource(R.drawable.play2);
                foto.setImageResource(R.drawable.foto2);
                hashtag = "GIOIA";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play4);
                foto.setImageResource(R.drawable.foto4);
                hashtag = "DISGUSTO";
                x++;
            }
        }
        else if (x == 3){
            if(i == 0){
                play.setImageResource(R.drawable.play3);
                foto.setImageResource(R.drawable.foto3);
                hashtag = "GIOIA";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play5);
                foto.setImageResource(R.drawable.foto5);
                hashtag = "NEUTRO";
                x++;
            }
        }
        else if (x == 4){
            if(i == 0){
                play.setImageResource(R.drawable.play4);
                foto.setImageResource(R.drawable.foto4);
                hashtag = "DISGUSTO";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play6);
                foto.setImageResource(R.drawable.foto6);
                hashtag = "GIOIA";
                x++;
            }
        }
        else if (x == 5){
            if(i == 0){
                play.setImageResource(R.drawable.play5);
                foto.setImageResource(R.drawable.foto5);
                hashtag = "NEUTRO";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play7);
                foto.setImageResource(R.drawable.foto7);
                hashtag = "SORPRESO";
                x++;
            }
        }
        else if (x == 6){
            if(i == 0){
                play.setImageResource(R.drawable.play6);
                foto.setImageResource(R.drawable.foto6);
                hashtag = "GIOIA";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play8);
                foto.setImageResource(R.drawable.foto8);
                hashtag = "TRISTE";
                x++;
            }
        }
        else if (x == 7){
            if(i == 0){
                play.setImageResource(R.drawable.play7);
                foto.setImageResource(R.drawable.foto7);
                hashtag = "SORPRESO";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play9);
                foto.setImageResource(R.drawable.foto9);
                hashtag = "GIOIA";
                x++;
            }
        }
        else if (x == 8){
            if(i == 0){
                play.setImageResource(R.drawable.play8);
                foto.setImageResource(R.drawable.foto8);
                hashtag = "TRISTE";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play10);
                foto.setImageResource(R.drawable.foto10);
                hashtag = "GIOIA";
                x++;
            }
        }
        else if (x == 9){
            if(i == 0){
                play.setImageResource(R.drawable.play9);
                foto.setImageResource(R.drawable.foto9);
                hashtag = "GIOIA";
                x--;
            }
            else{
                play.setImageResource(R.drawable.play1);
                foto.setImageResource(R.drawable.foto1);
                hashtag = "GIOIA";
                x=0;
            }
        }

    }

    private void takePicture() {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Size[] jpegSizes = null;
            if(characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //Capture image with custom size
            int width = 640;
            int height = 480;
            if(jpegSizes != null && jpegSizes.length > 0)
            {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,270);

            file = new File(Environment.getExternalStorageDirectory()+"/selfie.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        Intent myIntent = new Intent(MainActivity.this, RisultatoActivity.class);
                        myIntent.putExtra("hashtag",String.valueOf(hashtag));
                        myIntent.putExtra("fotoPath",String.valueOf(x));
                        startActivity(myIntent);

                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        {
                            if(image != null)
                                image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally {
                        if(outputStream != null)
                            outputStream.close();
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId,stateCallback,null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread= null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}



