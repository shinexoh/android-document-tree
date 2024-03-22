package com.android_document_tree;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.OutputStream;

public class FileOperation {
    private static final String TAG = "FileOperation";
    private static FileOperation instance;

    private FileOperation() {
    }

    public static FileOperation getInstance() {
        if (instance == null) {
            instance = new FileOperation();
        }
        return instance;
    }

    // 创建文件
    public boolean createFile(Context context, Uri rootDireUri, String mimeType, String displayName, @Nullable String content) {
        try {
            DocumentFile rootDire = DocumentFile.fromTreeUri(context, rootDireUri);
            if (rootDire != null) {
                DocumentFile newFile = rootDire.createFile(mimeType, displayName);
                if (newFile != null && newFile.exists()) {
                    if (content != null) {
                        OutputStream outputStream = context.getContentResolver().openOutputStream(newFile.getUri());
                        if (outputStream != null) {
                            outputStream.write(content.getBytes());
                            outputStream.flush();
                            outputStream.close();
                        }
                    }
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // 写入文件内容
    public boolean writeFileContent(Context context, Uri fileUri, String content) {
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri);
            if (outputStream != null) {
                outputStream.write(content.getBytes());
                outputStream.flush();
                outputStream.close();
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // 查找文件是否存在
    public boolean exists(Context context, Uri fileUri) {
        DocumentFile existsFile = DocumentFile.fromSingleUri(context, fileUri);
        if (existsFile != null) {
            return existsFile.exists();
        }
        return false;
    }

    // 删除文件
    public boolean delete(Context context, Uri fileUri) {
        DocumentFile deleteFile = DocumentFile.fromSingleUri(context, fileUri);
        if (deleteFile != null) {
            return deleteFile.delete();
        }
        return false;
    }

    // 获取文件Uri
    @Nullable
    public Uri getFileUri(Context context, Uri rootDireUri, String fileName) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, rootDireUri);
        if (documentFile != null) {
            DocumentFile file = documentFile.findFile(fileName);
            if (file != null) {
                return file.getUri();
            }
        }
        return null;
    }
}
