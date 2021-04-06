package com.example.converto.ui.gallery;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.converto.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class ImageToTextFragment extends Fragment {


    private ImageToTextViewModel galleryViewModel;
    private TextView textView;
    private FloatingActionButton mAdd;
    private FloatingActionButton mCamera;
    private FloatingActionButton mGallery;
    private ImageView mImageView;


    private static final int IMAGE_CAPTURE = 0;
    private static final int GALLERY_PICK = 1;
    private boolean mClicked = false;
    private static final String TAG = ImageToTextFragment.class.getName();

    private Uri imageUri;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(ImageToTextViewModel.class);
        View root = inflater.inflate(R.layout.fragment_image_to_text, container, false);
        textView = root.findViewById(R.id.tv_recognized_text);
        textView.setText(R.string.dummy_text_1);
        mImageView = root.findViewById(R.id.imageView);
        mAdd = root.findViewById(R.id.fab_add_image);
        mCamera = root.findViewById(R.id.fab_camera);
        mGallery = root.findViewById(R.id.fab_gallery);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mClicked) {
                    mCamera.setVisibility(View.VISIBLE);
                    mGallery.setVisibility(View.VISIBLE);
                    mAdd.setImageResource(R.drawable.ic_cancel);
                    mClicked = true;
                }
                else{
                    mCamera.setVisibility(View.INVISIBLE);
                    mGallery.setVisibility(View.INVISIBLE);
                    mAdd.setImageResource(R.drawable.ic_add);
                    mClicked = false;
                }
            }
        });
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,IMAGE_CAPTURE);
            }
        });
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == IMAGE_CAPTURE || requestCode == GALLERY_PICK) && resultCode == RESULT_OK){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(mImageView);
            translate();
        }
    }
    private void translate(){
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(getContext(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextRecognizer textRecognizer = TextRecognition.getClient();
        Task<Text> result =
                textRecognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    Rect boundingBox = block.getBoundingBox();
                                    Point[] cornerPoints = block.getCornerPoints();
                                    String text = block.getText();
                                    textView.setText(text);


                                    for (Text.Line line: block.getLines()) {
                                        // ...
                                        for (Text.Element element: line.getElements()) {
                                            // ...
                                        }
                                    }
                                }
                                // [END get_text]
                                // [END_EXCLUDE]
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.e(TAG, "onFailure: "+ e.getMessage() );
                                    }
                                });

    }
}