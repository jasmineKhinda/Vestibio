package com.amagesoftware.vestibio.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amagesoftware.vestibio.Constants;
import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.db.MYSQLiteHelperVertibio;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.DriveStatusCodes;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by jkhinda on 22/06/18.
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

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    DriveFolder baseFolder;
    protected static final int REQUEST_CODE_SIGN_IN = 0;
    protected static final int REQUEST_CODE_INTIAL_SIGN_IN = 1;
    static final String BACK_UP = "backup" ;
    //uncomment below when debugging
    //private TextView tvDriveResult;
    //private TextView tvDriveList;
    private ProgressDialog mProgress;
    private Button logout;
    private SharedPreferences prefs;

    private TextView driveAccountName;
    private TextView driveAccountEmail;
    private TextView textLastBackup;

    static final String DATABASE_NAME = "physiotherapyemetronome.db";
    //isBackup = true if backup button is pressed, false if restore button is pressed
    Boolean isBackup = true;
    CustomPropertyKey numberOfEntries =
            new CustomPropertyKey("ENTRIES", CustomPropertyKey.PUBLIC);


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_drive_activity);
        prefs =this.getSettings();

        String lastSavedBackup = prefs.getString(Constants.LAST_BACKUP, getString(R.string.nothing_backed_up));
        textLastBackup = (TextView) findViewById(R.id.text_last_backup);
        textLastBackup.setText(getString(R.string.date_backed_up) + "\n" +lastSavedBackup);

        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar);
        toolbarTop.setTitle("Backup and Restore");
        if (toolbarTop != null) {
            toolbarTop.setTitle("Backup and Restore");
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Backup and Restore");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        driveAccountName = (TextView) findViewById(R.id.textView_account_name);

        driveAccountEmail = (TextView) findViewById(R.id.textView_account_email);
        logout = (Button) findViewById(R.id.logout);


        //tvDriveResult = (TextView) findViewById(R.id.driveoutput);
        //tvDriveList = (TextView) findViewById(R.id.driveoutputList);
        Log.d("Vestibio PATH", getApplicationContext().getDatabasePath(MYSQLiteHelperVertibio.DATABASE_NAME).getAbsolutePath());
        // ...
        
        backupButton = (Button) findViewById(R.id.activity_backup_drive_button_backup);
        Button sync = (Button) findViewById(R.id.activity_backup_drive_button_sync);
        restoreButton = (Button) findViewById(R.id.activity_backup_drive_button_restore);

        mProgress = new ProgressDialog(this,R.style.AppDonateTheme);
        mProgress.setTitle(getString(R.string.processing));
        mProgress.setMessage(getString(R.string.please_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(false);


        signInAccount();



        backupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBackup=true;
                signIn(true);
            }
        });

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.show();

                Log.d("Vestibio", "name  "+ driveAccountName.getText());
               if(!(driveAccountName.getText() == getString(R.string.not_logged_in).trim())){
                   mDriveClient.requestSync().addOnSuccessListener (new OnSuccessListener<Void>()  {
                       @Override
                       public void onSuccess(Void aVoid) {
                           Log.d("Vestibio",  "Sync Successfull!");
                           mProgress.dismiss();

                           Toast.makeText(getApplicationContext(), "Sync Successfull!", Toast.LENGTH_SHORT).show();
                       }
                   })

                           .addOnFailureListener (new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Log.d("Vestibio",  "Could not update metadata buffer: " + e.getMessage());
                                   mProgress.dismiss();

                                   Toast.makeText(getApplicationContext(), "Could not sync. Please check your internet connection and try again in a minute.", Toast.LENGTH_LONG).show();
                               }
                           });
               }else{
                   Toast.makeText(getApplicationContext(), "Can't sync because not logged in.", Toast.LENGTH_LONG).show();
                   mProgress.dismiss();

               }




            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions signInOptions =
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestScopes(Drive.SCOPE_APPFOLDER)
                                .build();
                Set<Scope> requiredScopes = new HashSet<>(2);
                requiredScopes.add(Drive.SCOPE_APPFOLDER);
                GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(BackupActivity.this);
                if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
                    GoogleSignIn.getClient(BackupActivity.this, signInOptions).signOut();
                    driveAccountName.setText(getString(R.string.not_logged_in));
                    driveAccountEmail.setText("");
                    Log.d("Vestibio Sign Out", "signed out ");
                }
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBackup=false;
                signIn(false);

            }
        });
    }



    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    private void signIn(Boolean isBackup) {
        Set<Scope> requiredScopes = new HashSet<>(2);

        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            driveAccountName.setText(signInAccount.getGivenName()+ " "+ signInAccount.getFamilyName());
            driveAccountEmail.setText(signInAccount.getEmail());
            initializeDriveClient(signInAccount, isBackup);
            Log.d("Vestibio Sign In", "signIn: if ");
        } else {
            Log.d("Vestibio Sign In", "signIn: else ");
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(BackupActivity.this, signInOptions);

            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
            mProgress.show();


        }
    }

    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    private void signInAccount() {
        Set<Scope> requiredScopes = new HashSet<>(2);

        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        Log.d("Vestibio Sign In", "signIn: account "+ signInAccount);

        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {

            driveAccountName.setText(signInAccount.getGivenName()+ " "+ signInAccount.getFamilyName());
            driveAccountEmail.setText(signInAccount.getEmail());
            mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
            mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        } else {
            Log.d("Vestibio Sign In", "signIn: else ");
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();

            GoogleSignInClient  googleSignInClient = GoogleSignIn.getClient(BackupActivity.this, signInOptions);


            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_INTIAL_SIGN_IN);
            mProgress.show();

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
        Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();

        Log.d("Vestibio", "getRootFolder: " + appFolderTask);

        appFolderTask
                .addOnSuccessListener(this, new OnSuccessListener<DriveFolder>() {
                    @Override
                    public void onSuccess(DriveFolder driveFolder) {
                        ////tvDriveResult.append("Root folder found\n");
                        baseFolder = driveFolder;
                    /* Base folder is found, now check if backup file exists */
//                        mDriveClient.requestSync().addOnSuccessListener(aVoid -> checkForBackUp(isBackup))
//                                .addOnFailureListener(e -> Log.d("Vestibio",  "Could not update metadata buffer: " + e.getMessage()));
//
//                    }

                        checkForBackUp(isBackup);

                    }})
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //tvDriveResult.append("Root folder not found, error: " + e.toString() + "\n");
                        Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();
                        Log.e("Vestibio Path", "Root folder not found, error: " + e.toString() + "\n");


                    }
                });

    }

    private void checkForBackUp(Boolean isBackup) {
       /* Build a query */
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, BACK_UP))
                .build();

       /* Query contents of app folder */

        Task<MetadataBuffer> queryTask = mDriveResourceClient.listChildren(baseFolder);

        Log.d("Vestibio", "chevking " + queryTask);
       /* Check for result of query */
        queryTask
                .addOnSuccessListener(this, new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadataBuffer) {
                //Restore logic
                if(!isBackup){
                    if (metadataBuffer.getCount() == 0){
                        ////tvDriveResult.append("File " + BACK_UP + " not found\n");
                        Toast.makeText(getApplicationContext(),"No backup files found. Please backup your data first.", Toast.LENGTH_SHORT).show();
                        Log.e("Vestibio Path", "File " + BACK_UP + " not found\n");
                        //backUpDatabase();
                    } else {
                        ////tvDriveResult.append(metadataBuffer.getCount() + " Instances of file " + BACK_UP + " found\n");

                        List<String> items = new ArrayList<String>(metadataBuffer.getCount());

                        for (int i = 0; i < metadataBuffer.getCount(); i++) {

                            items.add(i,metadataBuffer.get(i).getCreatedDate().toString() + " \nEntries: "+ metadataBuffer.get(i).getCustomProperties().values() +  " Size: "+ metadataBuffer.get(i).getFileSize()/1000 + " KB" );
                        }


                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(BackupActivity.this,R.style.RestoreDialogTheme);
                        builder.setTitle("Restore Backup");
                        builder.setSingleChoiceItems(items.toArray(new String[items.size()]),-1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                // Do something with the selection
                                //Metadata metadata = metadataBuffer.get(item);
                                //restoreBackUp(metadata.getDriveId().asDriveFile());
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(BackupActivity.this,R.style.RestoreDialogTheme);
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
                                metadataBuffer.release();
                            }
                        });
                        builder.show();
                        
                    }
                //Backup logic
                }else{
                     /* Make file backup */
                    ////tvDriveResult.append(metadataBuffer.getCount() + " Instances of file " + BACK_UP + " found\n");
                    backUpDatabase();
                }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ////tvDriveResult.append("Could not search for file, error: " + e.toString() + "\n");
                        Log.e("Vestibio Path", "Could not search for file, error: " + e.toString() + "\n");
                        Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void backUpDatabase() {
        ////tvDriveResult.append("Creating Drive back-up\n");
 /* get the path of the local backup */
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);

 /* Check of dbFileExists on device */
        if (! dbFile.exists()){
            ////tvDriveResult.append("Local database not found?!\n");
            Log.e("Vestibio Path", " Db doesn't exist on device: " + DATABASE_PATH + DATABASE_NAME);
         
            Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();

            return;
        }
 /* File input stream from database to read from */
        final FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(dbFile);
        } catch (FileNotFoundException e) {
            //tvDriveResult.append("Could not get input stream from local file\n");
            Log.e("Vestibio Sign In", "Could not get input stream from local file\n");
            Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();
            return;
        }

  /* Task to make file */
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        //tvDriveResult.append("Creating a back-up of the Database File\n");

        Tasks.whenAll(createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {

            @Override
            public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
   /* Retrieved the drive contents returned by the Task */
                DriveContents contents = createContentsTask.getResult();



   /* Output stream where data will be written */
                OutputStream outputStream = contents.getOutputStream();
   /* File output stream */
                //tvDriveResult.append("Attempting to write\n");
                MYSQLiteHelperVertibio sqh = new MYSQLiteHelperVertibio(BackupActivity.this);
                byte[] buffer = new byte[4096];
                int c;

                while ((c = fileInputStream.read(buffer, 0, buffer.length)) > 0){
                    outputStream.write(buffer, 0, c);
                }
                outputStream.flush();
                outputStream.close();
                fileInputStream.close();
                //tvDriveResult.append("Database written\n");
                Log.d("Vestibio Sign In", "fileinsput stream successful");
    /* Save the file, using MetadataChangeSet */
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(BACK_UP)
                        .setMimeType("application/x-sqlite3")
                        .setStarred(false)
                        .setCustomProperty(numberOfEntries,Long.valueOf(DatabaseUtils.queryNumEntries(sqh.getReadableDatabase(),TABLE_SESSIONS)).toString())
                        .build();
                //sqh.close();
                Log.d("Vestibio Sign In", "fileinsput stream successful" + baseFolder.getDriveId()+ "  "+ baseFolder);
                return mDriveResourceClient.createFile(baseFolder, changeSet, contents);
            }
        })
    /* Task successful */
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        //tvDriveResult.append("Back up file created\n");
                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                        //DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

                        prefs.edit().putString(Constants.LAST_BACKUP,dateFormat.format(currentTime).toString()).apply();
                        textLastBackup.setText(getString(R.string.date_backed_up) + "\n" +dateFormat.format(currentTime).toString());
                        Toast.makeText(BackupActivity.this, getString(R.string.backup_success),Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //tvDriveResult.append("Could not create back up file\n");
                        Log.e("Vestibio Sign In", "Could not create back up file\n + " +e);
                        Toast.makeText(BackupActivity.this, getString(R.string.backup_failure),Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void restoreBackUp(DriveFile driveFile) {
        //tvDriveResult.append("Restoring from backup\n");
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
            //tvDriveResult.append("Could not get input stream from local file\n");
            Log.e("Vestibio Sign In", "Could not get input stream from local file\n");
            Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();
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

                //tvDriveResult.append("Attempting to restore from database\n");

                byte[] buffer = new byte[4096];
                int c;

                while ((c = inputStream.read(buffer, 0, buffer.length)) > 0){
                    fileOutputStream.write(buffer, 0, c);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
                //tvDriveResult.append("Database restored\n");
                Toast.makeText(BackupActivity.this, getString(R.string.restore_success),Toast.LENGTH_SHORT).show();

      /* Return statement needed to avoid task failure */
                Task<Void> discardTask = mDriveResourceClient.discardContents(backupContents);
                return discardTask;
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //tvDriveResult.append("Could not read file contents\n");
                        Log.e("Vestibio Sign In", "Could not read file contents"+ e.getMessage());
                        Toast.makeText(BackupActivity.this, getString(R.string.restore_failure),Toast.LENGTH_SHORT).show();
                    }
                });

    }


    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Vestibio Sign In", "requestCode " + resultCode + "  "+ requestCode);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK ) {
                /* Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                * required and is fatal. For apps where sign-in is optional,       handle appropriately
                */
                    //Check to see if GooglePlay Services is up to date
                    Integer gplayResults = GooglePlayServicesUtil.isGooglePlayServicesAvailable(BackupActivity.this);
                    if (gplayResults == ConnectionResult.SUCCESS) {
                        Log.e("Vestibio Sign In", "Google services is up to date");

                    } else {
                        Log.e("Vestibio Sign In", "Sign-in failed because google services not up to date");
                        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(gplayResults, BackupActivity.this, 0);
                        if (dialog != null) {
                            //This dialog will help the user update to the latest GooglePlayServices
                            dialog.show();

                        }
                    }
                    Log.e("Vestibio Sign In", "Sign-in failed via a button press");
                    Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();

                    return;
                }

                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    driveAccountName.setText( GoogleSignIn.getSignedInAccountFromIntent(data).getResult().getGivenName()+ " "+  GoogleSignIn.getSignedInAccountFromIntent(data).getResult().getFamilyName());
                    driveAccountEmail.setText( GoogleSignIn.getSignedInAccountFromIntent(data).getResult().getEmail());
                    initializeDriveClient(getAccountTask.getResult(),isBackup);
                    mProgress.dismiss();

                } else {
                    mProgress.dismiss();
                    Log.e("Vestibio Sign In", "Sign-in failed, account task was unsuccessful");
                    Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();
                    //tvDriveResult.append("Sign-in failed\n");
                }
                break;
            case REQUEST_CODE_INTIAL_SIGN_IN:
                if (resultCode != RESULT_OK ) {
                /* Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                * required and is fatal. For apps where sign-in is optional,       handle appropriately
                */

                //Check to see if GooglePlay Services is up to date
                    Integer gplayResults = GooglePlayServicesUtil.isGooglePlayServicesAvailable(BackupActivity.this);
                    if (gplayResults == ConnectionResult.SUCCESS) {
                        Log.e("Vestibio Sign In", "Google services is up to date");
                    } else {
                        Log.e("Vestibio Sign In", "Sign-in failed because google services not up to date");
                        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(gplayResults, BackupActivity.this, 0);
                        if (dialog != null) {
                            //This dialog will help the user update to the latest GooglePlayServices
                            dialog.show();

                        }
                    }
                    Log.e("Vestibio Sign In", "Sign-in failed via automatic sign in");
                    return;
                }

                Task<GoogleSignInAccount> getTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getTask.isSuccessful()) {
                    mProgress.dismiss();
                    driveAccountName.setText( GoogleSignIn.getSignedInAccountFromIntent(data).getResult().getGivenName()+ " "+  GoogleSignIn.getSignedInAccountFromIntent(data).getResult().getFamilyName());
                    driveAccountEmail.setText( GoogleSignIn.getSignedInAccountFromIntent(data).getResult().getEmail());
                    mDriveClient = Drive.getDriveClient(getApplicationContext(), getTask.getResult());
                    mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), getTask.getResult());

                }else{
                    //tvDriveResult.append("Sign-in failed\n");
                    mProgress.dismiss();
                    Log.e("Vestibio Sign In", "Sign-in failed via auto sign in, account task was unsuccessful");
                    Toast.makeText(BackupActivity.this, getString(R.string.action_failure),Toast.LENGTH_SHORT).show();
                }
                break;
                default:
                    Log.e("Vestibio Sign In", "Sign-in failed via a button press");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public SharedPreferences getSettings() {
        return getSharedPreferences(Constants.PREFS_BACKUP, Context.MODE_PRIVATE);
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

    @Override
    public void onResume(){
        super.onResume();
        if (mProgress.isShowing()){
            mProgress.cancel();
        }
        // put your code here...]


        Log.d("Vestibio", "tick onResume: ");
    }
    @Override
    public void onPause(){
        super.onPause();
        // put your code here...]
        // //mProgress.dismiss();
        Log.d("Vestibio", "tick onPause: ");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Vestibio", "tick onStop: ");

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("Vestibio", "tick onRestart: ");

    }


}
