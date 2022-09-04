package com.example.smartrecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
	private ListView recording_list;
	private ArrayList<String> list_data;
	private MediaRecorder recorder;
	private String main_path, bottom_path, current_file_name;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* variables initialization */
		boolean result = false;
		recorder = null;
		list_data = new ArrayList<>();
		this.recording_list = (ListView) findViewById(R.id.list_view);

		/* create two folder to store sound from different mic */
		File path = new File(getExternalCacheDir(), "main");
		main_path = path.getAbsolutePath() + "/";
		if (!path.exists())
		{
			result = path.mkdirs();
		}

		path = new File(getExternalCacheDir(), "bottom");
		bottom_path = path.getAbsolutePath() + "/";
		if (!path.exists())
		{
			result = result & path.mkdirs();
		}

		if (result)
		{
			Toast toast = Toast.makeText(getApplicationContext(), "folders created successfully", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	/* start recording sound */
	public void record()
	{
		if (recorder != null)
		{
			try
			{
				recorder.prepare();
				recorder.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/* stop recorder */
	public void stop_record(View v)
	{
		recorder.stop();
		recorder.release();
		recorder = null;

		list_data.add(current_file_name);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list_data);
		recording_list.setAdapter(adapter);
	}

	/* mode true for record with main mic, false for record with bottom mic */
	public boolean configure_recorder(boolean mode)
	{
		boolean result = false;

		/* formulate the file name with timestamp */
		long current_timestamp = System.currentTimeMillis();
		SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
		String file_path = "";

		/* store the file in different directory according to the mode */
		if (mode)
		{
			current_file_name = "main/" + date_format.format(current_timestamp) + ".amr";
			file_path = main_path + date_format.format(current_timestamp) + ".amr";
		}
		else
		{
			current_file_name = "bottom/" + date_format.format(current_timestamp) + ".amr";
			file_path = bottom_path + date_format.format(current_timestamp) + ".amr";
		}

		File record_file = new File(file_path);
		if (record_file.exists())
		{
			record_file.delete();
		}

		/* initialize the recorder if the recorder is not working */
		if (recorder == null)
		{
			recorder = new MediaRecorder();
			recorder.setAudioSamplingRate(44100);

			/* select recording mic according to mode */
			if (mode)
			{
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			}
			else
			{
				recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
			}

			/* configure output sound format, encoder, and output file */
			recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(record_file.getAbsolutePath());
			System.out.println(record_file.getAbsolutePath());

			/* add recorder error listener method */
			recorder.setOnErrorListener(new MediaRecorder.OnErrorListener()
			{
				@Override
				public void onError(MediaRecorder mediaRecorder, int i, int i1)
				{
					recorder.stop();
					recorder.release();
					recorder = null;

					Toast toast = Toast.makeText(getApplicationContext(), "recorder error", Toast.LENGTH_LONG);
					toast.show();
				}
			});

			/* message */
			Toast toast = Toast.makeText(getApplicationContext(), "start recording with " + (mode ? "main mic" : "bottom mic"), Toast.LENGTH_LONG);
			toast.show();

			result = true;
		}

		return result;
	}

	public void record_with_main(View v)
	{
		if (configure_recorder(true))
		{
			record();
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), "recorder initialization failed", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void record_with_bottom(View v)
	{
		if (configure_recorder(false))
		{
			record();
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), "recorder initialization failed", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	/* check if permissions are granted */
	private boolean check_permission(String[] permissions)
	{
		for (String permission : permissions)
		{
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
			{
				return false;
			}
		}

		return true;
	}

	/* asking for permission */
	public void grant_permission(View v)
	{
		String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};

		if (check_permission(permissions))
		{
			Toast toast = Toast.makeText(getApplicationContext(), "permission already granted", Toast.LENGTH_LONG);
			toast.show();
		}
		else
		{
			ActivityCompat.requestPermissions(this, permissions, 10000);
		}
	}
}