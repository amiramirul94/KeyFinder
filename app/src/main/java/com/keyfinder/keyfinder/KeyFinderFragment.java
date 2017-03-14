package com.keyfinder.keyfinder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContentResolverCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.api.request.model.CreateModelRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.input.image.Crop;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;


/**
 * Created by Lenovo on 11/2/2016.
 */

public class KeyFinderFragment extends Fragment {

    private static final String TAG = KeyFinderFragment.class.getSimpleName();
    private static final int REQUEST_PHOTO = 1;
    private static final String FAILED_TAG = "AUTHENTICATION_FAILED";
    private static final String GOOGLE_VISION_KEY = "AIzaSyBfPc3nBDAN4dIZKv4xEaDayQkuyI_cYf4";
    private static final String VISION_URL = "https://vision.googleapis.com/v1/images:annotate?key="+GOOGLE_VISION_KEY;
    private static final String SUCCESS_TAG = "AUTHENTICATION SUCCESFUL";


    private ImageButton mSelectImageButton;
    private Button mAuthenticateButton;
    private ImageView mImageView;
    private Uri mImageUri;
    private String encodedImage;
    private Vision vision;
    private boolean containsObject;

    public static KeyFinderFragment newInstance()
    {
        return new KeyFinderFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.key_finder_fragment,container,false);

        mSelectImageButton = (ImageButton) v.findViewById(R.id.select_image_button);
        mImageView = (ImageView) v.findViewById(R.id.image);

        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_PHOTO);
            }
        });

        mAuthenticateButton = (Button) v.findViewById(R.id.authenticate);

        mAuthenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                AuthenticationSuccessDialog dialog = new AuthenticationSuccessDialog();
//                dialog.show(manager,FAILED_TAG);

                try {
                    processImage();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK)
        {
            if(requestCode==REQUEST_PHOTO)
            {
                mImageUri = data.getData();

                Log.v(TAG,mImageUri.toString());

                Picasso.with(getActivity()).load(mImageUri).fit().centerCrop().into(mImageView);

            }
        }
    }

    private byte[] convertToByte() throws FileNotFoundException {


        InputStream inputStream = getActivity().getContentResolver().openInputStream(mImageUri); //You can get an inputStream using any IO API
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();

        return bytes;
    }

    private void processImage() throws FileNotFoundException {



// Due to a bug: requests to Vision API containing large images fail when GZipped.
        //annotate.setDisableGZipContent(true);
        new GetPredictions().execute();
    }


    private void getLabels(List<EntityAnnotation> annotations)
    {
        for(EntityAnnotation e: annotations)
        {
            Log.v(TAG,e.getDescription());
        }
    }

    public static Vision getVisionService() throws IOException, GeneralSecurityException {


        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
         Vision.Builder build = new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
                .setVisionRequestInitializer(new VisionRequestInitializer(GOOGLE_VISION_KEY));

        Vision vision = build.build();
        return vision;
    }



    private class GetPredictions extends AsyncTask<Void,Void,List<Concept>>{

        @Override
        protected List<Concept> doInBackground(Void... voids) {
            ClarifaiClient client = new ClarifaiBuilder("WvjHHlmndorWC8u3QWKQ8wIMVBcGYMUVhdFp_5oS", "N_h-Ecrm3idUqXCFbrLt3MI9M15339Ju_YQUwyBk").client(new OkHttpClient()).buildSync();


            List<ClarifaiOutput<Concept>> predictionResult = null;
            try {
                predictionResult = client.getDefaultModels().generalModel()
                        .predict()
                        .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(convertToByte())))
                        .executeSync()
                        .get();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            List<Concept> predictions = predictionResult.get(0).data();


            return predictions;
        }

        @Override
        protected void onPostExecute(List<Concept> concepts) {


            super.onPostExecute(concepts);
            for (Concept c: concepts) {

                String obj = c.name();
                Log.v(TAG,obj);

                if(obj.equals("book")){
                    containsObject = true;
                    break;
                }

            }
            FragmentManager manager = getFragmentManager();


            if(containsObject)
            {

                AuthenticationSuccessDialog successDialog = new AuthenticationSuccessDialog();
                successDialog.show(manager,SUCCESS_TAG);
                Log.v(TAG,"SUCCESS");
            }else{
                AuthenticationFailedDialog dialog = new AuthenticationFailedDialog();
                dialog.show(manager,FAILED_TAG);
                Log.v(TAG,"failed");
            }

        }
    }







}
