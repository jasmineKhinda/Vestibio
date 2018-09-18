package com.amagesoftware.vestibio.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.db.MYSQLiteHelperVertibio;
import com.amagesoftware.vestibio.tools.Backup;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInAccountCreator;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.BaseGmsClient;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by jasmine on 14/09/18.
 */

public class BackupActivity extends BaseActivity  {


    private Button backupButton;
    private Button restoreButton;


    private static final String PACKAGE_NAME = "com.amagesoftware.vestibio";

    private static final String DATABASE_PATH =
            "/data/data/" + PACKAGE_NAME + "/databases/";

    private static final String FILE_NAME = MYSQLiteHelperVertibio.DATABASE_NAME;
    private static final String MIME_TYPE = "application/x-sqlite-3";
    public static final String TABLE_SESSIONS = "sessions";
    private GoogleSignInClient mGoogleSignInClient;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    DriveFolder baseFolder;
    protected static final int REQUEST_CODE_SIGN_IN = 0;
    static final String BACK_UP = "backup" ;
    private TextView tvDriveResult;
    private TextView tvDriveList;
    static final String DATABASE_NAME = "physiotherapyemetronome.db";
    Boolean isBackup = true;
    CustomPropertyKey numberOfEntries =
            new CustomPropertyKey("ENTRIES", CustomPropertyKey.PUBLIC);


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_drive_activity);

        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar);
        toolbarTop.setTitle("Backup and Restore");
        if (toolbarTop != null) {
            toolbarTop.setTitle("Backup and Restore");
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Backup and Restore");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        tvDriveResult = (TextView) findViewById(R.id.driveoutput);
        tvDriveList = (TextView) findViewById(R.id.driveoutputList);
        Log.d("Vestibio PATH", getApplicationContext().getDatabasePath(MYSQLiteHelperVertibio.DATABASE_NAME).getAbsolutePath());
        // ...
        
        backupButton = (Button) findViewById(R.id.activity_backup_drive_button_backup);
        restoreButton = (Button) findViewById(R.id.activity_backup_drive_button_restore);



        backupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBackup=true;
                singIn(true);
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openFilePicker();
                isBackup=false;
                singIn(false);

            }
        });
    }



    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    private void singIn(Boolean isBackup) {
        Set<Scope> requiredScopes = new HashSet<>(2);

        requiredScopes.add(Drive.SCOPE_FILE);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount, isBackup);
        } else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount ,Boolean isBackup) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady(isBackup);
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    private void onDriveClientReady(Boolean isBackup){
/*   Initialise the root folder. */
/* Since the tasks are executed in a separate execution threads, the remaining tasks are called from within each other */
        getRootFolder(isBackup);
    }
    private void getRootFolder(Boolean isBackup) {
     /* Get the app folder */
        Task<DriveFolder> appFolderTask = mDriveResourceClient.getRootFolder();

        appFolderTask
                .addOnSuccessListener(this, new OnSuccessListener<DriveFolder>() {
                    @Override
                    public void onSuccess(DriveFolder driveFolder) {
                        tvDriveResult.append("Root folder found\n");
                        baseFolder = driveFolder;
     /* Base folder is found, now check if backup file exists */

                        checkForBackUp(isBackup);



            /* Use this to delete files. Remember to comment out the line able it */
            // listFilesInBaseFolder();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tvDriveResult.append("Root folder not found, error: " + e.toString() + "\n");
                    }
                });

    }

    private void checkForBackUp(Boolean isBackup) {
       /* Build a query */
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, BACK_UP))
                .build();

       /* Query contents of app folder */
        Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(baseFolder, query);

       /* Check for result of query */
        queryTask
                .addOnSuccessListener(this, new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadataBuffer) {
                //Restore logic
                if(!isBackup){
                    if (metadataBuffer.getCount() == 0){
                        tvDriveResult.append("File " + BACK_UP + " not found\n");
                        Toast.makeText(getApplicationContext(),"No backup files found. Please backup your data first.", Toast.LENGTH_SHORT);
                        //backUpDatabase();
                    } else {
                        tvDriveResult.append(metadataBuffer.getCount() + " Instances of file " + BACK_UP + " found\n");

                        List<String> items = new ArrayList<String>(metadataBuffer.getCount());

                        for (int i = 0; i < metadataBuffer.getCount(); i++) {

                            items.add(i,metadataBuffer.get(i).getCreatedDate().toString() + " \nEntries: "+ metadataBuffer.get(i).getCustomProperties().values() +  " Size: "+ metadataBuffer.get(i).getFileSize()/1000 + " KB" );
                        }


                        AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this,R.style.RestoreDialogTheme);
                        builder.setTitle("Restore Backup");
                        builder.setSingleChoiceItems(items.toArray(new String[items.size()]),-1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                // Do something with the selection
                                //Metadata metadata = metadataBuffer.get(item);
                                //restoreBackUp(metadata.getDriveId().asDriveFile());
                                AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this,R.style.RestoreDialogTheme);
                                builder.setTitle("Restore Backup?");
                                builder.setMessage("The backup file will replace your current data. What do you want to do?");
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialogNested, int WhichButton)  {
                                        dialogNested.dismiss();
                                    }
                                });
                                builder.setPositiveButton("Restore", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialogNested, int WhichButton)  {
                                        Metadata metadata = metadataBuffer.get(item);
                                        restoreBackUp(metadata.getDriveId().asDriveFile());
                                        dialogNested.dismiss();
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();




                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int WhichButton)  {
                                dialog.dismiss();
                            }
                        });
                        builder.show();





//                        backUpDatabase();
//                        for(int i=0; i < metadataBuffer.getCount() ; i++){
//                            tvDriveResult.append(metadataBuffer.get(i).getFileSize()+ "/n  ");
//                        }
                    }
                    //Backup logic
                }else{
                     /* Make file backup */
                    tvDriveResult.append(metadataBuffer.getCount() + " Instances of file " + BACK_UP + " found\n");
                    backUpDatabase();

                }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tvDriveResult.append("Could not search for file, error: " + e.toString() + "\n");
                    }
                });

    }
    private void backUpDatabase() {
        tvDriveResult.append("Creating Drive back-up\n");
 /* get the path of the local backup */
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);

 /* Check of dbFileExists on device */
        if (! dbFile.exists()){
            tvDriveResult.append("Local database not found?!\n");
            Log.d("Vestibio Path2", DATABASE_PATH + DATABASE_NAME);

            return;
        }
 /* File input stream from database to read from */
        final FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(dbFile);
        } catch (FileNotFoundException e) {
            tvDriveResult.append("Could not get input stream from local file\n");
            return;
        }

  /* Task to make file */
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        tvDriveResult.append("Creating a back-up of the Database File\n");

        Tasks.whenAll(createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {

            @Override
            public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
   /* Retrieved the drive contents returned by the Task */
                DriveContents contents = createContentsTask.getResult();



   /* Output stream where data will be written */
                OutputStream outputStream = contents.getOutputStream();
   /* File output stream */
                tvDriveResult.append("Attempting to write\n");
                MYSQLiteHelperVertibio sqh = new MYSQLiteHelperVertibio(BackupActivity.this);
                byte[] buffer = new byte[4096];
                int c;

                while ((c = fileInputStream.read(buffer, 0, buffer.length)) > 0){
                    outputStream.write(buffer, 0, c);
                }
                outputStream.flush();
                outputStream.close();
                fileInputStream.close();
                tvDriveResult.append("Database written\n");

    /* Save the file, using MetadataChangeSet */
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(BACK_UP)
                        .setMimeType("application/x-sqlite3")
                        .setStarred(false)
                        .setCustomProperty(numberOfEntries,Long.valueOf(DatabaseUtils.queryNumEntries(sqh.getReadableDatabase(),TABLE_SESSIONS)).toString())
                        .build();

                return mDriveResourceClient.createFile(baseFolder, changeSet, contents);
            }
        })
    /* Task successful */
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        tvDriveResult.append("Back up file created\n");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tvDriveResult.append("Could not create back up file\n");
                    }
                });

    }

    private void restoreBackUp(DriveFile driveFile) {
        tvDriveResult.append("Restoring from backup\n");
    /*  Get the path of the local backup */
        File dbFileOld = new File(DATABASE_PATH + DATABASE_NAME);

    /* Check of dbFileExists on device, delete if it does because it needs to be completely over written */
        if (dbFileOld.exists()){
            dbFileOld.delete();
        }

        File dbFileNew = new File(DATABASE_PATH + DATABASE_NAME);

    /* File input stream from database to read from */
        final FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(dbFileNew);
        } catch (FileNotFoundException e) {
            tvDriveResult.append("Could not get input stream from local file\n");
            return;
        }
    /* Task to open file */
        Task<DriveContents> openFileTask =
                mDriveResourceClient.openFile(driveFile, DriveFile.MODE_READ_ONLY);

    /* Continue with task */
        openFileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>(){

            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents backupContents = task.getResult();
                InputStream inputStream = backupContents.getInputStream();

                tvDriveResult.append("Attempting to restore from database\n");

                byte[] buffer = new byte[4096];
                int c;

                while ((c = inputStream.read(buffer, 0, buffer.length)) > 0){
                    fileOutputStream.write(buffer, 0, c);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
                tvDriveResult.append("Database restored\n");

      /* Return statement needed to avoid task failure */
                Task<Void> discardTask = mDriveResourceClient.discardContents(backupContents);
                return discardTask;
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tvDriveResult.append("Could not read file contents\n");
                    }
                });

    }


    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                /* Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                * required and is fatal. For apps where sign-in is optional,       handle appropriately
                */
                    Log.e("Vestibio", "Sign-in failed.");
                    return;
                }

                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult(),isBackup);
                    Log.e("VESTIBIO", "is Backup ? "+  isBackup);
                } else {
                    Log.e("Vestibio", "Sign-in failed2.");
                    tvDriveResult.append("Sign-in failed\n");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    }
