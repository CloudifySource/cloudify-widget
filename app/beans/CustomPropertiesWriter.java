package beans;

import models.ServerNode;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import utils.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 5/9/14
 * Time: 9:01 AM
 */
public class CustomPropertiesWriter {

    private static Logger logger = LoggerFactory.getLogger(CustomPropertiesWriter.class);

    public void writeProperties( ServerNode server, File file  ){
        logger.info("checking to see if need to write custom properties");
        if (!StringUtils.isEmptyOrSpaces(server.getRecipeProperties())) {
            logger.info("user passed properties for the recipe. writing them to a file [{}]", file);

            Collection<String> newLines = new LinkedList<String>();
            newLines.add("");
            JsonNode recipePropertiesJson = Json.parse(server.getRecipeProperties());

            int i = 0;
            while (recipePropertiesJson.has(i)) {
                JsonNode iNode = recipePropertiesJson.get(i);
                JsonNode value = iNode.get("value");
                String valueStr = "";
                if ( value.isTextual() ) {
                    valueStr = StringUtils.wrapWithQuotes(iNode.get("value").getTextValue());
                }else{
                    valueStr = iNode.get("value").toString();
                }

                newLines.add(iNode.get("key").getTextValue() + "=" + valueStr );
                i++;
            }
            try {
                FileUtils.writeLines(file, newLines, true);
            } catch (Exception e) {
                throw new RuntimeException("unable to write lines to properties file", e);
            }

        } else{
            logger.info("custom properties is empty. nothing to write");
        }
    }
}
