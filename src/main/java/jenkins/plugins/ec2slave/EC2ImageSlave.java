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

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.Slave;
import hudson.slaves.ComputerConnector;
import hudson.slaves.NodeProperty;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The {@link EC2ImageSlave} is a slave in the same way {@link DumbSlave} is, i.e.
 * you can create one of these through the normal node creation process.  What makes
 * this class different from DumbSlave is that it binds a Jenkins {@link Computer}
 * not to a running machine, but to an Amazon EC2 instance which is launched from
 * a specified Amazon Machine Image (AMI).  Configuring the {@link EC2ImageSlave}
 * involves specifying all information needed to: 
 * <ol><li>launch the AMI</li>
 * <li>connect to the resulting image using the user selected {@link ComputerConnector}</li> 
 * <li>launch Jenkins as a slave on the EC2 instance
 * 
 * @author Aaron Phillips
 */
public final class EC2ImageSlave extends Slave {
  private static final long serialVersionUID = -3392496004371742586L;

  private String instanceType, accessKey, imageId, secretKey, keypairName;

  private transient EC2ImageLaunchWrapper ec2ImageLaunchWrapper;

  /**
   * The connector that will factory the {@link ComputerLauncher} after providing 
   * it the public DNS hostname
   */
  private ComputerConnector computerConnector;

  @DataBoundConstructor
  public EC2ImageSlave(String secretKey, String accessKey, String imageId, String instanceType, String keypairName,
      String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString,
      ComputerConnector computerConnector, RetentionStrategy retentionStrategy,
      List<? extends NodeProperty<?>> nodeProperties) throws FormException, IOException {
    super(name, nodeDescription, remoteFS, numExecutors, mode, labelString,
        null /* null launcher because we create it dynamically in getLauncher */, retentionStrategy, nodeProperties);

    this.computerConnector = computerConnector;

    if (ec2ImageLaunchWrapper != null && ec2ImageLaunchWrapper.instanceIsRunning()) {
      final String msg = "You cannot change EC2 configuration while the instance is running.";
      if (!secretKey.equals(this.secretKey))
        throw new FormException(msg, "secretKey");
      if (!accessKey.equals(this.accessKey))
        throw new FormException(msg, "accessKey");
      if (!imageId.equals(this.imageId))
        throw new FormException(msg, "imageId");
      if (!instanceType.equals(this.instanceType))
        throw new FormException(msg, "instanceType");
      if (!keypairName.equals(this.keypairName))
        throw new FormException(msg, "keypairName");
    }

    this.secretKey = secretKey;
    this.accessKey = accessKey;
    this.imageId = imageId;
    this.instanceType = instanceType;
    this.keypairName = keypairName;
  }

  @Override
  public ComputerLauncher getLauncher() {
    //Letting the user choose ComputerConnector and returning
    //computerConnector.launch(..) here which will return a ComputerLauncher with the 
    //hostname already set.  This implies that the EC2ImageSlave config will be displaying
    //Computer *Connector* descriptor stuff rather than *Launcher*
    ec2ImageLaunchWrapper = new EC2ImageLaunchWrapper(computerConnector, secretKey, accessKey, imageId, instanceType,
        keypairName);

    setLauncher(ec2ImageLaunchWrapper);

    return ec2ImageLaunchWrapper;
  }

  @Extension
  public static final class DescriptorImpl extends SlaveDescriptor {
    public String getDisplayName() {
      return "EC2 Image Slave";
    }

    //		public FormValidation doCheckImageId(@QueryParameter String value, 
    //				@QueryParameter("secretKey") String secretKey,
    //				@QueryParameter("accessKey") String accessKey,
    //				@QueryParameter("instanceType") String instanceType,
    //				@QueryParameter("keypairName") String keypairName) {
    //			if(ec2ImageLaunchWrapper
    //			  if(looksOk(value))  return FormValidation.ok();
    //			  else                return FormValidation.error("There's a problem here");
    //			}

    public static List<Descriptor<ComputerConnector>> getComputerConnectorDescriptors() {
      return Hudson.getInstance().<ComputerConnector, Descriptor<ComputerConnector>> getDescriptorList(
          ComputerConnector.class);
    }
  }

  public String getSecretKey() {
    return secretKey;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getImageId() {
    return imageId;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public String getKeypairName() {
    return keypairName;
  }

  public ComputerConnector getComputerConnector() {
    return computerConnector;
  }

}
