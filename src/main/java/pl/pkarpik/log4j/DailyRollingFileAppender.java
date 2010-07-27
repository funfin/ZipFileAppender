/**
 * 
 */
package pl.pkarpik.log4j;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 
 */
//Referenced classes of package org.apache.log4j:
//FileAppender, RollingCalendar, AppenderSkeleton, WriterAppender, 
//Layout
/*
DECOMPILATION REPORT

Decompiled from: C:\Users\pkarpik\.m2\repository\log4j\log4j\1.2.13\log4j-1.2.13.jar
Total time: 758 ms
Jad reported messages/errors:
Exit status: 0
Caught exceptions:
 */
public class DailyRollingFileAppender extends FileAppender
{

	public DailyRollingFileAppender()
	{
		datePattern = "'.'yyyy-MM-dd";
		nextCheck = System.currentTimeMillis() - 1L;
		now = new Date();
		rc = new RollingCalendar();
		checkPeriod = -1;
	}

	public DailyRollingFileAppender(Layout layout, String filename, String datePattern)
	throws IOException
	{
		super(layout, filename, true);
		this.datePattern = "'.'yyyy-MM-dd";
		nextCheck = System.currentTimeMillis() - 1L;
		now = new Date();
		rc = new RollingCalendar();
		checkPeriod = -1;
		this.datePattern = datePattern;
		activateOptions();
	}

	public void setDatePattern(String pattern)
	{
		datePattern = pattern;
	}

	public String getDatePattern()
	{
		return datePattern;
	}

	@Override
	public void activateOptions()
	{
		super.activateOptions();
		if(datePattern != null && super.fileName != null)
		{
			now.setTime(System.currentTimeMillis());
			sdf = new SimpleDateFormat(datePattern);
			int type = computeCheckPeriod();
			printPeriodicity(type);
			rc.setType(type);
			File file = new File(super.fileName);
			scheduledFilename = super.fileName + sdf.format(new Date(file.lastModified()));
		} else
		{
			LogLog.error("Either File or DatePattern options are not set for appender [" + super.name + "].");
		}
	}

	void printPeriodicity(int type)
	{
		switch(type)
		{
		case 0: // '\0'
			LogLog.debug("Appender [" + super.name + "] to be rolled every minute.");
			break;

		case 1: // '\001'
			LogLog.debug("Appender [" + super.name + "] to be rolled on top of every hour.");
			break;

		case 2: // '\002'
			LogLog.debug("Appender [" + super.name + "] to be rolled at midday and midnight.");
			break;

		case 3: // '\003'
			LogLog.debug("Appender [" + super.name + "] to be rolled at midnight.");
			break;

		case 4: // '\004'
			LogLog.debug("Appender [" + super.name + "] to be rolled at start of week.");
			break;

		case 5: // '\005'
			LogLog.debug("Appender [" + super.name + "] to be rolled at start of every month.");
			break;

		default:
			LogLog.warn("Unknown periodicity for appender [" + super.name + "].");
			break;
		}
	}

	int computeCheckPeriod()
	{
		RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.ENGLISH);
		Date epoch = new Date(0L);
		if(datePattern != null)
		{
			for(int i = 0; i <= 5; i++)
			{
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
				simpleDateFormat.setTimeZone(gmtTimeZone);
				String r0 = simpleDateFormat.format(epoch);
				rollingCalendar.setType(i);
				Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
				String r1 = simpleDateFormat.format(next);
				if(r0 != null && r1 != null && !r0.equals(r1))
					return i;
			}

		}
		return -1;
	}

	void rollOver()
	throws IOException
	{
		if(datePattern == null)
		{
			super.errorHandler.error("Missing DatePattern option in rollOver().");
			return;
		}
		String datedFilename = super.fileName + sdf.format(now);
		if(scheduledFilename.equals(datedFilename))
			return;
		closeFile();
		File target = new File(scheduledFilename);
		if(target.exists())
			target.delete();
		File file = new File(super.fileName);
		boolean result = file.renameTo(target);
		if(result)
			LogLog.debug(super.fileName + " -> " + scheduledFilename);
		else
			LogLog.error("Failed to rename [" + super.fileName + "] to [" + scheduledFilename + "].");
		try
		{
			setFile(super.fileName, false, super.bufferedIO, super.bufferSize);
		}
		catch(IOException e)
		{
			super.errorHandler.error("setFile(" + super.fileName + ", false) call failed.");
		}
		scheduledFilename = datedFilename;
	}

	@Override
	protected void subAppend(LoggingEvent event)
	{
		long n = System.currentTimeMillis();
		if(n >= nextCheck)
		{
			now.setTime(n);
			nextCheck = rc.getNextCheckMillis(now);
			try
			{
				rollOver();
			}
			catch(IOException ioe)
			{
				LogLog.error("rollOver() failed.", ioe);
			}
		}
		super.subAppend(event);
	}

	static final int TOP_OF_TROUBLE = -1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR = 1;
	static final int HALF_DAY = 2;
	static final int TOP_OF_DAY = 3;
	static final int TOP_OF_WEEK = 4;
	static final int TOP_OF_MONTH = 5;
	private String datePattern;
	private String scheduledFilename;
	private long nextCheck;
	Date now;
	SimpleDateFormat sdf;
	RollingCalendar rc;
	int checkPeriod;
	static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

	class RollingCalendar extends GregorianCalendar {
		private static final long serialVersionUID = -3560331770601814177L;

		int type = TOP_OF_TROUBLE;

		RollingCalendar() {
			super();
		}

		RollingCalendar(TimeZone tz, Locale locale) {
			super(tz, locale);
		}

		void setType(int type) {
			this.type = type;
		}

		public long getNextCheckMillis(Date now) {
			return getNextCheckDate(now).getTime();
		}

		public Date getNextCheckDate(Date now) {
			this.setTime(now);

			switch (type) {
			case TOP_OF_MINUTE:
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.MINUTE, 1);
				break;
			case TOP_OF_HOUR:
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.HOUR_OF_DAY, 1);
				break;
			case HALF_DAY:
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				int hour = get(Calendar.HOUR_OF_DAY);
				if (hour < 12) {
					this.set(Calendar.HOUR_OF_DAY, 12);
				} else {
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.add(Calendar.DAY_OF_MONTH, 1);
				}
				break;
			case TOP_OF_DAY:
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.DATE, 1);
				break;
			case TOP_OF_WEEK:
				this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.WEEK_OF_YEAR, 1);
				break;
			case TOP_OF_MONTH:
				this.set(Calendar.DATE, 1);
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.MONTH, 1);
				break;
			default:
				throw new IllegalStateException("Unknown periodicity type.");
			}
			return getTime();
		}
	}
}
