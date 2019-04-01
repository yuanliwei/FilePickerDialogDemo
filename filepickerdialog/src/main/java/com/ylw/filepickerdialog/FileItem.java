package com.ylw.filepickerdialog;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 袁立位 on 2019/4/1 14:05.
 */
public class FileItem {
    private final String[] extensions;
    public int position;
    File file;
    public boolean isChecked;

    public FileItem(File file, String[] extensions) {
        this.file = file;
        this.extensions = extensions;
    }

    public List<FileItem> getFiles() {
        List<FileItem> items = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    items.add(new FileItem(file, extensions));
                } else if (extensions.length == 0) {
                    items.add(new FileItem(file, extensions));
                } else {
                    String name = file.getName().toLowerCase();
                    for (String extension : extensions) {
                        if (name.endsWith(extension)) {
                            items.add(new FileItem(file, extensions));
                            break;
                        }
                    }
                }
            }
        }
        Collections.sort(items, (o1, o2) -> {
            if (o1.file.isDirectory() && o2.file.isFile()) {
                return -1;
            }
            if (o1.file.isFile() && o2.file.isDirectory()) {
                return 1;
            }
            return o1.file.getName().compareToIgnoreCase(o2.file.getName());
        });
        FileItem parent = new FileItem(file, extensions);
        items.add(0, parent);
        for (int i = 0; i < items.size(); i++) {
            FileItem fileItem = items.get(i);
            fileItem.position = i;
        }
        return items;
    }

    public void setOffset(int offset, Context context) {
        SharedPreferences preference = context.getSharedPreferences("file_picker", Context.MODE_PRIVATE);
        preference.edit().putInt(file.getAbsolutePath(), offset).apply();
    }

    public int getOffset(Context context) {
        SharedPreferences preference = context.getSharedPreferences("file_picker", Context.MODE_PRIVATE);
        return preference.getInt(file.getAbsolutePath(), 0);
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "position=" + position +
                ", file=" + file +
                ", isChecked=" + isChecked +
                '}';
    }
}
