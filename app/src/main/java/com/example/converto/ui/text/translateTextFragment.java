package com.example.converto.ui.text;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.converto.R;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;


public class translateTextFragment extends Fragment {

    private TextView mDisplayText;
    private EditText mTranslateText;
    private Spinner mTextLanguageSpinner;
    private ImageButton mInterchange;
    private Spinner mTranslationLanguageSpinner;

    private String textLanguage;
    private String translationLanguage;

    private languageViewModel viewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewModel = new ViewModelProvider(requireActivity()).get(languageViewModel.class);
        View root =  inflater.inflate(R.layout.fragment_translate_text, container, false);

        mDisplayText = root.findViewById(R.id.translated_text_tv);
        mTranslateText = root.findViewById(R.id.text_editView);
        mTextLanguageSpinner = root.findViewById(R.id.text_language_spinner);
        mInterchange = root.findViewById(R.id.interchange_button);
        mTranslationLanguageSpinner = root.findViewById(R.id.translation_language_spinner);

        ArrayAdapter<languageViewModel.Language> languageAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,viewModel.getAvailableLanguage());
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTextLanguageSpinner.setAdapter(languageAdapter);
        mTranslationLanguageSpinner.setAdapter(languageAdapter);
        mTextLanguageSpinner.setSelection(languageAdapter.getPosition(new languageViewModel.Language("en")));

        mTextLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textLanguage = String.valueOf(parent.getSelectedItem());
                viewModel.sourceLang.setValue(languageAdapter.getItem(position));
                viewModel.downloadModel(languageAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTranslateText.setText("");
            }
        });

        mTranslationLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                translationLanguage = String.valueOf(parent.getSelectedItem());
                mDisplayText.setText(R.string.translate_dialog);
                viewModel.targetLang.setValue(languageAdapter.getItem(position));
                viewModel.downloadModel(languageAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDisplayText.setText("");
            }
        });
        mTranslateText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDisplayText.setText(R.string.translate_dialog);
                viewModel.sourceText.setValue(s.toString());
            }
        });

        viewModel.translatedText.observe(getViewLifecycleOwner(), new Observer<languageViewModel.ResultOrError>() {
            @Override
            public void onChanged(languageViewModel.ResultOrError resultOrError) {
                if(resultOrError.error != null){
                    mTranslateText.setError(resultOrError.error.getLocalizedMessage());
                }
                else{
                    mDisplayText.setText(resultOrError.result);
                }
            }
        });

        return root;
    }
}