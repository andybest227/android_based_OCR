package com.project.androidocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Home extends AppCompatActivity {
    private static final int MY_REQUEST_CODE_PERMISSION =1000;
    private static final int MY_MY_REQUEST_CODE_FILECHOOSER = 2000;
    private static final int STORAGE_REQUEST_CODE = 400;
    String[] storagePermission;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView newDoc = findViewById(R.id.new_document);
        TextView openDoc = findViewById(R.id.open_document);
        TextView logout = findViewById(R.id.logout_document);
        ListView listView = findViewById(R.id.recent_doc);

        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

            //set onClick Lister to create new document
        newDoc.setOnClickListener(view -> {
            Intent new_document = new Intent(Home.this, MainActivity.class);
            new_document.putExtra("file_link", "");
            startActivity(new_document);
        });
        populateDocuments(listView);
        openDoc.setOnClickListener(view -> openDoc());
        logout.setOnClickListener(view -> logOut());
    }
    //open existing document
    private void openDoc(){

        askPermissionAndBrowseFile();
    }
    private void askPermissionAndBrowseFile(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int permission = ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED){
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE_PERMISSION);
                return;
            }
        }
        this.doBrowseFile();
    }
    private void doBrowseFile(){
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("text/plain");
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose File");
        startActivityForResult(chooseFileIntent, MY_MY_REQUEST_CODE_FILECHOOSER);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if (requestCode == MY_REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Log.i(LOG_TAG, "Permission Granted");
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                //this.doBrowseFile();
            } else {
                //Log.i(LOG_TAG, "Permission denied");
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_MY_REQUEST_CODE_FILECHOOSER) {
            //if (requestCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri fileUri = data.getData();
                    String fileName = fileUri.getLastPathSegment().substring(fileUri.getLastPathSegment().lastIndexOf('/'));

                    Intent homepage = new Intent(this, MainActivity.class);
                    homepage.putExtra("file_link", fileName);
                    startActivity(homepage);
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void populateDocuments(ListView listView){
        //ask permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int permission = ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, storagePermission, MY_REQUEST_CODE_PERMISSION);
                return;
            }
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            File f = new File(path);
            File[] file = f.listFiles((file1, s) -> s.endsWith(".txt"));
            assert file != null;
            if (file.length>0){
                String[] fileName = new String[file.length];
                for (int i = 0; i< file.length; i++){
                    fileName[i] = file[i].getName();
                    List<String> recent_doc = new ArrayList<>(Arrays.asList(fileName));
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recent_doc);
                    listView.setAdapter(arrayAdapter);

                    //open a document from recent documents
                    listView.setOnItemClickListener((adapterView, view, position, l) -> {
                        String selectedItem = (String) adapterView.getItemAtPosition(position);
                        Intent homepage = new Intent(Home.this, MainActivity.class);
                        homepage.putExtra("file_link", selectedItem);
                        startActivity(homepage);
                    });

                }
            }
        }

    }
    //logout method
    private void logOut(){
        //finish();
        Intent intent = new Intent(Home.this, LoginActivity.class);
        startActivity(intent);
    }
}