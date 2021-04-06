package com.example.converto.ui.text;

import android.app.Application;
import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.converto.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class languageViewModel extends AndroidViewModel {
    private static final int NUM_TRANSLATOR = 3;

    private final RemoteModelManager modelManager;
    private final LruCache<TranslatorOptions, Translator> translator = new LruCache<TranslatorOptions, Translator>(NUM_TRANSLATOR){

        @Override
        protected com.google.mlkit.nl.translate.Translator create(TranslatorOptions key) {
            return Translation.getClient(key);
        }

        @Override
        protected void entryRemoved(boolean evicted, TranslatorOptions key, Translator oldValue, Translator newValue) {
            oldValue.close();
        }
    };

    public MutableLiveData<Language> sourceLang = new MutableLiveData<>();
    public MutableLiveData<Language> targetLang = new MutableLiveData<>();
    public MutableLiveData<String> sourceText = new MutableLiveData<>();
    public MediatorLiveData<ResultOrError> translatedText = new MediatorLiveData<>();
    public MutableLiveData<List<String >> availableModels = new MutableLiveData<>();


    public languageViewModel(@NonNull Application application) {
        super(application);
        modelManager = RemoteModelManager.getInstance();

        final OnCompleteListener<String> translationProcess = new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful())
                    translatedText.setValue(new ResultOrError(task.getResult(),null));

                else
                    translatedText.setValue(new ResultOrError(null, task.getException()));
            }

        };
            Observer<Language> languageObserver = new Observer<Language>() {
                @Override
                public void onChanged(Language language) {
                    translate().addOnCompleteListener(translationProcess);
                }
            };
            translatedText.addSource(sourceLang,languageObserver);
            translatedText.addSource(targetLang,languageObserver);
            fetchDownloadedModels();

    }

    public void fetchDownloadedModels(){
        modelManager.getDownloadedModels(TranslateRemoteModel.class).addOnSuccessListener(new OnSuccessListener<Set<TranslateRemoteModel>>() {
            @Override
            public void onSuccess(Set<TranslateRemoteModel> translateRemoteModels) {
                List<String> modelCodes = new ArrayList<>(translateRemoteModels.size());
                for(TranslateRemoteModel model : translateRemoteModels){
                    modelCodes.add(model.getLanguage());
                }
                Collections.sort(modelCodes);
                availableModels.setValue(modelCodes);
            }
        });

    }

    private TranslateRemoteModel getModel(String languageCode) {
        return new TranslateRemoteModel.Builder(languageCode).build();

    }

    void downloadModel(Language language){
        TranslateRemoteModel model = getModel(TranslateLanguage.fromLanguageTag(language.getCode()));
        modelManager.download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fetchDownloadedModels();
                    }
                });
    }

    void deleteModel(Language language){
        TranslateRemoteModel model = getModel(TranslateLanguage.fromLanguageTag(language.getCode()));
        modelManager.deleteDownloadedModel(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                fetchDownloadedModels();
            }
        });
        
    }

    public List<Language> getAvailableLanguage(){
        List<Language> languages = new ArrayList<>();
        List<String> languageIds = TranslateLanguage.getAllLanguages();
        for (String ids:languageIds) {
            languages.add(new Language(TranslateLanguage.fromLanguageTag(ids)));
        }
        return languages;
    };

    public Task<String> translate(){
        final String text = sourceText.getValue();
        final Language source = (Language) sourceLang.getValue();
        final Language target = (Language) targetLang.getValue();

        if( source == null || target == null || text == null || text.isEmpty())
            return Tasks.forResult("");

        String sourceLanguageCode = TranslateLanguage.fromLanguageTag(source.getCode());
        String targetLanguageCode = TranslateLanguage.fromLanguageTag(target.getCode());

        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguageCode)
                        .setTargetLanguage(targetLanguageCode)
                        .build();
        return translator.get(options).downloadModelIfNeeded().continueWithTask(new Continuation<Void, Task<String>>() {
            @Override
            public Task<String> then(@NonNull Task<Void> task) throws Exception {
                if(task.isSuccessful())
                    return translator.get(options).translate(text);

                else{
                    Exception e = task.getException();
                    if(e == null){
                        e = new Exception(getApplication().getString(R.string.common_google_play_services_unknown_issue));
                    }
                    return Tasks.forException(e);
                }

            }
        });
    }




    static class ResultOrError {

        final @Nullable
        String result;

        final @Nullable
        Exception error;

        public ResultOrError(@Nullable String result, @Nullable Exception error) {
            this.result = result;
            this.error = error;
        }
    }

    public static  class  Language implements Comparable<TranslateLanguage.Language>{

        private final String code;

        Language (String code){
            this.code = code;
        }

        public String getDisplayName(){
            return  new Locale(code).getDisplayName();
        }

        public String getCode(){
            return code;
        }

        public boolean equalTo(Object o){
            if(o == this)
                return true;
            if(!(o instanceof Language))
                return false;
            Language otherLanguage = (Language) o;
            return otherLanguage.code.equals(code);
        }

        @NotNull
        @Override
        public String toString() {
            return code + "-" + getDisplayName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Language language = (Language) o;
            return code.equals(language.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public int compareTo(TranslateLanguage.Language o) {
            return this.getDisplayName().compareTo(o.toString());
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        translator.evictAll();
    }
}
