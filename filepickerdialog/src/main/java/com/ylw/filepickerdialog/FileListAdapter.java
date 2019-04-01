package com.ylw.filepickerdialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by 袁立位 on 2019/4/1 11:57.
 */
class FileListAdapter extends BaseAdapter {

    private final FilePickerDialog picker;
    private List<FileItem> lists = new ArrayList<>();

    public FileListAdapter(FilePickerDialog picker) {
        this.picker = picker;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(picker.context).inflate(R.layout.list_item_layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setData((FileItem) getItem(position));
        return convertView;
    }

    public void setData(List<FileItem> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    public List<FileItem> getLists() {
        return lists;
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

    private class ViewHolder {
        private static final String TAG = "ViewHolder";
        private final View root;
        ImageView icon;
        TextView name;
        TextView time;
        CheckBox checkBox;

        public ViewHolder(View view) {
            this.root = view;
            this.icon = view.findViewById(R.id.icon);
            this.name = view.findViewById(R.id.name);
            this.time = view.findViewById(R.id.time);
            this.checkBox = view.findViewById(R.id.checkbox);
        }

        public void setData(FileItem item) {
            if (item.file.isDirectory()) {
                icon.setImageResource(R.drawable.floder);
                root.setOnClickListener(v -> {
                    if (item.position == 0) {
                        if (picker.stack.size() > 0) {
                            FileItem peek = picker.stack.pop();
                            FileListAdapter.this.setData(new FileItem(peek.file.getParentFile(), picker.extensions).getFiles());
                            picker.listView.smoothScrollToPositionFromTop(peek.position, peek.getOffset(picker.context), 0);
                            SharedPreferences preference = picker.context.getSharedPreferences("file_picker", Context.MODE_PRIVATE);
                            preference.edit().putString("last_file", peek.file.getParentFile().getAbsolutePath()).apply();
                        }
                    } else {
                        picker.stack.add(item);
                        item.setOffset((int) root.getY(), picker.context);
                        FileListAdapter.this.setData(item.getFiles());
                        SharedPreferences preference = picker.context.getSharedPreferences("file_picker", Context.MODE_PRIVATE);
                        preference.edit().putString("last_file", item.file.getAbsolutePath()).apply();
                    }
                });
            } else {
                icon.setImageResource(R.drawable.file);
                if (picker.pickMode == FilePickerDialog.PICK_MODE_SINGLE) {
                    root.setOnClickListener(v -> {
                        ArrayList<File> list = new ArrayList<>();
                        list.add(item.file);
                        picker.dialog.dismiss();
                        picker.callback.onSelectFile(list);
                    });
                } else {
                    root.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
                }
            }
            Log.i(TAG, "setData: position : " + item.position);
            if (item.position == 0) {
                name.setText("..");
                time.setText("");
                picker.subTitleView.setText(item.file.getPath());
            } else {
                name.setText(item.file.getName());
                time.setText(format.format(new Date(item.file.lastModified())));
            }
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isChecked = isChecked;
            });
            checkBox.setChecked(item.isChecked);
            if (picker.pickType == FilePickerDialog.PICK_TYPE_ALL) {
                checkBox.setVisibility(View.VISIBLE);
            } else if (picker.pickType == FilePickerDialog.PICK_TYPE_FILE) {
                if (item.file.isDirectory()) {
                    checkBox.setVisibility(View.INVISIBLE);
                } else {
                    checkBox.setVisibility(View.VISIBLE);
                }
            } else {
                if (item.file.isDirectory()) {
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    checkBox.setVisibility(View.INVISIBLE);
                }
            }
            if (picker.pickMode == FilePickerDialog.PICK_MODE_SINGLE) {
                checkBox.setVisibility(View.INVISIBLE);
            } else {
                checkBox.setVisibility(View.VISIBLE);
            }
        }
    }
}
