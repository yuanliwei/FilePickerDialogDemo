package com.ylw.filepickerdialogdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.ylw.filepickerdialog.FilePickerDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(v -> {
            new FilePickerDialog(this)
                    .setExtensions(new String[]{".wav"})
                    .setPickMode(FilePickerDialog.PICK_MODE_SINGLE)
                    .show(files -> Toast.makeText(this, "Sel:" + files, Toast.LENGTH_SHORT).show());
        });

        findViewById(R.id.button2).setOnClickListener(v -> {
            new FilePickerDialog(this)
                    .setPickType(FilePickerDialog.PICK_TYPE_ALL)
                    .setPickMode(FilePickerDialog.PICK_MODE_MULTIPLE)
                    .show(files -> Toast.makeText(this, "Sel:" + files, Toast.LENGTH_SHORT).show());
        });
    }
}
