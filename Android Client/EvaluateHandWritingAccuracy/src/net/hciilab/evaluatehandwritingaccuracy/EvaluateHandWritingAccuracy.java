/****************************************************************************
 * @project     EvaluateHandWritingAccuracy
 * @copyright   Copyright(C), 2015~, SCUT HCII-Lab(http://www.hcii-lab.net/gpen) All Rights Reserved.
 * @package     com.example.evaluatehandwritingaccuracy
 * @title       EvaluateHandWritingAccuracy.java
 *
 * @model 		 EvaluateHandWritingAccuracy
 * @description TODO
 *
 * @date        2015-5-13
 * @author      Xuefeng Xiao
 * @mail   xiaoxuefengchina@gmail.com
 * @version     1.0.0
 * 
****************************************************************************/
package net.hciilab.evaluatehandwritingaccuracy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EvaluateHandWritingAccuracy extends Activity 
{
	//All test files need to be placed under the specified folder.
	private static final String pathname = Environment.getExternalStorageDirectory().getPath() + "/hciiTestAccuracy";
	
	TextView currentInput ,result ;
	EditText getInput;
	Button nextWord,openFile;
	int finished,length,correct,wrongCount;
	String ShowWord = null ,InputWord = null;
	String ResultFile = null;//the name of the output file
	ArrayList<String> mWordList;
	Boolean finishFlag = false;
	String filename = null;
	
	@Override
	public  void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		nextWord = (Button) findViewById(R.id.nextWord);
		openFile = (Button) findViewById(R.id.openFile);
		currentInput = (TextView) findViewById(R.id.currentInput);
		
		result = (TextView) findViewById(R.id.result);
		getInput = (EditText)findViewById(R.id.getInput);
     
		result.setText("File£º" +'\n'+
				"Total:" +'\n'+
				"Correct/Finished:");
		
       	openFile.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finishFlag = false;
				Intent intent = new Intent(EvaluateHandWritingAccuracy.this,
						OpenFile.class);
				startActivityForResult(intent, BIND_AUTO_CREATE);
			}				
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(data == null) return;
		filename = data.getStringExtra(OpenFile.FILENAME);
		if(filename != null){
			 String samplePath = pathname +"/"+filename;
			 dealFile(samplePath);
		}
	}
	
	/**
	 * start testing
	 * @param samplePath
	 * 
	 */
	private void dealFile(String samplePath){
		mWordList = new ArrayList<String>();
		finished = 0;
		length = 0;
		correct=0;
		wrongCount=0;
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HH-mm-ss");
		ResultFile = sdf.format(date) + ".txt";
		File mFile = new File(pathname + "/" + ResultFile);
		
       
		if(filename!=null){
			try{
				// read the file, the file should be encoded by utf-8 
		        File file = new File(samplePath);
		        InputStreamReader input = new InputStreamReader(new FileInputStream(file), "UTF-8");
				BufferedReader reader = new BufferedReader(input);
				String tmp = null;
				while((tmp = reader.readLine()) != null)
				{
					mWordList.add(tmp);
				}
				reader.close();
		        }
			catch(Exception e){
				e.printStackTrace();
			}
			length = mWordList.size();
			if(length!=0){
				result.setText("File£º" +filename+'\n'+
								"Total:" +String.valueOf(length)+'\n'+
								"Correct/Finished:"+String.valueOf(correct)+'/'+String.valueOf(finished));
				ShowWord  = mWordList.get(finished);  
				currentInput.setText(ShowWord );
				// write the result information to the output files
				try
				{
					FileOutputStream fos = new FileOutputStream (mFile,true);
					final DataOutputStream mFileWriter = new DataOutputStream (fos);   	
					nextWord.setOnClickListener(new OnClickListener()
					{
						public void onClick(View v)
						{
							// when this actvity been created,the finishFlag = false . 
							if(!finishFlag)
							{
								InputWord = getInput.getText().toString();
								++finished ;
								if(InputWord.equals(ShowWord))
									++correct ;
								else
									++wrongCount;
								try{
									// result information of each line  is  number of finished + original word + number of wrong+ input word 
								    mFileWriter.write((String.valueOf(finished)+ShowWord).getBytes());		
									mFileWriter.write(String.valueOf(wrongCount).getBytes());    
									mFileWriter.write((InputWord+'\n').getBytes());		
							 	}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								if(finished==length)
								{
									double accuracyDou = (correct*100.0)/finished ;
									String temp = String.valueOf(accuracyDou);
									String accuracyRate ;
									// finishFlag  has become true 
									finishFlag = true;
									//deal the accuracy rate is irrational number 
									if(temp.length()<7)
										accuracyRate = temp;
									else
										accuracyRate  = temp.substring(0, 6);
									currentInput.setText("");
									getInput.setText("");
									result.setText("File£º" +filename+'\n'+
										"Total:" +String.valueOf(length)+ "                    Accuracy: "+accuracyRate+'%'   + '\n'+
										"Correct/Finished:"+String.valueOf(correct)+'/'+String.valueOf(finished));
									try
									{
										mFileWriter.write(("Accuracy:   "+accuracyRate+'%').getBytes());
									}
									catch(Exception e)
									{
										e.printStackTrace();
									}							
								}
								else
								{
									ShowWord  = mWordList.get(finished);
									currentInput.setText(ShowWord );
									result.setText("File£º" +filename+'\n'+
											"Total:" +String.valueOf(length)+'\n'+
											"Correct/Finished:"+String.valueOf(correct)+'/'+String.valueOf(finished));
									getInput.setText("");  //  clear EditView 
								}	
							}
							// current file has been tested. inform the user to change the test file
							else
							{
								Toast.makeText(getApplicationContext(), "current file has been tested  please open other file"  ,
										Toast.LENGTH_SHORT).show();
							}
						}			
					});
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else{
				result.setText("File£º" +filename+'\n'+
						"This file is empty," +'\n'+
						"please choose another one.");
			}
		}
	}
}












