package pl.pkarpik.log4j.zip.test;
import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * 
 */

/**
 * @author pkarpik
 *
 */
public class ZipFileAppenderTest extends TestCase {


	private Logger log = Logger.getLogger(ZipFileAppenderTest.class);

	public void testZipFileAppender(){


		for(int i=0;i<200;i++){
			log.info(i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

}
