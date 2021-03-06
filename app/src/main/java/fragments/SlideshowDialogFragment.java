package fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import co.realinventor.statusmanager.GlideApp;
import co.realinventor.statusmanager.R;
import co.realinventor.statusmanager.ViewActivity;
import helpers.AdViewInfo;
import helpers.Favourites;
import helpers.Image;
import helpers.MediaFiles;
import helpers.ViewPagerFixed;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();
    private ArrayList<Object> allObjects;
    private ArrayList<Image> images = new ArrayList<>();
    private ViewPagerFixed viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount, lblTitle, lblDate;
    private LinearLayout detailsLinearLayout;
    private int selectedPosition = 0;
    private ImageButton imageDownload,imageShare,imageLove,imageDelete,imageUnlove;
    private String page_title = "unknown";
    private ViewGroup cont;
    private AdView mAdView;
    private AdRequest adRequest;
    private boolean noAdsUnlocked;
    private final String AD_UNIT_ID_BANNER = "ca-app-pub-4525583199746587/8182378362";
    private final String TEST_AD_UNIT_ID_BANNER = "ca-app-pub-3940256099942544/6300978111";

    static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_slideshow_dialog, container, false);
        cont = container;

        SharedPreferences sharedPref = getActivity().getSharedPreferences("APP_DEFAULTS", Context.MODE_PRIVATE);
        noAdsUnlocked = sharedPref.getBoolean("NoAdsUnlocked", false);

        if(!noAdsUnlocked) {
            mAdView = v.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        viewPager = (ViewPagerFixed) v.findViewById(R.id.viewpagerss);
        viewPager.setOffscreenPageLimit(0);
        lblCount = (TextView) v.findViewById(R.id.lbl_count);
        lblTitle = (TextView) v.findViewById(R.id.titles);
        lblDate = (TextView) v.findViewById(R.id.date);
        detailsLinearLayout = (LinearLayout) v.findViewById(R.id.detailsLinearLayout);

        imageDownload = (ImageButton)v.findViewById(R.id.imageDownloadButton);
        imageShare = (ImageButton)v.findViewById(R.id.imageShareButton);
        imageLove =(ImageButton)v.findViewById(R.id.imageLoveButton);
        imageDelete =(ImageButton)v.findViewById(R.id.imageDeleteButton);
        imageUnlove =(ImageButton)v.findViewById(R.id.imageUnloveButton);

        allObjects = (ArrayList<Object>) getArguments().getSerializable("images");

        for(Object obj: allObjects){
            if(obj instanceof Image){
                images.add((Image)obj);
            }
        }

        selectedPosition = getArguments().getInt("position");
        setListeners(getArguments().getInt("position"));
        Log.e(TAG, "position: " + selectedPosition);
        Log.e(TAG, "images size: " + images.size());

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    //  page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
            setListeners(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {}

        @Override
        public void onPageScrollStateChanged(int arg0) {}

    };

    private void setListeners(final int position){
        final int selectedPosition = position;
        imageDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filepath = images.get(selectedPosition).getLarge();
                Log.d("File to be downloaded",filepath);
                MediaFiles.copyToDownload(filepath);
                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.file_saved),Toast.LENGTH_SHORT).show();
                Animation anims = AnimationUtils.loadAnimation(getContext(), R.anim.blink);
                imageDownload.startAnimation(anims);
                Log.d("ImageButton", "Pressed");

                //Notifies MediaScanner about this new file
                File file = new File(filepath.replace(MediaFiles.WHATSAPP_STATUS_FOLDER_PATH,MediaFiles.DOWNLOADED_IMAGE_PATH));
                Log.d("MediaScanner ",file.getPath());
                Intent intent =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                getActivity().sendBroadcast(intent);
            }
        });

        imageLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                builder.setMessage(getActivity().getResources().getString(R.string.add_to_fav))
                        .setPositiveButton(getActivity().getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.added_to_fav),Toast.LENGTH_SHORT).show();
                                imageLove.setBackgroundResource(R.drawable.ic_action_love_red);
                                Animation anims = AnimationUtils.loadAnimation(getContext(), R.anim.blink);
                                imageLove.startAnimation(anims);
                                anims.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {}

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        imageLove.setBackgroundResource(R.drawable.ic_action_love);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {}

                                });
                                Log.d("ImageButton", "Pressed");
                                addFavs(images.get(selectedPosition).getLarge());
                            }
                        })
                        .setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.create();
                builder.show();
            }
        });

        imageShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.prepare_for_share), Toast.LENGTH_SHORT).show();
                String filepath = images.get(selectedPosition).getLarge();
                //Log.d("Intent share file", filepath);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filepath));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, "Choose an app to share"));
            }
        });

        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                builder.setMessage(getActivity().getResources().getString(R.string.are_u_sure_del))
                        .setTitle(getActivity().getResources().getString(R.string.alert))
                        .setPositiveButton(getActivity().getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                imageDelete.setBackgroundResource(R.drawable.ic_action_delete_red);

                                File file = new File(images.get(position).getLarge());
                                Log.d("File path",file.getPath());
                                boolean deleted = file.delete();
//                setCurrentItem(position+1);
                                if(deleted){
                                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.deleted_file), Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.could_not_delete), Toast.LENGTH_SHORT).show();
                                }
                                Intent intent = new Intent(getActivity(), ViewActivity.class);
                                intent.putExtra("title","downloads");
                                getActivity().finish();
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.create();
                builder.show();
            }
        });

        if(images.get(selectedPosition).isVideo()){
            detailsLinearLayout.setVisibility(View.INVISIBLE);
            mAdView.setVisibility(View.INVISIBLE);
            mAdView.destroy();
            AdViewInfo.isSlideShowBannerAlive = false;
        }
        else{
            //Checks if adview is already loaded?
            if(!AdViewInfo.isSlideShowBannerAlive && !noAdsUnlocked)
                mAdView.loadAd(adRequest);
            mAdView.setVisibility(View.VISIBLE);
        }

        try{
            page_title = getArguments().getString("title");
            if(page_title.equals("downloads")){  //Download page
                //Remove Details LinearLayout
                detailsLinearLayout.setVisibility(View.INVISIBLE);

                //Removes ImageDownloadButton from Layout
                imageDownload.setVisibility(View.GONE);

                //Change ShareImageButton property to align_parent_right
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageShare.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageShare.setLayoutParams(params);

                //Make delete, fav button visible
                imageDelete.setVisibility(View.VISIBLE);
                imageLove.setVisibility(View.VISIBLE);
            }
            if(page_title.equals("favs")){   //The selected tab is favs
                //Remove Details LinearLayout, and other views
                detailsLinearLayout.setVisibility(View.INVISIBLE);
                imageDownload.setVisibility(View.GONE);
                imageDelete.setVisibility(View.GONE);
                imageShare.setVisibility(View.GONE);
                imageLove.setVisibility(View.GONE);
                imageUnlove.setVisibility(View.VISIBLE);
                imageUnlove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //write some code to delete file name entry from file
                        Log.d("Unlove button",  "Clicked");

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                        builder.setMessage(getActivity().getResources().getString(R.string.remove_from_fav))
                                .setPositiveButton(getActivity().getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // FIRE ZE MISSILES!
                                        String file_name_to_be_removed = images.get(position).getLarge();
                                        file_name_to_be_removed = file_name_to_be_removed.replace(MediaFiles.DOWNLOADED_IMAGE_PATH,"");
                                        int index = getLineIndex(file_name_to_be_removed);
                                        if(index != -1){
                                            File file = new File(getActivity().getFilesDir()+"/"+Favourites.FAV_FILENAME);
                                            try {
                                                Log.e("Try catch", "Enters here");
                                                removeLine(file, index);
                                                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.removed_from_fav),Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(), ViewActivity.class);
                                                intent.putExtra("title","favs");
                                                getActivity().finish();
                                                startActivity(intent);
                                            }
                                            catch (IOException e){
                                                Log.e("IOException at Unlove", "could not remove line");
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });
                        builder.create();
                        builder.show();
                    }
                });
            }
        }
        catch (Exception e){
            Log.d("Default tabs","Neglected");
        }
    }

    private void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " of " + images.size());
        Image image = images.get(position);
        lblTitle.setText(image.getSize());
        lblDate.setText(image.getTimestamp());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }


    //Private File Access
    private void addFavs(String favs){
        favs = favs.replace(MediaFiles.WHATSAPP_STATUS_FOLDER_PATH,"");
        favs = favs.replace(MediaFiles.DOWNLOADED_IMAGE_PATH, "");
        Log.d("Filename to be written",favs);
        boolean readingSuccess = false;
        ArrayList<String> lines = new ArrayList<>();
        try{
            FileInputStream fis = getActivity().openFileInput(Favourites.FAV_FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("String lines", line);
                lines.add(line);
            }
            readingSuccess = true;
        }
        catch (FileNotFoundException e){
            Log.e("File open ", "File not found..");
        }
        catch (IOException ios){
            Log.e("File read", "Error reading file");
        }

        if(readingSuccess){
            boolean entry_exists = false;
            for(String ln : lines){
                if(favs.equals(ln)){
                    entry_exists = true;
                    break;
                }
            }
            if(!entry_exists){
                try{
                    FileOutputStream fos = getActivity().openFileOutput(Favourites.FAV_FILENAME, Context.MODE_APPEND);
                    fos.write(favs.getBytes());
                    fos.write("\n".getBytes());
                    fos.close();
                }
                catch (IOException ioexc){
                    ioexc.printStackTrace();
                }
            }
        }
    }

    public int getLineIndex(String line_str){
        int index = -1;
        try{
            FileInputStream fis = getActivity().openFileInput(Favourites.FAV_FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            int nom_index = 0;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("String lines", line);
                if(line.equals(line_str)){
                    index = nom_index;
                    break;
                }
                nom_index++;
            }
        }
        catch (FileNotFoundException e){
            Log.e("File open ", "File not found..");
        }
        catch (IOException ios){
            Log.e("File read", "Error reading file");
        }
        return index;
    }

    public void removeLine(final File file, final int lineIndex) throws IOException{
        final List<String> lines = new LinkedList<>();
        final Scanner reader = new Scanner(new FileInputStream(file), "UTF-8");
        while(reader.hasNextLine())
            lines.add(reader.nextLine());
        reader.close();
        assert lineIndex >= 0 && lineIndex <= lines.size() - 1;
        lines.remove(lineIndex);
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
        for(final String line : lines)
            writer.write(line+"\n");
        writer.flush();
        writer.close();
    }

    //	Adapter class
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        private MediaController mc;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = layoutInflater.inflate(R.layout.image_fullscreen_preview, container, false);

            if(!(images.get(position).isVideo())){
                //The content is an image
                PhotoView photoView = (PhotoView) view.findViewById(R.id.image_preview);
                photoView.setVisibility(View.VISIBLE);

                Image image = images.get(position);

                GlideApp.with(getActivity()).load(image.getLarge())
                        .thumbnail(0.5f)
                        .transition(withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(photoView);

                container.addView(view);
            }
            else{
                //The content is video
                final Image video = images.get(position);

                mc = new MediaController(getActivity());
                final VideoView videoView = (VideoView) view.findViewById(R.id.video_preview);
                videoView.setMediaController(mc);

                videoView.setVisibility(View.VISIBLE);
                Log.d("Video to be played ",video.getLarge());
                String large = video.getLarge();
                videoView.setVideoURI(Uri.parse(large));

                //Set MediaController anchored to Framelayour
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.BOTTOM;
                mc.setLayoutParams(lp);

                ((ViewGroup) mc.getParent()).removeView(mc);

                ((FrameLayout) view.findViewById(R.id.frameLayout)).addView(mc);
                ((FrameLayout) view.findViewById(R.id.frameLayout)).setVisibility(View.VISIBLE);

                videoView.requestFocus();
                videoView.seekTo(10);

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        Log.d("On prepared ", "video prepared");
                        mc.setAnchorView(videoView);
                        videoView.requestFocus();
                        mc.show();
                    }
                });

                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        try{
                            //do your code here
//                            Log.d("Event dispatcher", "Running");
                            long downTime = SystemClock.uptimeMillis();
                            long eventTime = SystemClock.uptimeMillis() + 100;
                            float y = getActivity().getResources().getDisplayMetrics().widthPixels;
                            float x = getActivity().getResources().getDisplayMetrics().heightPixels;
//                            Log.d("Event dispatcher y cord", ""+y);
//                            Log.d("Event dispatcher x cord", ""+x);
                            // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                            int metaState = 0;
                            MotionEvent motionEvent = MotionEvent.obtain(
                                    downTime,
                                    eventTime,
                                    MotionEvent.ACTION_UP,
                                    x,
                                    y,
                                    metaState
                            );

                            videoView.dispatchTouchEvent(motionEvent);
                        }
                        catch (Exception e) {
                            // TODO: handle exception
                            Log.d("Event dispatcher", "Exception");
                        }
                        finally{
                            //also call the same runnable to call it at regular interval
                            handler.postDelayed(this, 3000);
                            Log.d("Event dispatcher", "Calling again");
                        }
                    }
                };

                videoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus){

                            page_title = getArguments().getString("title");
                            if(!(page_title.equals("downloads") || page_title.equals("favs")))
                                videoView.start();
                            handler.postDelayed(runnable, 500);
                        }
                        else {
                            videoView.pause();
                            handler.removeCallbacks(runnable);
                        }
                    }
                });

                container.addView(view);
            }
            return view;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}