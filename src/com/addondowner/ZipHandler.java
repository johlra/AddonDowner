package com.addondowner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by johlar on 30/03/15.
 *
 */
public class ZipHandler {
	private static final int BUFFER_SIZE = 4096;
	private static final String SEPARATOR = "/"; //File.separator;

	private List<String> topDirs;
	private List<Toc> tocs;
	private final String zipFilePath;
	private final String destDirectory;
	private boolean writeToDisk;

	public ZipHandler(String zipFilePath, String destDirectory) {
		topDirs = new ArrayList<String>();
		tocs = new ArrayList<Toc>();
		this.zipFilePath = zipFilePath;
		this.destDirectory = destDirectory;
		this.writeToDisk = true;
	}

	public ZipHandler(String zipFilePath) {
		topDirs = new ArrayList<String>();
		tocs = new ArrayList<Toc>();
		this.zipFilePath = zipFilePath;
		this.destDirectory = "";
		this.writeToDisk = false;
	}

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 *
	 * @throws IOException
	 */

	public void unzip() throws IOException {
		if(writeToDisk) {
			File destDir = new File(destDirectory);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			collectDir(entry);
			String filePath = destDirectory + SEPARATOR + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath, entry.getCrc());
			} else {
				if(writeToDisk) {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					dir.mkdirs();
				}
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	private void collectDir(ZipEntry entry) {
		String filePath = entry.getName();
		int indexOf = filePath.indexOf(SEPARATOR);
		String dir = filePath.substring(0, indexOf);
		if(!topDirs.contains(dir)){
			topDirs.add(dir);
		}
	}


	/**
	 * Extracts a zip entry (file entry)
	 *
	 * @param zipIn zipstream
	 * @param filePath file
	 * @param crc file zip crc
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath, long crc) throws IOException {

		// make dirs so we can extract file
		int lastIndexOf = filePath.lastIndexOf(SEPARATOR);
		if(writeToDisk) {
			String path = filePath.substring(0, lastIndexOf);
			File dir = new File(path);
			dir.mkdirs();
		}

		// Checking if is toc
		String fileName = "";
		boolean isToc = false;
		//org.apache.commons.StringUtils.countMatches()
		int amountDirs = 0;
		for (int i = 0; i < filePath.length(); i++)
			if (filePath.charAt(i) == SEPARATOR.charAt(0))
				amountDirs++;
		if (filePath.endsWith(".toc") && amountDirs == 2) {
			isToc = true;
			fileName = filePath.substring(lastIndexOf + 1);
		}

		// extract file
		StringBuilder fileContent = new StringBuilder();
		if (writeToDisk) {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read;
			while ((read = zipIn.read(bytesIn)) != -1) {
				if (isToc) {
					copyOutTocFile(fileContent, bytesIn, read);
				}
				bos.write(bytesIn, 0, read);
			}
			bos.close();
		} else if (isToc) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read;
			while ((read = zipIn.read(bytesIn)) != -1) {
				copyOutTocFile(fileContent, bytesIn, read);
			}
		}

		if (isToc && fileContent.length() > 0) {
			tocs.add(new Toc(fileName, fileContent, crc));
		}
	}

	private void copyOutTocFile(StringBuilder fileContent, byte[] bytesIn, int read) throws UnsupportedEncodingException {
		byte[] t = new byte[read];
		System.arraycopy(bytesIn, 0, t, 0, read);
		fileContent.append(new String(t, "UTF-8"));
	}

	public List<String> getTopDirs() {
		return topDirs;
	}

	public List<Toc> getTocs() {
		return tocs;
	}

	public String getUniqueVersion() {
		return Toc.getUniqueVersion(tocs);
	}
}
