package net.skink.savingfiles;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void onClickTestInternal(View view) {
        Log.d("SavingFiles", "onClickTestInternal() called.");

        // To create a new file in one of these directories you can use the File() constructor,
        // passing the File provided by one of the above methods that specifies your internal
        // storage directory.  For example
        Context context = view.getContext();
        // getFilesDir() returns a File representing an internal directory for your app.
        String filename = "myfile";
        File file = new File(context.getFilesDir(), filename);
        String strAbsolutePath = file.getAbsolutePath();

        // Show path
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.append("\n ABS Path = ");
        editText.append(strAbsolutePath);


        // Try to write to it.
        PrintStream p = null;
        try {
            // The FileOutputStream is set to false so its not appended.
            p = new PrintStream(new BufferedOutputStream(new FileOutputStream(file,false)));
            p.println(strAbsolutePath.toString());
            // No need to flush
            //p.flush();

        } catch (FileNotFoundException e) {
            // if FileOutputStream(file) fails
            e.printStackTrace();
        } finally {
            p.close();
        }

        // Try to write to it.
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            // We know how much we wrote, so we can set the buffer length to that
            //Integer iLen = strAbsolutePath.length();
            // Or simply set it to 1024 to see if we wrote more than we thought.
            Integer iLen = 1024;

            byte[] bufferIn = new byte[iLen];
            //in.read(bufferIn, 0, bufferIn.length);

            int bytesRead = 0;
            String strFileContents = "";
            while (-1 != (bytesRead = in.read(bufferIn))) {
                strFileContents += new String(bufferIn,0,bytesRead);
            }
            editText.append("\n We Read = ");
            editText.append(strFileContents);

        } catch (FileNotFoundException e) {
            // if FileInputStream(file) fails
            Log.d("SavingFiles", "onClickTestInternal() FileNotFound Exception");
            e.printStackTrace();
        } catch (IOException e) {
            // if in.read fails
            Log.d("SavingFiles", "onClickTestInternal() IO Exception");
            e.printStackTrace();
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                Log.d("SavingFiles", "onClickTestInternal() close IO Exception");
                e.printStackTrace();
            }
        }

        // Delete file since its just a test
        context.deleteFile(filename);
    }

    /* Check to see if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Check to see if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    // See manifest.xml for the uses external storage permission
    // if you have write access you get read access as well.
    public void onClickTestExternal(View view) {
        Log.d("SavingFiles", "onClickTestExternal() called.");

        /* If you want to save public files on external storage, use
        the getExternalStoragePublicDirectory() method to get a File
        representing the appropriate directory on the external storage.
        The method takes an argument specifying the type of file you want
        to save so that they can be logically organized with other public
        files, such as DIRECTORY_MUSIC or DIRECTORY_PICTURES.  For example:
         */

        // Get the directory for the user's public DOWNLOAD directory.
        File extStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"TESTY");
        if (!extStorage.mkdirs()) {
            // If the directory already exists, the mkdirs command above will fail.
            Log.e("savingFiles", "Directory not created");
        }

        if (null != extStorage) {
            // Write a test file.
            File outFile = new File(extStorage, "aTestFile");
            try {
                // Dont append, truncate and write new.
                FileWriter writer = new FileWriter(outFile,false);
                BufferedWriter out = new BufferedWriter(writer);
                out.append("A line of text to write to the file.\n");
                out.close();
            } catch (IOException e) {
                // If we can not creat the filewriter
                e.printStackTrace();
            }
        }

        // At this point, you can use a shell to verify the file is written and exists.
        // click on Terminal in Android Studio
        // cd to c:/users/davis and run the setenv.bat script to set path to the adk tools
        // Then use adb shell to get a shell on the emulator or nexus. then navigate to
        // the directory and then cat the file.
        // c:\Users\john>setenv.bat
        // c:\Users\john>adb shell
        // root@generic_x86:/ # cd /mnt/sdcard/download/TESTY
        // root@generic_x86:/mnt/sdcard/download/TESTY # cat aTestFile
        // It should print: A line of text to write to the file.

        if (null != extStorage) {
            // Write a test file.
            File inFile = new File(extStorage, "aTestFile");
            FileReader reader = null;
            try {
                reader = new FileReader(inFile);
            } catch (FileNotFoundException e) {
                // FileReader fails
                e.printStackTrace();
            }
            BufferedReader in = new BufferedReader(reader);
            String strRead = "";
            try {
                strRead = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Set Text
            EditText editText = (EditText) findViewById(R.id.editText);
            editText.setText("We read from file: " + strRead);

            // Delete the file since its just a demo
            inFile.delete();
        }


    }


}
