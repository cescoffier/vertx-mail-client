package io.vertx.ext.mail;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.mail.mailutil.MySimpleEmail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * @author <a href="http://oss.lehmann.cx/">Alexander Lehmann</a>
 *
 */
public class MailServiceImpl implements MailService {

  private static final Logger log = LoggerFactory.getLogger(MailService.class);

  private Vertx vertx;
  private JsonObject config;

  private String username;
  private String password;

  private String hostname;

  private int port;

  public MailServiceImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
    username = config.getString("username");
    password = config.getString("password");
    hostname = config.getString("hostname", "localhost");
    port = config.getInteger("port", 25);
  }

  @Override
  public void sendMailString(String email, Handler<AsyncResult<JsonObject>> resultHandler) {
    // not yet implemented
  }

  @Override
  public void start() {
    // may take care of validating the options
    // and configure a queue if we implement one
    log.debug("mail service started");
  }

  @Override
  public void stop() {
    // may shut down the queue, if we implement one
    log.debug("mail service stopped");
  }

  @Override
  public void sendMail(JsonObject emailJson,
      Handler<AsyncResult<JsonObject>> resultHandler) {
    try {
      String text = emailJson.getString("text");
      String bounceAddress = emailJson.getString("bounceAddress");
      String from = emailJson.getString("from");
      // TODO: handle more than one recipient
      String recipient = emailJson.getString("recipient");

      Email email = new MySimpleEmail();
      email.setFrom(from);
      List<InternetAddress> tos = new ArrayList<InternetAddress>();
      tos.add(new InternetAddress(recipient));
      email.setTo(tos);
      email.setSubject(emailJson.getString("subject"));
      email.setBounceAddress(bounceAddress);
      email.setContent(text, "text/plain");

      // use optional as default, this way we do opportunistic unless disabled
      // TODO: can we do this as enum?
      final String starttls=config.getString("starttls", "enabled");
      if(starttls.equals("enabled")) {
        email.setStartTLSEnabled(true);
      }
      if(starttls.equals("required")) {
        email.setStartTLSRequired(true);
      }

      if(config.getString("ssl", "false").equals("true")) {
        email.setSSLOnConnect(true);
      }

      email.setHostName(hostname);
      email.setSmtpPort(port);

      final String login = config.getString("login", "enabled");

      MailVerticle mailVerticle = new MailVerticle(vertx, resultHandler);
      mailVerticle.sendMail(email, username, password, login);
    } catch (EmailException | AddressException e) {
      e.printStackTrace();
    }
  }

//  @Override
//  public void sendMail(Email email, Handler<AsyncResult<String>> resultHandler) {
//  }

}
