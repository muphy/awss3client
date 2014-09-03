import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@Component
public class S3Client {
	private final static Logger logger = LoggerFactory.getLogger(S3Client.class);
	final static String BUCKET_NAME = "your_bucket_name";
	final static String PROFILE_BUCKET_NAME = "your_another_bucket_name";
	private final static String PROFILE_PREFIX = "test_prefix";
	final static String filename = "AwsCredentials.properties";
	private AmazonS3 s3;

	@PostConstruct
	public void init() {
		s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider(filename));
		Region northeast = Region.getRegion(Regions.AP_NORTHEAST_1);
		s3.setRegion(northeast);
	}

	public void uploadImage(String bucketName, String objectKey, InputStream fileIn) throws IOException {
		String vucketName = bucketName;
		if (bucketName == null) {
			vucketName = BUCKET_NAME;
			if (objectKey.startsWith(PROFILE_PREFIX)) {
				vucketName = PROFILE_BUCKET_NAME;
			}
		}
		/**
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType("image/jpeg");
		byte[] data = IOUtils.toByteArray(fileIn);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		IOUtils.write(data, os);
		objectMetadata.setContentLength(data.length);
		InputStream is = new ByteArrayInputStream(data);
		PutObjectRequest por = new PutObjectRequest(vucketName, objectKey, is, objectMetadata);
		por.setCannedAcl(CannedAccessControlList.PublicRead);
		s3.putObject(por);
		*/
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType("image/jpeg");
		PutObjectRequest por = new PutObjectRequest(vucketName, objectKey, fileIn, objectMetadata);
		por.setCannedAcl(CannedAccessControlList.PublicRead);
		s3.putObject(por);
	}

	public void uploadImage(String objectKey, InputStream fileIn) throws IOException {
		uploadImage(null, objectKey, fileIn);
	}

	public void deleteObject(String bucketName, String objectKey) {
		String vucketName = bucketName;
		if (bucketName == null) {
			vucketName = BUCKET_NAME;
			if (objectKey.startsWith(PROFILE_PREFIX)) {
				vucketName = PROFILE_BUCKET_NAME;
			}
		}
		s3.deleteObject(vucketName, objectKey);
	}

	public void deleteObject(String objectKey) {
		deleteObject(null, objectKey);
	}

	public void copyObject(String bucketName, String objectKey, String destinationBucketName, String destinationKey, boolean isProfile) {
		String vucketName = bucketName;
		if (bucketName == null) {
			vucketName = BUCKET_NAME;
			if (objectKey.startsWith(PROFILE_PREFIX)) {
				vucketName = PROFILE_BUCKET_NAME;
			}
		}
		String destinationVucketName = destinationBucketName;
		if (bucketName == null) {
			destinationVucketName = BUCKET_NAME;
			if (destinationKey.startsWith(PROFILE_PREFIX)) {
				destinationVucketName = PROFILE_BUCKET_NAME;
			}
		}
		// String beforeEtag = getObjectEtag(destinationKey);
		CopyObjectRequest copyObjRequest = new CopyObjectRequest(vucketName, objectKey, destinationVucketName, destinationKey);
		copyObjRequest.setCannedAccessControlList(CannedAccessControlList.PublicRead);
		CopyObjectResult copyObjectResult = s3.copyObject(copyObjRequest);
		// String afterEtag = copyObjectResult.getETag();
		// System.out.println(String.format("beforeEtag:%s,afterEtag:%s",beforeEtag,afterEtag));
	}

	public void copyObject(String key, String destinationKey) {
		try {
			copyObject(null, key, null, destinationKey, false);
		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, " + "which means your request made it " + "to Amazon S3, but was rejected with an error " + "response for some reason.");
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, " + "which means the client encountered " + "an internal error while trying to " + " communicate with S3, "
					+ "such as not being able to access the network.");
			logger.error("Error Message: " + ace.getMessage());
		}
	}

	// public void copyProfileObject(String key, String destinationKey) {
	// copyObject(null,key,null,destinationKey,true);
	// }

	public String getObjectEtag(String objectKey) {
		String vucketName = BUCKET_NAME;
		if (objectKey.startsWith(PROFILE_PREFIX)) {
			vucketName = PROFILE_BUCKET_NAME;
		}
		GetObjectRequest getObjectRequest = new GetObjectRequest(vucketName, objectKey);
		S3Object s3Object = s3.getObject(getObjectRequest);
		return s3Object.getObjectMetadata().getETag();
	}

	public long getObjectSize(String objectKey) {
		String vucketName = BUCKET_NAME;
		if (objectKey.startsWith(PROFILE_PREFIX)) {
			vucketName = PROFILE_BUCKET_NAME;
		}
		GetObjectRequest getObjectRequest = new GetObjectRequest(vucketName, objectKey);
		S3Object s3Object = s3.getObject(getObjectRequest);
		return s3Object.getObjectMetadata().getContentLength();
	}

	public static void main(String args[]) {
		// 54,535
		File f = new File("C:/Users/Public/Pictures/Sample Pictures/Tulips.jpg");
		System.out.println(f.length());
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] b = IOUtils.toByteArray(new FileInputStream(f));
			System.out.println(b.length);
//			byte[] c = IOUtils.toByteArray(new FileInputStream(f));
//			System.out.println(c.length);
			S3Client s3Client = new S3Client();
			s3Client.init();
			s3Client.uploadImage(PROFILE_PREFIX+"testfile", fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
}
