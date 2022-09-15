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
	private MediaRecorder recorder = null, camera_recorder = null;
	private String main_path, stereo_path, current_file_name;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* variables initialization */
		recorder = null;
		list_data = new ArrayList<>();
		this.recording_list = (ListView) findViewById(R.id.list_view);

		/* check preliminary */
		if (grant_permission())
		{
			if (create_folder())
			{
				Toast toast = Toast.makeText(getApplicationContext(), "Preliminaries Done.", Toast.LENGTH_LONG);
				toast.show();
			}
			else
			{
				Toast toast = Toast.makeText(getApplicationContext(), "Folders creation failed.", Toast.LENGTH_LONG);
				toast.show();

				System.exit(100);
			}
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), "We need permission!", Toast.LENGTH_LONG);
			toast.show();

			System.exit(100);
		}
	}

	public boolean create_folder()
	{
		boolean result = true;

		/* create two folder to store sound from different mic */
		try
		{
			File path = new File(getExternalCacheDir(), "main");
			main_path = path.getAbsolutePath() + "/";

			if (!path.exists())
			{
				result = path.mkdirs();
			}

			path = new File(getExternalCacheDir(), "stereo");
			stereo_path = path.getAbsolutePath() + "/";

			if (!path.exists())
			{
				result = result & path.mkdirs();
			}
		}
		catch (Exception e)
		{
			result = false;
		}

		return result;
	}

	/* start recording sound */
	public void record(boolean mode)
	{
		try
		{
			if (recorder != null)
			{
				recorder.prepare();
				recorder.start();
			}

			if (!mode && camera_recorder != null)
			{
				System.out.println("===========================stereo begin");
				camera_recorder.prepare();
				camera_recorder.start();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* stop recorder */
	public void stop_record(View v)
	{
		try
		{
			if (recorder != null)
			{
				recorder.stop();
				recorder.release();
				recorder = null;

				list_data.add(current_file_name);
				ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list_data);
				recording_list.setAdapter(adapter);
			}

			if (camera_recorder != null)
			{
				camera_recorder.stop();
				camera_recorder.release();
				camera_recorder = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			System.exit(101);
		}
	}

	/* mode true for record with main mic, false for record with main mic and top mic */
	public boolean configure_recorder(boolean mode)
	{
		boolean result = false;

		/* formulate the file name with timestamp */
		long current_timestamp = System.currentTimeMillis();
		SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
		String file_path = "", top_file_path = "";

		/* store the file in different directory according to the mode */
		if (mode)
		{
			current_file_name = "main/" + date_format.format(current_timestamp) + ".acc";
			file_path = main_path + date_format.format(current_timestamp) + ".acc";
		}
		else
		{
			current_file_name = "stereo/" + date_format.format(current_timestamp) + ".acc";
			file_path = stereo_path + date_format.format(current_timestamp) + "_main.acc";
			top_file_path = stereo_path + date_format.format(current_timestamp) + "_top.acc";
		}

		File record_file = new File(file_path);
		if (record_file.exists())
		{
			record_file.delete();
		}

		File top_file = new File(top_file_path);
		if (top_file.exists())
		{
			top_file.delete();
		}

		/* initialize the recorder if the recorder is not working */
		if (recorder == null)
		{
			recorder = new MediaRecorder();
			recorder.setAudioSamplingRate(44100);

			/* configure mic, output sound format, encoder, and output file */
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
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

					Toast toast = Toast.makeText(getApplicationContext(), "main recorder error", Toast.LENGTH_LONG);
					toast.show();
				}
			});

			result = true;
		}

		try
		{
			if (camera_recorder == null && !mode)
			{
				camera_recorder = new MediaRecorder();
				camera_recorder.setAudioSamplingRate(44100);

				/* configure mic, output sound format, encoder, and output file */

				camera_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				camera_recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
				camera_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				camera_recorder.setOutputFile(top_file.getAbsolutePath());
				System.out.println(top_file.getAbsolutePath());

				/* add recorder error listener method */
				camera_recorder.setOnErrorListener(new MediaRecorder.OnErrorListener()
				{
					@Override
					public void onError(MediaRecorder mediaRecorder, int i, int i1)
					{
						camera_recorder.stop();
						camera_recorder.release();
						camera_recorder = null;

						Toast toast = Toast.makeText(getApplicationContext(), "camera recorder error", Toast.LENGTH_LONG);
						toast.show();
					}
				});

				result = result & true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			Toast toast = Toast.makeText(getApplicationContext(), "camera recorder may not be supported", Toast.LENGTH_LONG);
			toast.show();

			result = false;
		}

		/* message */
		if (result)
		{
			Toast toast = Toast.makeText(getApplicationContext(), "start recording with " + (mode ? "main mic" : "stereo mic"), Toast.LENGTH_LONG);
			toast.show();
		}

		return result;
	}

	public void record_with_main(View v)
	{
		if (configure_recorder(true))
		{
			record(true);
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), "recorder initialization failed", Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void record_with_stereo(View v)
	{
		if (configure_recorder(false))
		{
			record(false);
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
	public boolean grant_permission()
	{
		String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};

		try
		{
			if (!check_permission(permissions))
			{
				ActivityCompat.requestPermissions(this, permissions, 10000);
			}

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return false;
		}
	}
}