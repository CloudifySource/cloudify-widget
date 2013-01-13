package org.cloudifysource.widget.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: sagib
 * Date: 08/01/13
 * Time: 11:35
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static Map<String, String> getConfiguration() {
        Map<String, String> configuration = new HashMap<String, String>();
        File configurationFile = new File(System.getProperty("user.dir") + "/../conf/dev/me.conf");
        if(!configurationFile.exists()){
            configurationFile = new File(System.getProperty("user.dir") + "/../conf/prod.conf");
        }
        String s;
        try {
            s = convertStreamToString(new FileInputStream(configurationFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String[] split = s.split("\r\n");
        for (String line : split){
            if(line.contains("db.default.driver")){
                configuration.put("driver", line.split("=")[1]);
            }
            else if(line.contains("db.default.url")){
                String startUrl = line.split("=")[1].replace("\"", "");
                String[] splitUrl = startUrl.split("@");
                String[] splitBeginning = splitUrl[0].split("//");
                String userPassword = splitBeginning[1];
                String[] splitUserPassword = userPassword.split(":");
                configuration.put("user", splitUserPassword[0]);
                if(splitUserPassword.length == 2)
                    configuration.put("password", splitUserPassword[1]);
                String[] splitHostSchema = splitUrl[1].split("/");
                configuration.put("url", "jdbc:" + splitBeginning[0] + "//" + splitHostSchema[0]);
                configuration.put("schema", splitHostSchema[1]);
            }
        }
        return configuration;
    }

    public static String convertStreamToString(InputStream is) {
        String ans = "";
        Scanner s = new Scanner(is);
        s.useDelimiter("\\A");
        ans = s.hasNext() ? s.next() : "";
        s.close();
        return ans;
    }

    public static ResultSet runSQLQuery(String sql, Map<String, String> config) {
        try {
            Statement statement = getStatement(config);
            return statement.executeQuery(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query:" + sql, e);
        }
    }

    public static boolean runSQLExecute(String sql, Map<String,String> config) {
        try {
            Statement statement = getStatement(config);
            return statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query:" + sql, e);
        }
    }

    private static Statement getStatement(Map<String, String> config) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        Class.forName(config.get("driver")).newInstance();
        Connection connection = DriverManager.getConnection(config.get("url"), config.get("user"), config.get("password"));
        return connection.createStatement();
    }

    public static boolean dropSchema(){
        try{
            Map<String, String> dbConfiguration = getConfiguration();
            return runSQLExecute("DROP SCHEMA IF EXISTS " + dbConfiguration.get("schema"), dbConfiguration);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static boolean dropUser(String name) {
        try{
            Map<String, String> dbConfiguration = getConfiguration();
            return runSQLExecute("DELETE FROM " + dbConfiguration.get("schema") + "." + "user WHERE first_name='" + name + "'", dbConfiguration);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String ... args) throws FileNotFoundException {

    }

    public static String getHost() {
        return "localhost:9000";
    }
}
