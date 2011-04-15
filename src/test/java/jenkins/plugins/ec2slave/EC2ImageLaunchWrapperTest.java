/*
 * The MIT License
 * 
 * Copyright (c) 2011, Aaron Phillips
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.ec2slave;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

public class EC2ImageLaunchWrapperTest {
  
  private String credsFile = "../../aws-test/src/AwsCredentials.properties";

  /**
   * For obvious reasons this test doesn't work out of the box.  It is really only practical for 
   * developer (not CI) testing.  To run it, point to your AwsCredentials.properties and set ami, 
   * instanceType, and keypairName
   */
  @Ignore
	@Test
	public void testImageLauncher() throws FileNotFoundException, IOException, InterruptedException {
		Properties props = new Properties();
		props.load(new FileReader(credsFile));

		String ami = "ami-xxxx";
		String instanceType = "t1.micro";
		String keypairName = "mykeypair";

		EC2ImageLaunchWrapper launcher = new EC2ImageLaunchWrapper(null,
				props.getProperty("secretKey"), props.getProperty("accessKey"),
				ami, instanceType, keypairName);

		launcher.preLaunch(System.out);
		launcher.terminateInstance(System.out);
	}

}