package com.android_document_tree;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

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

    public boolean createFile(Context context, Uri rootDireUri, String mimeType, String displayName, @Nullable String content) {
        try {
            DocumentFile rootDire = DocumentFile.fromTreeUri(context, rootDireUri);
            if (rootDire != null) {
                Log.d(TAG, "createFile: " + rootDire.getUri());
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

    public boolean writeFileContent(Context context, Uri fileUri, String content) {
        try {
            DocumentFile writeFile = DocumentFile.fromSingleUri(context, fileUri);
            if (writeFile != null) {
                Log.d(TAG, "writeFileContent: " + writeFile.getUri());
                OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri);
                if (outputStream != null) {
                    outputStream.write(content.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean exists(Context context, Uri fileUri) {
        DocumentFile existsFile = DocumentFile.fromSingleUri(context, fileUri);
        if (existsFile != null) {
            Log.d(TAG, "exists: " + existsFile.getUri());
            return existsFile.exists();
        }
        return false;
    }

    public boolean delete(Context context, Uri fileUri) {
        DocumentFile deleteFile = DocumentFile.fromSingleUri(context, fileUri);
        if (deleteFile != null) {
            Log.d(TAG, "delete: " + deleteFile.getUri());
            return deleteFile.delete();
        }
        return false;
    }

    @Nullable
    public Uri getFileUri(Context context, Uri rootDireUri, String fileName) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, rootDireUri);
        if (documentFile != null) {
            Log.d(TAG, "getFileUri: " + documentFile.getUri());
            DocumentFile file = documentFile.findFile(fileName);
            if (file != null) {
                return file.getUri();
            }
        }
        return null;
    }
}
