/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An utility class that helps to unzip any zip file to desired output directory.
 * 
 * @author Igor Goldenberg
 */
public class ZipUtils
{
    private static Logger logger = LoggerFactory.getLogger( ZipUtils.class );
	static public void unzipArchive(File archive, File outputDir)
		throws ZipException
	{
		try
		{
			ZipFile zipfile = new ZipFile(archive);
			for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements();)
			{
				ZipEntry entry = e.nextElement();
				unzipEntry(zipfile, entry, outputDir);
			}
		} catch (Exception e)
		{
			String msg = "Failed to extract archive: " + archive;
			logger.error( msg, e );
			throw new ZipException(msg);
		}
	}

	static private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir)
			throws IOException
	{

		if (entry.isDirectory())
		{
			new File(outputDir, entry.getName()).mkdir();
			return;
		}

		File outputFile = new File(outputDir, entry.getName());
		if (!outputFile.getParentFile().exists())
			outputFile.getParentFile().mkdir();

		logger.debug("Extracting: [{}]", entry);
		BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

		try
		{
			IOUtils.copy(inputStream, outputStream);
		} finally
		{
			outputStream.close();
			inputStream.close();
		}
	}
}