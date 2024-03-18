package com.android_document_tree;

import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final Uri initialDirectoryUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fmark.via");
    Button grantedDirectory;
    Button addFile;
    Button modifyFile;
    Button deleteFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        grantedDirectory = findViewById(R.id.granted_directory_button);
        addFile = findViewById(R.id.add_file_button);
        modifyFile = findViewById(R.id.modify_file_button);
        deleteFile = findViewById(R.id.delete_file_button);

        ActivityResultLauncher<Uri> treeUriResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            if (result.equals(initialDirectoryUri)) {
                                // 保存对该目录的权限，防止手机重启后失效
                                MainActivity.this.getContentResolver().takePersistableUriPermission(result,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                showToastText("授予目录成功");
                            } else {
                                showToastText("目录授予错误，请重新授予");
                            }
                        } else {
                            showToastText("未授予目录");
                        }
                    }
                });

        // 授予目录
        grantedDirectory.setOnClickListener(v -> {
            if (isDirectoryGranted()) {
                showToastText("你已授予目录，无需再次授予");
            } else {
                DocumentFile documentFile = DocumentFile.fromTreeUri(this, initialDirectoryUri);
                if (documentFile != null) {
                    Log.d(TAG, "onCreate: " + documentFile.getUri());
                    treeUriResultLauncher.launch(documentFile.getUri());
                }
            }
        });

        // 添加demo.txt文件
        addFile.setOnClickListener(v -> {
            if (!isDirectoryGranted()) {
                showToastText("你未授予目录，请先授予后重试");
                return;
            } else if (FileOperation.getInstance().exists(this,
                    FileOperation.getInstance().getFileUri(this, initialDirectoryUri, "demo.txt"))) {
                showToastText("文件已存在");
                return;
            }

            boolean createFile = FileOperation.getInstance().createFile(
                    this,
                    initialDirectoryUri,
                    "text/plain",
                    "demo", "这是新建的文件内容");

            if (createFile) {
                showToastText("添加成功");
            } else {
                showToastText("添加失败");
            }
        });

        // 修改demo.txt文件
        modifyFile.setOnClickListener(v -> {
            DocumentFile rootDire = DocumentFile.fromTreeUri(this, initialDirectoryUri);
            if (!isDirectoryGranted()) {
                showToastText("你未授予目录，请先授予后重试");
                return;
            } else if (rootDire == null) {
                showToastText("你的安卓系统低于安卓5.0，不支持此功能");
                return;
            }

            Uri fileUri = FileOperation.getInstance().getFileUri(this, rootDire.getUri(), "demo.txt");
            if (fileUri != null) {
                boolean writeFileContent = FileOperation.getInstance().writeFileContent(this, fileUri, "这是经过修改的文件内容");
                showToastText(writeFileContent ? "修改成功" : "修改失败");
            } else {
                showToastText("文件不存在");
            }
        });

        // 删除demo.txt文件
        deleteFile.setOnClickListener(v -> {
            if (!isDirectoryGranted()) {
                showToastText("你未授予目录，请先授予后重试");
                return;
            }

            Uri fileUri = FileOperation.getInstance().getFileUri(this, initialDirectoryUri, "demo.txt");
            if (fileUri != null) {
                boolean d = FileOperation.getInstance().delete(this, fileUri);
                showToastText(d ? "删除成功" : "删除失败");
            } else {
                showToastText("文件不存在");
            }
        });
    }

    // 判断目录是否授予
    private boolean isDirectoryGranted() {
        List<UriPermission> list = getContentResolver().getPersistedUriPermissions();
        for (UriPermission uriPermission : list) {
            if (uriPermission.getUri().equals(initialDirectoryUri)) {
                return true;
            }
        }
        return false;
    }

    private void showToastText(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
