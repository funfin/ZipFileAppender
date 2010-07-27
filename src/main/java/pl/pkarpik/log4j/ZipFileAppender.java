/**
 * 
 */
package pl.pkarpik.log4j;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author pkarpik
 *
 */
public class ZipFileAppender extends DailyRollingFileAppender{

	private String compressBackups = "false";
	private String maxNumberOfBackups = "14";


	@Override
	void rollOver() throws IOException {
		super.rollOver();
		buckupsManagment();
	}

	public void buckupsManagment() {
		final File file = new File(fileName);
		int maxBackups=14;
		try{
			maxBackups=Integer.parseInt(getMaxNumberOfBackups());
		}catch (Exception e) {
		}


		//zipowanie logow
		File[] files = file.getParentFile().listFiles(getFileFilter(file));
		for (int i = 0; i < files.length; i++) {
			if (getCompressBackups().equalsIgnoreCase("YES") || getCompressBackups().equalsIgnoreCase("TRUE")) {
				zipFile(files[i]);
			}
		}

		//sortowanie logow
		files = file.getParentFile().listFiles(getFileFilter(file));
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		//usuwanie plikow w przypadku przekroczenia maksymalnej liczby logow
		for (int i = 0; i < files.length; i++) {
			if (i<files.length-maxBackups) {
				files[i].delete();
			}
		}

	}

	protected void zipFile(File file) {
		if (!file.getName().endsWith(".zip")) {
			try {
				File zipFile = new File(file.getParent(), file.getName() + ".zip");
				FileInputStream fis;
				fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(zipFile);
				ZipOutputStream zos = new ZipOutputStream(fos);
				ZipEntry zipEntry = new ZipEntry(file.getName());
				zos.putNextEntry(zipEntry);

				byte[] buffer = new byte[4096];
				while (true) {
					int bytesRead = fis.read(buffer);
					if (bytesRead == -1)
						break;
					else {
						zos.write(buffer, 0, bytesRead);
					}
				}
				zos.closeEntry();
				fis.close();
				zos.close();
				file.delete();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}

	protected FileFilter getFileFilter(final File file){
		return new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory() || pathname.getName().toUpperCase().equals(file.getName().toUpperCase()))
					return false;
				return pathname.getName().toUpperCase().startsWith(file.getName().toUpperCase());
			}
		};
	}



	public String getCompressBackups() {
		return compressBackups;
	}
	public void setCompressBackups(String compressBackups) {
		this.compressBackups = compressBackups;
	}
	public String getMaxNumberOfBackups() {
		return maxNumberOfBackups;
	}
	public void setMaxNumberOfBackups(String maxNumberOfBackups) {
		this.maxNumberOfBackups = maxNumberOfBackups;
	}

}
