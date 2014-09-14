/***************
 * Cloud configuration file for the Amazon ec2 cloud. Uses the default jclouds-based cloud driver.
 * See org.cloudifysource.domain.cloud.Cloud for more details.
 * @author barakme
 *
 */
 
def EC2_ACCESS_KEY_ID = "${user}" as String
def EC2_SECRET_ACCESS_KEY = "${apiKey}" as String
def BLU_EC2_KEYPAIR_FILE_NAME = "${keyFile}" as String
def BLU_EC2_KEYPAIR_NAME = "${keyPair}" as String

cloud {
	// Mandatory. The name of the cloud, as it will appear in the Cloudify UI.
	name = CLOUD_DISPLAY_NAME

	/********
	 * General configuration information about the cloud driver implementation.
	 */
	configuration {
		// Optional. The cloud implementation class. Defaults to the build in jclouds-based provisioning driver.
		className "org.cloudifysource.esc.driver.provisioning.jclouds.DefaultProvisioningDriver"
		storageClassName "org.cloudifysource.esc.driver.provisioning.storage.aws.EbsStorageDriver"
		// Optional. The template name for the management machines. Defaults to the first template in the templates section below.
		managementMachineTemplate "BLU_EC2"
		// Optional. Indicates whether internal cluster communications should use the machine private IP. Defaults to true.
		connectToPrivateIp true
		
		// Optional. Path to folder where management state will be written. Null indicates state will not be written.
		persistentStoragePath persistencePath
		
		
	}

	/*************
	 * Provider specific information.
	 */
	provider {
		// Mandatory. The name of the provider.
		// When using the default cloud driver, maps to the Compute Service Context provider name.
		provider "aws-ec2"


		// Optional. The HTTP/S URL where cloudify can be downloaded from by newly started machines. Defaults to downloading the
		// cloudify version matching that of the client from the cloudify CDN.
		// Change this if your compute nodes do not have access to an internet connection, or if you prefer to use a
		// different HTTP server instead.
		// IMPORTANT: the default linux bootstrap script appends '.tar.gz' to the url whereas the default windows script appends '.zip'.
		// Therefore, if setting a custom URL, make sure to leave out the suffix.
		//cloudifyUrl "http://repository.cloudifysource.org/softlayer/gigaspaces-cloudify-2.7.1-m6-b6205-130bmcci"

		// Mandatory. The prefix for new machines started for servies.
		machineNamePrefix INSTANCE_NAME_PREFIX
		// Optional. Defaults to true. Specifies whether cloudify should try to deploy services on the management machine.
		// Do not change this unless you know EXACTLY what you are doing.


		//
		managementOnlyFiles ([])

		// Optional. Logging level for the intenal cloud provider logger. Defaults to INFO.
		sshLoggingLevel "WARNING"

		// Mandatory. Name of the new machine/s started as cloudify management machines. Names are case-insensitive.
		managementGroup MANAGEMENT_NAME_PREFIX
		// Mandatory. Number of management machines to start on bootstrap-cloud. In production, should be 2. Can be 1 for dev.
		numberOfManagementMachines 1


		reservedMemoryCapacityPerMachineInMB 1024

	}

	/*************
	 * Cloud authentication information
	 */
	user {
		// Optional. Identity used to access cloud.
		// When used with the default driver, maps to the identity used to create the ComputeServiceContext.
		user EC2_ACCESS_KEY_ID

		// Optional. Key used to access cloud.
		// When used with the default driver, maps to the credential used to create the ComputeServiceContext.
		apiKey EC2_SECRET_ACCESS_KEY
	}
	
	cloudStorage {
		
				templates ([
				
					SMALL_BLOCK : storageTemplate{
						deleteOnExit true
						size 5
						path "/storage"
						namePrefix "cloudify-storage-volume"
						deviceName "/dev/sdc"
						fileSystemType "ext4"
						custom ([:])
					}
				])
	}

	cloudCompute {
		
		/***********
		 * Cloud machine templates available with this cloud.
		 */
		templates ([
					// Mandatory. Template Name.
					BLU_EC2 : computeTemplate{
					
					installer {
						connectionTestRouteResolutionTimeoutMillis 10000
						connectionTestIntervalMillis 5000
						connectionTestConnectTimeoutMillis 10000				 
						fileTransferConnectionTimeoutMillis 10000
						fileTransferRetries 50						
						fileTransferConnectionRetryIntervalMillis 5000				 					
					}
					
						// Mandatory. Image ID.
						imageId EC2_REGION + "/" + BLU_EC2_AMI
						// Mandatory. Files from the local directory will be copied to this directory on the remote machine.
						remoteDirectory "/${BLU_EC2_SSH_USER}/gs-files"						
						// Mandatory. Amount of RAM available to machine.
						machineMemoryMB 1600
						// Mandatory. Hardware ID.
						hardwareId BLU_EC2_HARDWARE_ID
						// Optional. Location ID.
						locationId EC2_REGION
						// Mandatory. All files from this LOCAL directory will be copied to the remote machine directory.
						localDirectory "upload"
						// Optional. Name of key file to use for authenticating to the remot machine. Remove this line if key files
						// are not used.
						keyFile BLU_EC2_KEYPAIR_FILE_NAME
	
						username BLU_EC2_SSH_USER
						
						//fileTransfer org.cloudifysource.domain.cloud.FileTransferModes.SCP
						// Additional template options.
						// When used with the default driver, the option names are considered
						// method names invoked on the TemplateOptions object with the value as the parameter.
						options ([
									"securityGroups" : [BLU_EC2_SECURITY_GROUP]as String[],
									"keyPair" : BLU_EC2_KEYPAIR_NAME
								])
	
						// when set to 'true', agent will automatically start after reboot.
						autoRestartAgent true
	
						// Optional. Overrides to default cloud driver behavior.
						// When used with the default driver, maps to the overrides properties passed to the ComputeServiceContext a
						overrides (["jclouds.ec2.ami-query":"",
									"jclouds.ec2.cc-ami-query":""])
	
						// enable sudo.
						privileged true
	
	
	
					}
	
				])
	
	}

	/*****************
	 * Optional. Custom properties used to extend existing drivers or create new ones.
	 */
	custom ([
		"org.cloudifysource.clearRemoteDirectoryOnStart" : true , 
        "org.cloudifysource.stop-management-timeout-in-minutes" : 20
	])
}