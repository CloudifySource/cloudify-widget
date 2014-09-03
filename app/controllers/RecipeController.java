package controllers;

import beans.config.ServerConfig;
import cloudify.widget.common.StringUtils;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Result;
import server.ApplicationContext;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/3/14
 * Time: 12:27 PM
 */
public class RecipeController extends GsController {


    private static Logger logger = LoggerFactory.getLogger(RecipeController.class);

    public static Result getTempRecipeDownloadLink(){
        validateSession();

        logger.info("generating download link");

        ServerConfig.BootstrapConfiguration bootstrap = ApplicationContext.get().conf().server.bootstrap;

        if ( "wget".equalsIgnoreCase(bootstrap.recipeDownloadMethod) ) {
            return ok(bootstrap.recipeUrl);

        }
        else{
            String accessKey = bootstrap.urlAccessKey;
            String secretKey = bootstrap.urlSecretKey ;

            String recipeUrl = bootstrap.recipeUrl;
            int firstSlash = recipeUrl.indexOf("/", recipeUrl.indexOf("/") + 1);

            String bucketName = StringUtils.substring(recipeUrl, 1, firstSlash);
            String objectKey = StringUtils.substring(recipeUrl, firstSlash+1 );

            logger.info("bucket name is [{}] and objectKey is [{}]", bucketName, objectKey );

            AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials( accessKey , secretKey ));
            s3client.setEndpoint(bootstrap.urlEndpoint);

            java.util.Date expiration = new java.util.Date();
            long msec = expiration.getTime();
            msec += 1000 * 5 ; // 5 seconds.
            expiration.setTime(msec);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, objectKey);


            generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
            generatePresignedUrlRequest.setExpiration(expiration);

            URL s = s3client.generatePresignedUrl(generatePresignedUrlRequest);
            return ok(s.toString());
        }

    }

}
