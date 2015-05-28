/****************************************************************************
 * @project     MouseMoveReceiver
 * @copyright   Copyright(C), 2013~, SCUT HCII-Lab(http://www.hcii-lab.net/gpen) All Rights Reserved.
 * @package     net.hciilab.net.mousemove
 * @title       MouseMoveReciver.java
 *
 * @model 		MouseMoveReciver
 * @description TODO
 *
 * @date        2014-11-17
 * @author      Liquan Qiu
 * @version     1.0.0
 * 
****************************************************************************/
package net.hciilab.mousemove;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.hciilab.net.mousemove.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MouseMoveReceiver extends Activity {
	private TextView mTextView;
	private EditText mEditText;
	private TextView mFilePathTv;
	private Button mButton;

	String logMsg = "";	
	String preMsg = "";
	
	final static String txtFileDecodeString = "UTF-8";
    
    FileBrowserManager mFileBrowserManager;
    String mFilePath;
    String mFileName;
    String mParentDir = Environment.getExternalStorageDirectory().getPath() + "/MouseMoveTest";
    
    FileInputStream mFileInputStream;
    FileOutputStream mFileOutputStream;
    
    boolean flag = true;
    boolean done = true; //Whether need to output information that the test have been done
    
	String mLabelString =null;
	int mLabelStringLen;
	double recognizeRate;
    int processedCount = 0;
    int preStart = 0;
    int correctNumber=0;
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mTextView = (TextView)findViewById(R.id.textView01);
        mEditText = (EditText)findViewById(R.id.editText01);
        mFilePathTv = (TextView)findViewById(R.id.filepath);
        mButton = (Button)findViewById(R.id.button1);
        
        File file = new File(mParentDir);
    	if(!file.exists())
    		file.mkdirs(); 
        mFileBrowserManager = new FileBrowserManager();
        
        mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(FileBrowserManager.DIALOG_ID);
			}
		});
        
       
        mEditText.addTextChangedListener(new TextWatcher(){ 
        	public void afterTextChanged(Editable s) { 
        		
        	}   
        	
        	public void beforeTextChanged(CharSequence s,int start,int count,int after){ 
        		
        	}
        	public void onTextChanged(CharSequence s, int start, int before, int count) {         		
        		//this button can't be clicked when start testing
        		if(processedCount == 1)
        			mButton.setClickable(false);
        		
        		if( start > preStart && processedCount < mLabelStringLen)
        		{
        			processedCount++;
        			preStart = start;//We can get the pre_count by computing the formula (start - preStart)
            		logMsg = mEditText.getText().toString();
            		String targetText=new String();
            				targetText=logMsg.substring( start-1, start);      		
            		int index=processedCount-1;
	
            		if(targetText.equals(mLabelString.substring(index, index+1)))
            		{      
            			correctNumber++;
            			logMsg=targetText+"	"+mLabelString.substring(index, index+1)+"	��" + "\n";
            		}else
            		{
            			logMsg=targetText+"	"+mLabelString.substring(index, index+1)+"	��" + "\n";
            		}
            		mTextView.setText("correct/total: " + correctNumber + "/" + processedCount);

            		try {
    					mFileOutputStream.write( logMsg.getBytes() );
    					mFileOutputStream.flush();  
    				} catch (IOException e) {
    					try {
    						mFileOutputStream.close();
    					} catch (IOException e1) {
    					
    					}
    				} 
    				
    				preMsg = logMsg;
        		}
        		else if(processedCount == mLabelStringLen && done)
        		{
        			mTextView.append("  Done!");
        			done =  !done;
        		}
			} 
        	
        	});
        
    }
 
    protected Dialog onCreateDialog(int id) 
    {
		if (id == FileBrowserManager.DIALOG_ID) 
		{
			return mFileBrowserManager.showDialog(this, mParentDir);
		}
		return null;
	}
    
    /**
	 * get the filepath and create label and output file
	 */
	@SuppressLint("SimpleDateFormat")
	public void endDialogCall(String filePath) {
		mFilePath = filePath;
		mFileName = filePath.substring( filePath.lastIndexOf("/") + 1);
		mFilePathTv.setText("file: " + mFileName);
		
		//to create label
		try {
			File file = new File(filePath);
			mFileInputStream = new FileInputStream(file);
	    	int len1 = (int)file.length();
			byte[] tmp1 = new byte[ len1 ];
			mFileInputStream.read( tmp1 );
			mLabelString = new String(tmp1,txtFileDecodeString);		
		 }catch(Exception e)
		 {
		     try{
					mFileInputStream.close();	 
			}catch(Exception e2)
			{
						
			}
		 }finally
		 {
			try{
				mFileInputStream.close();	 
			}catch(Exception e)
			{
				
			} 
		 }
		mLabelString=mLabelString.trim();
		mLabelString= mLabelString.replaceAll("\n","");
		mLabelString= mLabelString.replaceAll("\r","");
		mLabelStringLen = mLabelString.length();
		//System.out.println(mLabelString);
		
		//to create output file
		try {
        	Date date = new Date();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-hh-mm");
    		String outFilePath = mParentDir + "/" +sdf.format(date) + ".dat";
        	mFileOutputStream = new FileOutputStream(outFilePath);
        } catch (Exception e) {
        	try {
				mFileOutputStream.close();
			} catch (IOException e1) {
			
			}
        }
	}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	try {
    		if( mFileOutputStream != null )
    			mFileOutputStream.close();
		} catch (IOException e1) {
		
		}
    }
}