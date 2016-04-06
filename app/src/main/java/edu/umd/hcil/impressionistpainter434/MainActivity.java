package edu.umd.hcil.impressionistpainter434;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ActionMenuView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener, View.OnTouchListener {

    private static int RESULT_LOAD_IMAGE = 1;
    private  ImpressionistView _impressionistView;
    private Menu _toolMenu;

    // These images are downloaded and added to the Android Gallery when the 'Download Images' button is clicked.
    // This was super useful on the emulator where there are no images by default
    private static String[] IMAGE_URLS ={
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/BoliviaBird_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/BolivianDoor_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/MinnesotaFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PeruHike_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/ReginaSquirrel_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreDog_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreStreet_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreStreet_PhotoByJonFroehlich2(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreWine_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/WashingtonStateFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/JonILikeThisShirt_Medium.JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/JonUW_(853x1280).jpg",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/MattMThermography_Medium.jpg",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PinkFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PinkFlower2_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PurpleFlowerPlusButterfly_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/WhiteFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/YellowFlower_PhotoByJonFroehlich(Medium).JPG",
    };

    static final String _appDirectoryName = "Impressionist";
    static final File _imageRoot = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), _appDirectoryName);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _impressionistView = (ImpressionistView)findViewById(R.id.viewImpressionist);
        PreviewImageView imageView = (PreviewImageView)findViewById(R.id.viewImage);
        _impressionistView.setImageView(imageView);
        _impressionistView.setOnTouchListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!_imageRoot.exists()) _imageRoot.mkdirs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        _toolMenu = menu;

        menu.getItem(0).setIcon(R.drawable.ic_menu_undo_gray);
        menu.getItem(0).setEnabled(false);
        menu.getItem(1).setIcon(R.drawable.ic_menu_redo_gray);
        menu.getItem(1).setEnabled(false);

        return true;
    }

    public void onMenuClickUndo(MenuItem mi) {
        if (_impressionistView.undo()) {
            _toolMenu.getItem(0).setIcon(R.drawable.ic_menu_undo_gray);
            _toolMenu.getItem(0).setEnabled(false);
        }
        _toolMenu.getItem(1).setIcon(R.drawable.ic_menu_redo);
        _toolMenu.getItem(1).setEnabled(true);
    }

    public void onMenuClickRedo(MenuItem mi) {
        if (_impressionistView.redo()) {
            _toolMenu.getItem(1).setIcon(R.drawable.ic_menu_redo_gray);
            _toolMenu.getItem(1).setEnabled(false);
        }
        _toolMenu.getItem(0).setIcon(R.drawable.ic_menu_undo);
        _toolMenu.getItem(0).setEnabled(true);
    }

    public void onButtonClickClear(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Clear Painting?")
                .setMessage("Do you really want to clear your painting?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "Painting cleared", Toast.LENGTH_SHORT).show();
                        _impressionistView.clearPainting();
                        _toolMenu.getItem(0).setIcon(R.drawable.ic_menu_undo_gray);
                        _toolMenu.getItem(0).setEnabled(false);
                        _toolMenu.getItem(1).setIcon(R.drawable.ic_menu_redo_gray);
                        _toolMenu.getItem(1).setEnabled(false);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void onButtonClickSetBrush(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuCircle:
                Toast.makeText(this, "Circle Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Circle);
                return true;
            case R.id.menuSquare:
                Toast.makeText(this, "Square Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Square);
                return true;
            case R.id.menuSpeedCircle:
                Toast.makeText(this, "Speed Circle Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.SpeedCircle);
                return true;
            case R.id.menuSpeedSquare:
                Toast.makeText(this, "Speed Square Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.SpeedSquare);
                return true;
            case R.id.menuSpin:
                Toast.makeText(this, "Spin Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Spin);
                return true;
            case R.id.menuSpeedSpin:
                Toast.makeText(this, "Speed Spin Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.SpeedSpin);
                return true;
        }
        return false;
    }


    /**
     * Downloads test images to use in the assignment. Feel free to use any images you want. I only made this
     * as an easy way to get images onto the emulator.
     *
     * @param v
     */
    public void onButtonClickDownloadImages(View v){

        // Without this call, the app was crashing in the onActivityResult method when trying to read from file system
        FileUtils.verifyStoragePermissions(this);

        // Amazing Stackoverflow post on downloading images: http://stackoverflow.com/questions/15549421/how-to-download-and-save-an-image-in-android
        final BasicImageDownloader imageDownloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {

            @Override
            public void onError(String imageUrl, BasicImageDownloader.ImageError error) {
                Log.v("BasicImageDownloader", "onError: " + error);
            }

            @Override
            public void onProgressChange(String imageUrl, int percent) {
                Log.v("BasicImageDownloader", "onProgressChange: " + percent);
            }

            @Override
            public void onComplete(String imageUrl, Bitmap downloadedBitmap) {
                File externalStorageDirFile = Environment.getExternalStorageDirectory();
                String externalStorageDirStr = Environment.getExternalStorageDirectory().getAbsolutePath();
                boolean checkStorage = FileUtils.checkPermissionToWriteToExternalStorage(MainActivity.this);
                String guessedFilename = URLUtil.guessFileName(imageUrl, null, null);

                // See: http://developer.android.com/training/basics/data-storage/files.html
                // Get the directory for the user's public pictures directory.
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), guessedFilename);
                try {
                    boolean compressSucceeded = downloadedBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                    FileUtils.addImageToGallery(file.getAbsolutePath(), getApplicationContext());
                    Toast.makeText(getApplicationContext(), "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        for(String url: IMAGE_URLS){
            imageDownloader.download(url, true);
        }
    }

    /**
     * Loads an image from the Gallery into the ImageView
     *
     * @param v
     */
    public void onButtonClickLoadImage(View v){

        // Without this call, the app was crashing in the onActivityResult method when trying to read from file system
        FileUtils.verifyStoragePermissions(this);

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    /**
     * Called automatically when an image has been selected in the Gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ImageView imageView = (ImageView) findViewById(R.id.viewImage);

                // destroy the drawing cache to ensure that when a new image is loaded, its cached
                imageView.destroyDrawingCache();
                imageView.setImageBitmap(bitmap);
                imageView.setDrawingCacheEnabled(true);

                _impressionistView.setPicture(bitmap);

                _toolMenu.getItem(0).setIcon(R.drawable.ic_menu_undo_gray);
                _toolMenu.getItem(0).setEnabled(false);
                _toolMenu.getItem(1).setIcon(R.drawable.ic_menu_redo_gray);
                _toolMenu.getItem(1).setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void onButtonClickSave(View v) {
        if(_impressionistView._picture == null) {
            Toast.makeText(this, "Nothing to save!", Toast.LENGTH_LONG).show();
            return;
        }

        // Timestamps make for easy unique file names
        String fname = "drawing_" + (new SimpleDateFormat("yy_MM_dd_hh_mm_ss").format(new Date())) + ".jpeg";
        File image = new File(_imageRoot, fname);
        try {
            FileOutputStream fos = new FileOutputStream(image);
            // Get the Bitmap of the DoodleView, and put it into a .jpeg at the file address
            _impressionistView._offScreenBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();

            // Make sure the system knows that the new file exists
            MediaScannerConnection.scanFile(this, new String[]{image.toString()}, null, null);

            Toast.makeText(this, "Artwork saved!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Doesn't matter what failed, the end result is that the image can't be shared
            Toast.makeText(this, "Error saving drawing", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == _impressionistView.getId() && _impressionistView._picture != null) {
            // As soon as the user draws a line, there is something to undo and nothing to redo
            _toolMenu.getItem(0).setIcon(R.drawable.ic_menu_undo);
            _toolMenu.getItem(0).setEnabled(true);
            _toolMenu.getItem(1).setIcon(R.drawable.ic_menu_redo_gray);
            _toolMenu.getItem(1).setEnabled(false);
        }

        return false;
    }

}
