package com.example.computervisionapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.*;
import android.app.*;
import android.content.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.*;
import android.os.*;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import android.provider.*;

import com.google.gson.Gson;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import org.w3c.dom.Text;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.transform.Result;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisInDomainResult;
import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

public class RisultatoActivity extends AppCompatActivity {

    ImageView foto1,foto2;
    Button scopriTipo;

    // Replace `<API endpoint>` with the Azure region associated with
    // your subscription key. For example,
    // apiEndpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0"
    private final String apiEndpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0";

    // Replace `<Subscription Key>` with your subscription key.
    // For example, subscriptionKey = "0123456789abcdef0123456789ABCDEF"
    private final String subscriptionKey = "aa195d5c74024db083b81095b12ab38c";

    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private ProgressDialog detectionProgressDialog;

    Face[] risultato;
    ScrollView scrollView;
    TextView gender,age,anger,contempt,disgust,fear,happiness,neutral,sadness,surprise,finalrequest,risultato1,hashtagPhoto;
    public static Bitmap bitmap;
    int scelta,numFoto;
    String emotionFaceresult,emotionresult,fotoPath,hashtag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risultato);
        foto1 = (ImageView) findViewById(R.id.foto1);
        foto2 = (ImageView) findViewById(R.id.foto2);
        scopriTipo = (Button) findViewById(R.id.scopriTipo);
        gender = (TextView) findViewById(R.id.gender);
        age = (TextView) findViewById(R.id.age);
        anger = (TextView) findViewById(R.id.anger);
        contempt = (TextView) findViewById(R.id.contempt);
        disgust = (TextView) findViewById(R.id.disgust);
        fear = (TextView) findViewById(R.id.fear);
        happiness = (TextView) findViewById(R.id.happiness);
        neutral = (TextView) findViewById(R.id.neutral);
        sadness = (TextView) findViewById(R.id.sadness);
        surprise = (TextView) findViewById(R.id.surprise);
        finalrequest = findViewById(R.id.finalrequest);
        risultato1 = findViewById(R.id.risultato1);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        hashtagPhoto = (TextView) findViewById(R.id.hashtag);
        Intent intent = getIntent();
        hashtag = intent.getStringExtra("hashtag");
        hashtagPhoto.setText("#"+hashtag);
        fotoPath = intent.getStringExtra("fotoPath");
        numFoto = Integer.valueOf(fotoPath);
        cambiaFoto(Integer.valueOf(fotoPath));
        File image = new File(Environment.getExternalStorageDirectory()+"/selfie.jpg");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        foto1.setImageBitmap(bitmap);

        scopriTipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trovaEmozione();
            }
        });

        detectionProgressDialog = new ProgressDialog(this);
    }

    public void cambiaFoto(int y){
        if(y == 0){
            foto2.setImageResource(R.drawable.foto1);
        }
        else if(y == 1){
            foto2.setImageResource(R.drawable.foto2);
        }
        else if(y == 2){
            foto2.setImageResource(R.drawable.foto3);
        }
        else if(y == 3){
            foto2.setImageResource(R.drawable.foto4);
        }
        else if(y == 4){
            foto2.setImageResource(R.drawable.foto5);
        }
        else if(y == 5){
            foto2.setImageResource(R.drawable.foto6);
        }
        else if(y == 6){
            foto2.setImageResource(R.drawable.foto7);
        }
        else if(y == 7){
            foto2.setImageResource(R.drawable.foto8);
        }
        else if(y == 8){
            foto2.setImageResource(R.drawable.foto9);
        }
        else if(y == 9){
            foto2.setImageResource(R.drawable.foto10);
        }
    }

    protected void trovaEmozione(){
        foto1.setImageBitmap(bitmap);
        detectAndFrame(bitmap);

    }

    // Detect faces by uploading a face image.
    // Frame faces after detection.
    private void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    false,       /* Whether to return face ID */
                                    false,       /* Whether to return face landmarks */
                                    new FaceServiceClient.FaceAttributeType[] {
                                            FaceServiceClient.FaceAttributeType.Age,
                                            FaceServiceClient.FaceAttributeType.Gender,
                                            FaceServiceClient.FaceAttributeType.Emotion
                                    });
                            if (result.length == 0){
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                Intent myIntent = new Intent(RisultatoActivity.this, MainActivity.class);
                                myIntent.putExtra("returnValue","1");
                                myIntent.putExtra("returnFoto",fotoPath);
                                startActivity(myIntent);
                                return null;
                            }
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;
                        risultato = result;
                        foto1.setImageBitmap(
                                drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();
                        scelta = 1;
                        returnEmotionFace();
                        scrollView.setVisibility(View.VISIBLE);
                        scopriTipo.setVisibility(View.INVISIBLE);
                        gender.setText("GENDER = < " + String.valueOf(result[0].faceAttributes.gender) + " >");
                        age.setText("AGE = < " + String.valueOf(result[0].faceAttributes.age) + " >");
                        anger.setText("ANGER = < " + String.valueOf(result[0].faceAttributes.emotion.anger) + " >");
                        contempt.setText("CONTEMPT= < " + String.valueOf(result[0].faceAttributes.emotion.contempt) + " >");
                        disgust.setText("DISGUST = < " + String.valueOf(result[0].faceAttributes.emotion.disgust) + " >");
                        fear.setText("FEAR = < " + String.valueOf(result[0].faceAttributes.emotion.fear) + " >");
                        happiness.setText("HAPPINESS = < " + String.valueOf(result[0].faceAttributes.emotion.happiness) + " >");
                        neutral.setText("NEUTRAL = < " + String.valueOf(result[0].faceAttributes.emotion.neutral) + " >");
                        sadness.setText("SADNESS = < " + String.valueOf(result[0].faceAttributes.emotion.sadness) + " >");
                        surprise.setText("SURPRISE = < " + String.valueOf(result[0].faceAttributes.emotion.surprise) + " >");
                        risultato1.setText(emotionresult.toUpperCase());
                    }
                };

        detectTask.execute(inputStream);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }})
                .create().show();
    }

    private static Bitmap drawFaceRectanglesOnBitmap(
            Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);
        int x = 0;
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
                x++;
            }
        }
        return bitmap;
    }

    private void returnEmotionFace(){

        if(risultato[scelta-1].faceAttributes.emotion.anger > 0.5 ){
            emotionFaceresult = "angry";
        }
        else if(risultato[scelta-1].faceAttributes.emotion.contempt > 0.5 ){
            emotionFaceresult = "has contempt";
        }
        else if(risultato[scelta-1].faceAttributes.emotion.disgust > 0.5 ){
            emotionFaceresult = "disgusted";
        }
        else  if(risultato[scelta-1].faceAttributes.emotion.fear > 0.5 ){
            emotionFaceresult = "afraid";
        }
        else if(risultato[scelta-1].faceAttributes.emotion.happiness > 0.5 ){
            emotionFaceresult = "happy";
        }
        else if(risultato[scelta-1].faceAttributes.emotion.neutral > 0.5 ){
            if(risultato[scelta-1].faceAttributes.emotion.anger > 0.2 ){
                emotionFaceresult = "angry";
            }
            else if(risultato[scelta-1].faceAttributes.emotion.contempt > 0.2 ){
                emotionFaceresult = "has contempt";
            }
            else if(risultato[scelta-1].faceAttributes.emotion.disgust > 0.2 ){
                emotionFaceresult = "disgusted";
            }
            else  if(risultato[scelta-1].faceAttributes.emotion.fear > 0.2 ){
                emotionFaceresult = "afraid";
            }
            else if(risultato[scelta-1].faceAttributes.emotion.happiness > 0.2 ){
                emotionFaceresult = "happy";
            }
            else if(risultato[scelta-1].faceAttributes.emotion.sadness > 0.2 ){
                emotionFaceresult= "sad";
            }
            else if(risultato[scelta-1].faceAttributes.emotion.surprise > 0.2 ){
                emotionFaceresult = "surprised";
            }
            else {
                emotionFaceresult = "neutral";
            }
        }
        else if(risultato[scelta-1].faceAttributes.emotion.sadness > 0.5 ){
            emotionFaceresult= "sad";
        }
        else if(risultato[scelta-1].faceAttributes.emotion.surprise > 0.5 ){
            emotionFaceresult = "surprised";
        }

        returnEmotion();

    }

    private void returnEmotion(){
        if(numFoto == 0){//Paracadutismo
            if(emotionFaceresult == "afraid"){
               emotionresult = "Terrified";
            }
            else if(emotionFaceresult == "happy"){
                emotionresult = "Excited";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Swaggering";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 1){//mangia gelato
            if(emotionFaceresult == "happy"){
                emotionresult = "Hungry";
            }
            else if(emotionFaceresult == "sad"){
                emotionresult = "Eager";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Indifferent";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 2){//goal
            if(emotionFaceresult == "happy"){
                emotionresult = "Excited";
            }
            else if(emotionFaceresult == "angry"){
                emotionresult = "Grudging";
            }
            else if(emotionFaceresult == "surprised"){
                emotionresult = "Enthusiastic";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 3){//Uomo grasso
            if(emotionFaceresult == "happy"){
                emotionresult = "Bastard";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Disinterested";
            }
            else if(emotionFaceresult == "disgusted"){
                emotionresult = "nauseated";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 4){//Presa elettrica
            if(emotionFaceresult == "happy"){
                emotionresult = "Insane";
            }
            else if(emotionFaceresult == "afraid"){
                emotionresult = "Scared";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Disinterested";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 5){//Tramonto al mare
            if(emotionFaceresult == "sad"){
                emotionresult = "melancholy";
            }
            else if(emotionFaceresult == "happy"){
                emotionresult = "Enamored";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Apathetic";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 6){//Ufo
            if(emotionFaceresult == "happy"){
                emotionresult = "Excited";
            }
            else if(emotionFaceresult == "surprised"){
                emotionresult = "Incredulous";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Realist";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 7){//Cane morto
            if(emotionFaceresult == "sad"){
                emotionresult = "Grieved";
            }
            else if(emotionFaceresult == "happy"){
                emotionresult = "macabre";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Insensitive";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 8){//Ragazzi si baciano
            if(emotionFaceresult == "happy"){
                emotionresult = "Cheerful";
            }
            else if(emotionFaceresult == "angry"){
                emotionresult = "Jealousy";
            }
            else if(emotionFaceresult == "neutral"){
                emotionresult = "Indifferent";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
        else if(numFoto == 9){//Clown che ride
            if(emotionFaceresult == "afraid"){
                emotionresult = "Scared";
            }
            else if(emotionFaceresult == "happy"){
                emotionresult = "Entertained";
            }
            else if(emotionFaceresult == "surprised"){
                emotionresult = "Curious";
            }
            else{
                emotionresult = emotionFaceresult;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(RisultatoActivity.this, MainActivity.class);
        myIntent.putExtra("returnFoto",fotoPath);
        myIntent.putExtra("returnValue","0");
        startActivity(myIntent);
    }
}
