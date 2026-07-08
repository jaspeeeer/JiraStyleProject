package com.standardinsurance.intrack.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default {@link EmailSender} that logs the email. Keeps local dev and tests free of SMTP
 * infrastructure; swap for an SMTP-backed sender (Mailhog on :1025) when wiring real email.
 */
@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[email] to={} subject={}\n{}", to, subject, body);
    }
}
