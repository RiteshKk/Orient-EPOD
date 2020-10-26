package com.ipssi.orient_epod.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;

import com.ipssi.orient_epod.R;
import com.ipssi.orient_epod.callbacks.OnLanguageSelectedListener;
import com.ipssi.orient_epod.model.LanguageDetail;

import java.util.ArrayList;

public class LanguageSelectorAdapter extends RecyclerView.Adapter<LanguageSelectorAdapter.ViewHolder> {
    private ArrayList<LanguageDetail> languageList;
    private String preSelectedLanguage;
    private AppCompatRadioButton preSelectedButton;
    private OnLanguageSelectedListener languageSelectorListener;


    public LanguageSelectorAdapter(OnLanguageSelectedListener listener, ArrayList<LanguageDetail> languageList, String preSelected) {
        this.languageList = languageList;
        this.preSelectedLanguage = preSelected;
        languageSelectorListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.language_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LanguageDetail language = languageList.get(position);
        StringBuilder sb = new StringBuilder();
        sb.append(language.getLangName());
        if (!language.getLangEnglishName().equalsIgnoreCase("English")) {
            sb.append(" (");
            sb.append(language.getLangEnglishName());
            sb.append(")");
        }else if(preSelectedButton == null){
            holder.selectedLangRadioButton.setChecked(true);
            preSelectedButton = holder.selectedLangRadioButton;
        }
        holder.languageText.setText(sb);
        if (language.getLangName().equalsIgnoreCase(preSelectedLanguage)) {
            holder.selectedLangRadioButton.setChecked(true);
            preSelectedButton = holder.selectedLangRadioButton;
        }
    }

    @Override
    public int getItemCount() {
        return languageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView languageText;
        AppCompatRadioButton selectedLangRadioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            languageText = itemView.findViewById(R.id.language_text);
            selectedLangRadioButton = itemView.findViewById(R.id.radio_button);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (preSelectedButton != null) {
                        if (preSelectedButton == selectedLangRadioButton) {
                            return;
                        }
                        preSelectedButton.setChecked(false);
                    }
                    selectedLangRadioButton.setChecked(true);
                    preSelectedButton = selectedLangRadioButton;
                    languageSelectorListener.onLanguageSelected(languageList.get(getAdapterPosition()));
                }
            });
        }
    }
}
