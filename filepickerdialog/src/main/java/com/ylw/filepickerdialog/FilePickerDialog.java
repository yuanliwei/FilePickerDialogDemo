package com.ylw.filepickerdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * FilePickerDialog
 * <p>
 * Created by 袁立位 on 2019/4/1 11:18.
 */
public class FilePickerDialog {

    public static final int PICK_TYPE_FOLDER = 0;
    public static final int PICK_TYPE_FILE = 1;
    public static final int PICK_TYPE_ALL = 2;

    public static final int PICK_MODE_SINGLE = 0;
    public static final int PICK_MODE_MULTIPLE = 1;

    private static final String TAG = "FilePickerDialog";
    final Context context;
    String[] extensions = new String[]{};
    Stack<FileItem> stack = new Stack<>();
    ListView listView;
    TextView subTitleView;
    int pickType = PICK_TYPE_ALL;
    int pickMode = PICK_MODE_MULTIPLE;
    Dialog dialog;
    IOnSelectFile callback;
    boolean showHiddenFile = false;
    private String rootDir;
    private String currentDir;
    private FileListAdapter adapter;
    private CharSequence title = "Choose File Dialog";

    public FilePickerDialog(Context context) {
        this.context = context;
        this.rootDir = "/mnt";
        SharedPreferences preference = context.getSharedPreferences("file_picker", Context.MODE_PRIVATE);
        this.currentDir = preference.getString("last_file", rootDir);
        if ((currentDir != null ? currentDir.length() : 0) < rootDir.length()) {
            currentDir = rootDir;
        }
        if (!checkStorageAccessPermissions(context)) {
            Toast.makeText(context, "checkStorageAccessPermissions false!", Toast.LENGTH_SHORT).show();
        }
    }

    public FilePickerDialog setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public FilePickerDialog setRootDir(String rootDir) {
        this.rootDir = rootDir;
        return this;
    }

    public FilePickerDialog setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
        return this;
    }

    public FilePickerDialog showHiddenFile(boolean showHiddenFile) {
        this.showHiddenFile = showHiddenFile;
        return this;
    }

    /**
     * @param extensions null: all || new String[]{".wav"} || new String[]{".wav", ".mp4}
     * @return FilePickerDialog
     */
    public FilePickerDialog setExtensions(String[] extensions) {
        if (extensions == null) {
            extensions = new String[]{};
        }
        this.extensions = extensions;
        return this;
    }

    /**
     * @param pickType FilePickerDialog.PICK_TYPE_FOLDER
     *                 FilePickerDialog.PICK_TYPE_FILE
     *                 FilePickerDialog.PICK_TYPE_ALL
     * @return FilePickerDialog
     */
    public FilePickerDialog setPickType(int pickType) {
        this.pickType = pickType;
        return this;
    }

    /**
     * @param pickMode FilePickerDialog.PICK_MODE_SINGLE
     *                 FilePickerDialog.PICK_MODE_MULTIPLE
     * @return FilePickerDialog
     */
    public FilePickerDialog setPickMode(int pickMode) {
        this.pickMode = pickMode;
        return this;
    }

    private boolean checkStorageAccessPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String permission = "android.permission.READ_EXTERNAL_STORAGE";
            int res = context.checkCallingOrSelfPermission(permission);
            return (res == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    public FilePickerDialog show(IOnSelectFile callback) {
        this.callback = callback;
        File cur = new File(currentDir);
        File root = new File(rootDir);
        while (cur != null && cur.getAbsolutePath().length() > root.getAbsolutePath().length()) {
            stack.insertElementAt(new FileItem(cur, extensions, showHiddenFile, pickType), 0);
            cur = cur.getParentFile();
        }
        SharedPreferences preference = context.getSharedPreferences("file_picker", Context.MODE_PRIVATE);
        String positionString = preference.getString("positions", null);
        if (positionString == null || positionString.length() == 0) {
            positionString = "0";
        }
        String[] pos = positionString.split(",");
        for (int i = 0; i < stack.size() && i < pos.length; i++) {
            stack.get(i).position = Integer.parseInt(pos[i]);
        }
        dialog = new Dialog(context) {
            @Override
            public void onBackPressed() {
                if (stack.size() > 0) {
                    FileItem peek = stack.pop();
                    adapter.setData(new FileItem(peek.file.getParentFile(), extensions, showHiddenFile, pickType).getFiles());
                    listView.smoothScrollToPositionFromTop(peek.position, peek.getOffset(context), 0);
                } else {
                    super.onBackPressed();
                }
            }
        };
        dialog.setOnDismissListener(dialog1 -> {
            StringBuilder sb = new StringBuilder();
            for (FileItem fileItem : stack) {
                sb.append(fileItem.position).append(",");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            SharedPreferences preference1 = context.getSharedPreferences("file_picker", Context.MODE_PRIVATE);
            preference1.edit().putString("positions", sb.toString()).apply();
            preference1.edit().putInt("cur_position", listView.getFirstVisiblePosition()).apply();
        });
        View contentView = View.inflate(context, R.layout.list_layout, null);
        listView = contentView.findViewById(R.id.list);
        TextView titleView = contentView.findViewById(R.id.title);
        subTitleView = contentView.findViewById(R.id.path);
        titleView.setText(title);
        subTitleView.setText(currentDir);
        contentView.findViewById(R.id.ok).setOnClickListener(v -> {
            List<FileItem> lists = adapter.getLists();
            List<File> files = new ArrayList<>();
            for (FileItem item : lists) {
                if (item.isChecked) {
                    files.add(item.file);
                }
            }
            dialog.dismiss();
            callback.onSelectFile(files);
        });
        contentView.findViewById(R.id.cancle).setOnClickListener(v -> {
            dialog.dismiss();
        });
        adapter = new FileListAdapter(this);
        listView.setAdapter(adapter);
        dialog.setContentView(contentView);
        FileItem curItem = new FileItem(new File(currentDir), extensions, showHiddenFile, pickType);
        adapter.setData(curItem.getFiles());

        int cur_position = preference.getInt("cur_position", 0);
        listView.smoothScrollToPositionFromTop(cur_position, curItem.getOffset(context), 0);

        dialog.show();
        return this;
    }

    public interface IOnSelectFile {

        void onSelectFile(List<File> files);
    }
}
