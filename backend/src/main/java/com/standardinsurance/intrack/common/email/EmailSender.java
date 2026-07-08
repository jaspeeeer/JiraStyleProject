package com.standardinsurance.intrack.common.email;

/**
 * Sends transactional emails (invites, password resets). The default {@link LoggingEmailSender}
 * logs the message; a real SMTP implementation (Mailhog locally) can replace it later.
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}
