package com.example.converto.ui.audio;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.converto.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class AudioToTextFragment extends Fragment {

    private static final int RESULT_AUDIO = 1;
    private AudioToTextViewModel slideshowViewModel;
    private  TextView textView;
    private FloatingActionButton mAddAudio;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                new ViewModelProvider(this).get(AudioToTextViewModel.class);
        View root = inflater.inflate(R.layout.fragment_audio_to_text, container, false);
        textView = root.findViewById(R.id.text_slideshow);
        mAddAudio = root.findViewById(R.id.fab_add_audio);

        textView.setText(R.string.audio_text);
        mAddAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent audioIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                audioIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                audioIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");
                try{
                    startActivityForResult(audioIntent, RESULT_AUDIO);
                    textView.setText("");
                }
                catch (ActivityNotFoundException e){
                    Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onClick: "+e.getMessage());
                }

            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_AUDIO && resultCode == RESULT_OK && data != null ){
            ArrayList<String> textAudio = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            textView.setText(textAudio.get(0));
        }
    }
}