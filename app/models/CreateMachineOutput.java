package models;

import beans.HmacImpl;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.db.ebean.Model;
import server.ApplicationContext;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/2/14
 * Time: 12:34 AM
 */
@Entity
public class CreateMachineOutput extends Model {

    @Id
    private Long id;

    private int maxTries;

    private int currentTry;

    private Long created;

    private int exitCode;

    @Lob
    private String output;

    @Lob
    private String exception;

    private boolean alertWasSent;

    private boolean outputRead = false;


    public static Finder<Long, CreateMachineOutput> finder = new Finder<Long, CreateMachineOutput>( Long.class, CreateMachineOutput.class );

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isOutputRead() {
        return outputRead;
    }

    public void setOutputRead(boolean errorRead) {
        this.outputRead = errorRead;
    }

    public Long getCreated() {
        return created;
    }


    public int getCurrentTry() {
        return currentTry;
    }

    public void setCurrentTry(int currentTry) {
        this.currentTry = currentTry;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @JsonIgnore
    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @JsonIgnore
    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    @Transient
    public boolean isHasException(){
        return getException() != null;
    }

    @Transient
    public boolean isHasOutput(){
        return getOutput() != null;
    }

    private String sign( String link ){
        HmacImpl hmac = ApplicationContext.get().getHmac();
        return hmac.sign( link );
    }

    private String getSignedLink( String link ){
        return link + "?hmac=" + sign(link);
    }

    @Transient
    public String getExceptionLink(){
        return isHasException() ? getSignedLink("/backend/createMachineOutput/" + id + "/exception") : null;
    }

    @Transient
    public String getOutputLink(){
        return isHasOutput() ? getSignedLink("/backend/createMachineOutput/" + id + "/output") : null;
    }


    public boolean isAlertWasSent() {
        return alertWasSent;
    }

    public void setAlertWasSent(boolean alertWasSent) {
        this.alertWasSent = alertWasSent;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
