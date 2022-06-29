package com.hariofspades.chatbot;

import android.content.Intent;

import android.content.res.AssetManager;
import android.net.Uri;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import com.hariofspades.chatbot.Adapter.ChatMessageAdapter;
import com.hariofspades.chatbot.Pojo.ChatMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {


    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    public Bot bot;
    public static Chat chat;
    private ChatMessageAdapter mAdapter;

    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;

    private final WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
    private final Node titleNode = new Node();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (FloatingActionButton) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        ImageView mImageView = (ImageView) findViewById(R.id.iv_OpenChat);

        mAdapter = new ChatMessageAdapter(this, new ArrayList<>());

        mImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatBotActivity.class);
            //intent.putExtra("message_data", response);
            startActivity(intent);

        });


        //receiving the assets from the app directory
        AssetManager assets = getResources().getAssets();
        File jayDir = new File(Objects.requireNonNull(getApplicationContext().getExternalFilesDir(null)).getAbsolutePath() + "/hari/bots/Hari");
        //boolean b = jayDir.mkdirs();
        if (jayDir.exists()) {
            //Reading the file
            try {
                for (String dir : Objects.requireNonNull(assets.list("Hari"))) {
                    //File subdir = new File(jayDir.getPath() + "/" + dir);
                    //boolean subdir_check = subdir.mkdirs();
                    for (String file : Objects.requireNonNull(assets.list("Hari/" + dir))) {
                        File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                        if (f.exists()) {
                            continue;
                        }
                        InputStream in;
                        OutputStream out;
                        in = assets.open("Hari/" + dir + "/" + file);
                        out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
                        //copy file from assets to the mobile's SD card or any secondary memory
                        copyFile(in, out);
                        in.close();
                        out.flush();
                        out.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get the working directory
        MagicStrings.root_path = Objects.requireNonNull(getApplicationContext().getExternalFilesDir(null)).getAbsolutePath() + "/hari";
        System.out.println("Working Directory = " + MagicStrings.root_path);
        AIMLProcessor.extension = new PCAIMLProcessorExtension();
        //Assign the AIML files to bot for processing
        bot = new Bot("Hari", MagicStrings.root_path, "chat");
        chat = new Chat(bot);
        //String[] args = null;
        //mainFunction(args);

        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        loadModels();

    }

    private String getResponse() {
        return chat.multisentenceRespond(mEditTextMessage.getText().toString());
    }


    //copying the file
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    public void loadModels() {
        //WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/Persian.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {
                    MainActivity activity = weakActivity.get();

                    mButtonSend.setOnClickListener(v -> {
                        String message = mEditTextMessage.getText().toString();
                        TextView textView = (TextView) viewRenderable.getView();

                        String response = getResponse();
                        textView.setText(response);

                        if (TextUtils.isEmpty(message)) {
                            return;
                        }
                        mEditTextMessage.setText("");
                    });

                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null || viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model)
                .animate(true).start();
        model.select();

        //Node titleNode = new Node();
        titleNode.setParent(model);
        titleNode.setEnabled(false);
        titleNode.setLocalPosition(new Vector3(0.0f, 0.4f, 0.0f));
        titleNode.setRenderable(viewRenderable);
        titleNode.setEnabled(true);
    }

}
