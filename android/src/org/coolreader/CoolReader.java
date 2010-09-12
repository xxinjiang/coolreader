// Main Class
package org.coolreader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.coolreader.crengine.Engine;
import org.coolreader.crengine.ReaderView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class CoolReader extends Activity
{
	
	void installLibrary()
	{
		File sopath = getDir("libs", Context.MODE_PRIVATE);
		File soname = new File(sopath, "libcr3engine.so");
		try {
			sopath.mkdirs();
	    	File zip = new File(getPackageCodePath());
	    	ZipFile zipfile = new ZipFile(zip);
	    	ZipEntry zipentry = zipfile.getEntry("lib/armeabi/libcr3engine.so");
	    	if ( !soname.exists() || zipentry.getSize()!=soname.length() ) {
		    	InputStream is = zipfile.getInputStream(zipentry);
				OutputStream os = new FileOutputStream(soname);
		        Log.i("cr3", "Installing JNI library " + soname.getAbsolutePath());
				final int BUF_SIZE = 0x10000;
				byte[] buf = new byte[BUF_SIZE];
				int n;
				while ((n = is.read(buf)) > 0)
				    os.write(buf, 0, n);
		        is.close();
		        os.close();
	    	} else {
		        Log.i("cr3", "JNI library " + soname.getAbsolutePath() + " is up to date");
	    	}
			System.load(soname.getAbsolutePath());
		} catch ( Exception e ) {
	        Log.e("cr3", "cannot install cr3engine library", e);
		}
	}
	
	private String[] findFonts()
	{
		File fontDir = new File( Environment.getRootDirectory(), "fonts");
		// get font names
		String[] fileList = fontDir.list(
				new FilenameFilter()
		{ public boolean  accept(File  dir, String  filename)
			{
				return filename.endsWith(".ttf") && !filename.endsWith("Fallback.ttf");
			}
			});
		// append path
		for ( int i=0; i<fileList.length; i++ ) {
			fileList[i] = new File(fontDir, fileList[i]).getAbsolutePath();
			Log.v("cr3", "found font: " + fileList[i]);
		}
		return fileList;
	}
	
	Engine engine;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	installLibrary();
    	String[] fonts = findFonts();
    	try {
    		Log.v("cr3", "Creating engine");
    		engine = new Engine( fonts );
    		Log.v("cr3", "Requesting font face list");
    		String[] faces = engine.getFontFaceList();
    		for ( String face : faces )
    			Log.v("cr3", "* font face: " + face);
    	} catch ( IOException e ) {
    		Log.e("cr3", "CREngine init failed", e);
    		
    		throw new RuntimeException("CREngine init failed");
    	}
        super.onCreate(savedInstanceState);
        setContentView(new ReaderView(this, engine));
    }

	@Override
	protected void onDestroy() {
		if ( engine!=null ) {
			engine.uninit();
			engine = null;
		}
			
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}