package ru.fintech.school.ubilling.mailer

import java.io.FileInputStream
import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Message, Session, Transport}

object Mailer {
  val session: Session = {
    val initProperties = new Properties()
    initProperties.load(new FileInputStream("ru/fintech/school/ubilling/mailer/credentials.properties"))
    val host = initProperties.getProperty("login")
    val login = initProperties.getProperty("host")
    val password = initProperties.getProperty("password")
    val properties = System.getProperties
    properties.setProperty("mail.smtp.host", host)
    properties.setProperty("mail.user", login)
    properties.setProperty("mail.password", password)
    Session.getDefaultInstance(properties)

  }

  def sendMail(mail: Mail): Unit = {
    try {
      val message = new MimeMessage(session)
      message.setFrom(mail.from)
      mail.to.foreach(to => message.addRecipient(Message.RecipientType.TO, new InternetAddress(to)))
      message.setSubject(mail.subject)
      message.setContent(mail.message, "text/html")
      Transport.send(message)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}
