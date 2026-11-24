package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import jakarta.activation.FileDataSource;

/**
 * Envoie le rapport de test par e-mail.
 *
 * Aucun identifiant n'est écrit dans le code. Chaque paramètre est lu :
 *   1. dans une variable d'environnement (ex : SMTP_PASSWORD), en priorité ;
 *   2. sinon dans le fichier mail.properties (ignoré par Git).
 *
 * Voir mail.properties.example pour la liste des clés.
 */
public class EmailWithAttachment {

    private static final String CONFIG_FILE = "mail.properties";

    public static void sendMail() {

        Properties config = loadConfig();

        String smtpHost      = get(config, "smtp.host", "SMTP_HOST", true);
        int    smtpPort      = Integer.parseInt(get(config, "smtp.port", "SMTP_PORT", false, "587"));
        String smtpLogin     = get(config, "smtp.login", "SMTP_LOGIN", true);
        String smtpPassword  = get(config, "smtp.password", "SMTP_PASSWORD", true);

        String fromName      = get(config, "mail.from.name", "MAIL_FROM_NAME", false, "CloudBank");
        String fromAddress   = get(config, "mail.from.address", "MAIL_FROM_ADDRESS", false, smtpLogin);
        String recipient     = get(config, "mail.to.address", "MAIL_TO_ADDRESS", true);
        String recipientName = get(config, "mail.to.name", "MAIL_TO_NAME", false, recipient);
        String subject       = get(config, "mail.subject", "MAIL_SUBJECT", false, "Rapport de tests CloudBank");
        String message       = get(config, "mail.body", "MAIL_BODY", false, "Veuillez trouver ci-joint le rapport des tests.");
        String attachmentPath = get(config, "mail.attachment", "MAIL_ATTACHMENT", false, null);

        System.out.println("== Envoi de l'e-mail ==");

        List<AttachmentResource> attachments = new ArrayList<>();
        if (attachmentPath != null) {
            File file = new File(attachmentPath);
            if (file.exists()) {
                attachments.add(new AttachmentResource(file.getName(), new FileDataSource(file)));
            } else {
                System.out.println("Pièce jointe introuvable, e-mail envoyé sans : " + attachmentPath);
            }
        }

        Email email = EmailBuilder.startingBlank()
                .from(fromName, fromAddress)
                .to(recipientName, recipient)
                .withSubject(subject)
                .withPlainText(message)
                .withAttachments(attachments)
                .buildEmail();

        Mailer mailer = MailerBuilder
                .withSMTPServer(smtpHost, smtpPort, smtpLogin, smtpPassword)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10 * 1000)
                .buildMailer();

        mailer.sendMail(email);
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Impossible de lire " + CONFIG_FILE, e);
            }
        }
        return props;
    }

    private static String get(Properties config, String key, String envVar, boolean required) {
        return get(config, key, envVar, required, null);
    }

    private static String get(Properties config, String key, String envVar, boolean required, String defaultValue) {
        String value = System.getenv(envVar);
        if (value == null || value.trim().isEmpty()) {
            value = config.getProperty(key);
        }
        if (value == null || value.trim().isEmpty()) {
            if (required) {
                throw new IllegalStateException(
                        "Paramètre manquant : définir la variable d'environnement " + envVar
                        + " ou la clé '" + key + "' dans " + CONFIG_FILE);
            }
            return defaultValue;
        }
        return value.trim();
    }
}
