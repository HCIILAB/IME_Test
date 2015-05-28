/****************************************************************************
 * @project     MouseMoveReceiver
 * @copyright   Copyright(C), 2013~, SCUT HCII-Lab(http://www.hcii-lab.net/gpen) All Rights Reserved.
 * @package     net.hciilab.net.mousemove
 * @title       FileBrowserManager.java
 *
 * @model 		FileBroswerDialog
 * @description TODO
 *
 * @date        2013-4-17
 * @author      ��Ծ
 * @tutor       ������
 * @version     1.0.0
 * 
 * @modified 
 *	1.Date: 2014-11-16 by Liquan Qiu
 *         �޸����ó�ʼ��Ŀ¼
****************************************************************************/

package net.hciilab.mousemove;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hciilab.net.mousemove.R;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/****************************************************************************
 * @className   FileBrowserManager
 * @description �ļ��Ի��������
 * @author      ��Ծ
 * @version     1.0
 * @date        2013	2013-4-17	  ����8:24:18
 ****************************************************************************/
public class FileBrowserManager {

	/** �ļ���ʾ�Ի��� */ 
	private Dialog mDialog = null;
	/** �Ի���ID */
	public static final int DIALOG_ID = 1;
	/** �ļ��б� */ 
	private FileListView mListView;
	
	/** ��activity */ 
	private MouseMoveReceiver mAcivity;
	
	/** �û����ѡ����ļ�·�� */ 
	private String mChoiceFilePath = "";
	
	/**
	 * ��ʾ�Ի���.
	 */
	public Dialog showDialog(MouseMoveReceiver activity, String directory) {
		mAcivity = activity;
		// �����Ի���
		createDialog(activity,directory);		
		return mDialog;
		
	}

	/**
	 * ���ó�ʼ��Ŀ¼��Ĭ��Ϊ��Ŀ¼.
	 */
	public void setDirectory(String directory) {
		mListView.mInitDirectory = directory;
	}	
	
	/**
	 * ��ȡ�û����ѡ����ļ�·��.
	 */
	public String getChoiceFile() {
		return mListView.getFilePath();
	}
	
	/**
	 * �����Ի���.
	 */
	private void createDialog(Context context, String directory) {
		
		if (mDialog != null) {
			return;
		}
		
		mListView = new FileListView(context, directory);
		AlertDialog.Builder builder= new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_name);
		builder.setView(mListView);
		builder.setPositiveButton("confirm", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mListView.mPreChoisePosition != FileListView.FILE_NOCHOICE) {
					mAcivity.endDialogCall(mListView.getFilePath());
					mDialog.dismiss();
				}
				else {
					// ����ûѡ��
					Toast.makeText(mAcivity, "Please select a path or a file!", 300).show();
				}
			}
		});
		builder.setNegativeButton("cancel", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				mListView.resetState();
				//mAcivity.endDialogCall("");
				mDialog.dismiss();
			}
		});
		
		
		mDialog = builder.show();
		
//		mDialog = new Dialog(context);
//		mDialog.setContentView(mListView);
//		mDialog.setTitle("Custom Dialog");
//		mDialog.show();

		// �޸��ļ��Ի���Ĵ�С
		DisplayMetrics dm = mAcivity.getResources().getDisplayMetrics();  
		WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
		params.width = (int)(0.9 * dm.widthPixels);
		params.height = (int)(0.9 * dm.heightPixels) ;
		mDialog.getWindow().setAttributes(params);
	}
	
		
	/****************************************************************************
	 * @className   FileListView
	 * @description �ڲ��࣬������ʾ�ļ��б��listview
	 * @author      ��Ծ
	 * @version     1.0
	 * @date        2013	2013-4-18	  ����2:50:13
	****************************************************************************/
	private class FileListView extends ListView implements android.widget.AdapterView.OnItemClickListener {
		
		/** ��Ŀ¼ */ 
		private static final String sRoot = "/";
		/** ������һ���ַ� */ 
		private static final String sParent = "..";
		
		/** �ļ�����id�ַ�����map */ 
		private static final String FILETYPEID = "filetypeid";
		/** �ļ������ַ�����map */ 
		private static final String FILENAME = "filename";
		/** �ļ�ûѡ�б�־ */ 
		private static final int FILE_NOCHOICE = -1;
		/** ѡ�е�λ�� */ 
		public int mPreChoisePosition;
		/** ѡ�е�view */ 
		private View mChoiseView;
//		private static final String FILEPATH = "filepath";
		
		/** ��ʼ����Ŀ¼ */ 
		public String mInitDirectory = "";
		/** ��ǰ����Ŀ¼ */ 
		public String mCurrentDirectory;
		/** ��ʾlist */ 
		private List<Map<String, Object>> mList = new ArrayList<Map<String,Object>>();
		
		
		
		/**
		 * Creates a new instance of FileBrowserManager.java.
		 * @param context
		 */
		public FileListView(Context context, String directory) {
			super(context);
			// TODO Auto-generated constructor stub
			mInitDirectory = directory;
			if (mInitDirectory == "") {
				mInitDirectory = sRoot;
			}
			
			setOnItemClickListener(this);
			setCacheColorHint(0);
			resetState();
			
			mCurrentDirectory = mInitDirectory;
			refreshListView();
			
		}
		
		/**
		 * ��ȡ��ǰѡ���ļ�·��.
		 */
		public String getFilePath() {
			String fileName = (String)mList.get(mPreChoisePosition).get(FILENAME);
			
			// ��ȡ�û���ǰ�ļ�·��
			String filePath;
//			filePath = currentDirectory.getAbsolutePath() + fileName ;
			if (sRoot.equals(mCurrentDirectory)) {
				filePath= mCurrentDirectory  + fileName;
			}
			else {
				filePath= mCurrentDirectory + "/" + fileName;
			}
			return filePath;
		}
		
		
		/**
		 * �ظ�ԭʼδѡ��״̬.
		 */
		public void resetState() {
			mPreChoisePosition = FILE_NOCHOICE;
			mChoiseView = null;
		}
		
		/**
		 * ˢ����ʾ�б�.
		 */
		private void refreshListView() {
			// TODO Auto-generated method stub		
			if (mList != null) {
				mList.clear();
			}
			
			resetState();
			
			int currentDrawableID = 0;
			// ��ȡ�ļ��б�
			File[] files = new File(mCurrentDirectory).listFiles();
			
			// �ж��Ƿ�Ϊ��Ŀ¼����Ŀ¼û�и�Ŀ¼
			if (!sRoot.equals(mCurrentDirectory)) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(FILETYPEID, R.drawable.filedialog_folder_up);
				map.put(FILENAME, sParent);
				mList.add(map);
			}
			
			if (files != null) {				
				for(File currentfile: files) {
					// �ж��Ƿ�ΪĿ¼
					if (currentfile.isDirectory()) {
						currentDrawableID = R.drawable.filedialog_folder;
					}
					else {
						// ����ļ����ȡ��Ӧ���ļ�������ʾͼƬid
						currentDrawableID = getDrawableID(currentfile.getName());
					}
					
					// ����
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(FILETYPEID, currentDrawableID);
					map.put(FILENAME, currentfile.getName());
					mList.add(map);
				}
				// ���ļ�������
				Collections.sort(mList, mComparator);
			}
			else {
				// �ļ����ܶ�ȡ
//				Toast.makeText(mAcivity, "sorry, this directory cannot read!", 300).show();
			}
			
			// ������������ˢ����ʾ
			SimpleAdapter adapter = new SimpleAdapter(mAcivity, mList, R.layout.filedialogitem, 
					new String[]{FILETYPEID, FILENAME}, new int[]{R.id.myimageview, R.id.mytextview});
			this.setAdapter(adapter);
			
		}

		
		/**
		 * ����ļ����ȡ��Ӧ���ļ�������ʾͼƬid.
		 * @param fileName �ļ���
		 * @return �ļ�������ʾͼƬid
		 */
		private int getDrawableID(String fileName) {
			
			int drawableID;
			if(checkEndsWithInStringArray(fileName, getResources().
					getStringArray(R.array.fileEndingImage))){
				drawableID = R.drawable.filedialog_image; 
			}else if(checkEndsWithInStringArray(fileName, getResources().
							getStringArray(R.array.fileEndingWebText))){
				drawableID = R.drawable.filedialog_webtext;
			}else if(checkEndsWithInStringArray(fileName, getResources().
							getStringArray(R.array.fileEndingPackage))){
				drawableID = R.drawable.filedialog_packed;
			}else if(checkEndsWithInStringArray(fileName, getResources().
							getStringArray(R.array.fileEndingAudio))){
				drawableID = R.drawable.filedialog_audio;
			}else{
				drawableID = R.drawable.filedialog_text;
			}
			return drawableID;
			
		}
		
		/** Checks whether checkItsEnd ends with 
		 * one of the Strings from fileEndings */
		private boolean checkEndsWithInStringArray(String checkItsEnd, 
						String[] fileEndings){
			for(String aEnd : fileEndings){
				if(checkItsEnd.endsWith(aEnd))
					return true;
			}
			return false;
		}
		
		
		/** �����ļ����� */ 
		private Comparator< Map<String,Object> > mComparator = new Comparator<Map<String,Object>>() {
			
			public int compare(Map<String, Object> object1,
					Map<String, Object> object2) {
				// TODO Auto-generated method stub
				return ((String) object1.get(FileListView.FILENAME)).compareTo(
						(String) object2.get(FileListView.FILENAME));
				
			}
			
		};
		
		/**
		 * Override.
		 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
//			mActivity.removeDialog(DIALOG_ID);
			
			// ��ȡ����ѡ����ļ���
//			Map<String, Object> map = (Map<String, Object>)parent.getItemAtPosition(position); 
			
			String fileName = (String)mList.get(position).get(FILENAME);
			
			// ��ȡ�û���ǰ�ļ�·��
			String filePath;
			File currentDirectory = new File(mCurrentDirectory);
//			filePath = currentDirectory.getAbsolutePath() + fileName ;
			if (sRoot.equals(mCurrentDirectory)) {
				filePath= mCurrentDirectory  + fileName;
			}
			else {
				filePath= mCurrentDirectory + "/" + fileName;
			}
			
			File currentFile = new File(filePath);
			
			// �ж��Ƿ�ѡ���˸�Ŀ¼
			if (sParent.equals(fileName)) {
				
				String parentFileString = currentDirectory.getParent();
				if (parentFileString != null) {
					mCurrentDirectory = parentFileString;
				}
				else {
					mCurrentDirectory = sRoot;
				}
			}
			else {
				
				// ����ѡ������һ���ļ�
				if (mPreChoisePosition != position) {
					// �ظ�֮ǰview��״̬
					if (mChoiseView != null) {
						mChoiseView.setBackgroundDrawable(null);
					}
					// ������ѡ��view��״̬
					mPreChoisePosition = position;
					mChoiseView = view;
					mChoiseView.setBackgroundColor(Color.BLUE);
					
					return;
				}
				
				
				if (!currentFile.isDirectory()) {
					// �����ļ��������Ի�������ѡ���ļ�·��
					mChoiceFilePath = filePath;
//					mAcivity.endDialogCall(filePath);
//					mDialog.dismiss();
					return;
				}
				else {
					mCurrentDirectory = filePath;
				}
			}
			refreshListView();
			
		}
	}
	
}
